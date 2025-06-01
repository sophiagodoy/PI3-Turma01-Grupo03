// TELA PRINCIPAL DO APLICATIVO

package br.com.ibm.superid

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import br.com.ibm.superid.ui.theme.SuperIDTheme
import br.com.ibm.superid.ui.theme.core.util.BackButtonBar
import br.com.ibm.superid.ui.theme.core.util.StandardBoxPopUp
import br.com.ibm.superid.ui.theme.core.util.SuperIDHeader
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import br.com.ibm.superid.ui.theme.core.util.decryptPassword
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import br.com.ibm.superid.ui.theme.core.util.ConfirmDeleteCategoryDialog
import br.com.ibm.superid.ui.theme.core.util.ConfirmPasswordDialog
import br.com.ibm.superid.ui.theme.core.util.CustomOutlinedTextField
import br.com.ibm.superid.ui.theme.core.util.PasswordDetailDialog
import br.com.ibm.superid.ui.theme.core.util.RemovePasswordDialog

import br.com.ibm.superid.ui.theme.core.util.deletePasswordById
import br.com.ibm.superid.ui.theme.core.util.reauthenticateUser
import br.com.ibm.superid.ui.theme.core.util.resendVerificationEmail
import br.com.ibm.superid.ui.theme.core.util.checkEmailVerified

// Declarando a Activity da página principal do aplicativo
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                MainScreen()
            }
        }
    }
}

// Função responsável pela interface da tela principal do aplicativo
@Composable
fun MainScreen() {

    val context = LocalContext.current

    var passwords by remember { mutableStateOf<List<SenhaItem>>(emptyList()) }
    var categoriesList by remember { mutableStateOf<List<String>>(emptyList()) }
    var showAddPopUp by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showQRInstructionDialog by remember { mutableStateOf(false) }
    var showConfirmPasswordDialog by remember { mutableStateOf(false) }

    // Carrega as senhas do Firestore assim que a tela iniciar
    fun loadDataFireBase() {

        val user = Firebase.auth.currentUser

        if (user != null) {
            val db = Firebase.firestore

            // Carrega categorias
            db.collection("users")
                .document(user.uid)
                .collection("categorias")
                .get()
                .addOnSuccessListener { catResult ->
                    val listaCategorias = catResult.documents.mapNotNull { it.getString("nome") }
                    categoriesList = listaCategorias
                }
                .addOnFailureListener {
                    Log.e("Firestore", "Erro ao carregar categorias: ${it.message}")
                }

            // Carrega senhas
            db.collection("users")
                .document(user.uid)
                .collection("senhas")
                .get()
                .addOnSuccessListener { senhaResult ->
                    val tempList = mutableListOf<SenhaItem>()
                    for (doc in senhaResult) {
                        try {
                            val decrypted = decryptPassword(
                                encrypted = doc.getString("senha") ?: "",
                                ivBase64 = doc.getString("iv") ?: ""
                            )
                            tempList.add(
                                SenhaItem(
                                    titulo = doc.getString("titulo") ?: "",
                                    login = doc.getString("login") ?: "",
                                    senha = decrypted,
                                    descricao = doc.getString("descricao") ?: "",
                                    categoria = doc.getString("categoria") ?: "",
                                    id = doc.id
                                )
                            )
                        } catch (e: Exception) {
                            Log.e("Decrypt", "Erro ao descriptografar: ${e.message}")
                        }
                    }
                    passwords = tempList
                }
                .addOnFailureListener {
                    Log.e("Firestore", "Erro ao carregar senhas: ${it.message}")
                }
        }
    }

    // Carrega dados na inicialização da tela
    LaunchedEffect(Unit) {
        loadDataFireBase()
    }
    val categoriasMap = remember(passwords, categoriesList) {

        // Começa com um mapa de todas as categorias, incluindo as que podem estar vazias
        val map = categoriesList.associateWith { mutableListOf<SenhaItem>() }.toMutableMap()

        // Agrupa as senhas em suas categorias
        passwords.forEach { senha ->
            val cat = senha.categoria
            if (map.containsKey(cat)) {
                map[cat]?.add(senha)
            } else {
                // Se senha tem categoria que não está na lista de categorias, adiciona também
                map[cat] = mutableListOf(senha)
            }
        }
        map
    }

    fun deleteCategory(categoria: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val user = Firebase.auth.currentUser
        if (user != null) {
            val db = Firebase.firestore
            db.collection("users")
                .document(user.uid)
                .collection("categorias")
                .whereEqualTo("nome", categoria)
                .get()
                .addOnSuccessListener { query ->
                    if (query.documents.isNotEmpty()) {
                        val docId = query.documents[0].id
                        db.collection("users")
                            .document(user.uid)
                            .collection("categorias")
                            .document(docId)
                            .delete()
                            .addOnSuccessListener {
                                onSuccess()
                                loadDataFireBase() // Atualiza a lista após exclusão
                            }
                            .addOnFailureListener { e -> onError(e) }
                    } else {
                        onError(Exception("Categoria não encontrada"))
                    }
                }
                .addOnFailureListener { e -> onError(e) }
        }
    }

    // Rolagem de tela
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background)
    ) {

        // Cabeçalho visual personalizado
        SuperIDHeader()

        IconButton(
            onClick = {
                showExitDialog = true // exibe o dialogo de sair do aplicativo
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 10.dp, top = 95.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = "Sair do aplicativo",
                modifier = Modifier.size(35.dp)
            )
        }

        Column(modifier = Modifier
            .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(130.dp))
            categoriasMap.forEach { (categoria, itens) ->
                Spacer(modifier = Modifier.height(16.dp))
                Categories(
                    title = categoria,
                    items = itens,
                    onDeleteCategory = { cat ->
                        if (itens.isEmpty()) {
                            // Confirma antes de excluir
                            // Aqui podemos abrir diálogo para confirmar
                            deleteCategory(cat,
                                onSuccess = {
                                    Toast.makeText(context, "Categoria excluída", Toast.LENGTH_SHORT).show()
                                    loadDataFireBase()
                                },
                                onError = { e ->
                                    Toast.makeText(context, "Erro ao excluir categoria: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            )
                        } else {
                            Toast.makeText(context, "Não é possível excluir categoria com senhas", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onPasswordRemoved = {
                        loadDataFireBase()
                    }
                )
            }
            Spacer(modifier = Modifier.height(110.dp))
        }

        // Botão flutuante de adicionar senha e categoria
        FloatingActionButton(
            onClick = {
                showAddPopUp = true // pop-up de adicionar senha e categoria é ativado
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 90.dp, end = 15.dp)
                .size(70.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Adicionar Senha e Categoria"
            )
        }

        // Botão flutuante do QR Code
        FloatingActionButton(
            onClick = {
                showConfirmPasswordDialog = true // pop-up do QR é ativado
            },
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Image(
                painter = painterResource(id = R.drawable.qrcodewhite),
                contentDescription = "QR Code",
                modifier = Modifier.size(80.dp)
            )
        }

        // Pop-up de adicionar senha e categoria
        if (showAddPopUp) { // Se showAddPopUp = true abre o pop-up

            // Exibe uma caixa de diálogo (pop-up)
            Dialog(
                onDismissRequest = {
                    showAddPopUp = false // Fecha o pop-up ao tocar fora da tela
                }
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {

                        // Exibe um ícone de voltar e fecha o pop-up (false)
                        BackButtonBar(
                            onBackClick = {
                                showAddPopUp = false
                            }
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {

                                // Botão de adicionar categoria
                                Button(
                                    onClick = {
                                        showAddPopUp = false // fecha o pop-up
                                        context.startActivity(Intent(context, AddCategoryActivity::class.java))
                                    },
                                    modifier = Modifier
                                        .height(50.dp)
                                        .width(250.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Text("Adicionar Categoria")
                                }

                                Spacer(Modifier.height(10.dp))

                                // Botão de adicionar senha
                                Button(
                                    // Fecha o botão ao clicar e vai para a AddPasswordActivity
                                    onClick = {
                                        showAddPopUp = false
                                        context.startActivity(Intent(context, AddPasswordActivity::class.java))
                                    },
                                    modifier = Modifier
                                        .height(50.dp)
                                        .width(250.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary // cor de fundo do botão
                                    )
                                ) {
                                    Text("Adicionar Senha")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Baseado em: https://www.geeksforgeeks.org/alertdialog-in-android-using-jetpack-compose/?utm_source
        // Se a showExitDialog estiver como true (ativa)
        if (showExitDialog) {

            // Exibe uma caixa de diálogo (pop-up)
            AlertDialog(
                onDismissRequest = { showExitDialog = false }, // Fecha o pop-up ao tocar fora
                title = { Text("Deseja sair do SuperID?") },

                // Botão de confirmação (fecha o pop-up quando clicado)
                confirmButton = {
                    TextButton(
                        // Define showExistDialog como inativo e muda de página
                        onClick = {
                            // 1) Desloga do Firebase
                            Firebase.auth.signOut()

                            // 2) Vai pra tela de login

                            showExitDialog = false
                            val intent = Intent(context, AccessOptionActivity::class.java)
                            context.startActivity(intent)

                            // 3) Fecha esta Activity pra não voltar ao apertar "voltar"
                            (context as? Activity)?.finish()
                        }
                    ) {
                        Text(
                            text = "Sim",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },

                // Botão de cancelamento
                dismissButton = {
                    TextButton(
                        onClick = {
                            showExitDialog = false // Define showExistDialog como inativo
                        }
                    ) {
                        Text(
                            text = "Não",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }

        if (showConfirmPasswordDialog) {
            ConfirmPasswordDialog(
                onDismiss = { showConfirmPasswordDialog = false },
                onConfirmed = {
                    showQRInstructionDialog = true // ativa o segundo popup
                }
            )
        }

        if (showQRInstructionDialog) {
            AlertDialog(
                onDismissRequest = { showQRInstructionDialog = false },
                title = { Text("Instrução para Leitura") },
                text = { Text("Aponte a câmera do celular para o QR Code exibido no site para realizar o login.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showQRInstructionDialog = false
                            val intent = Intent(
                                context,
                                br.com.ibm.superid.permissions.MainActivity::class.java
                            )
                            context.startActivity(intent)
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showQRInstructionDialog = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }

    }
}

@Composable
fun Categories(
    title: String,
    items: List<SenhaItem>,
    onDeleteCategory: (String) -> Unit,
    onPasswordRemoved: () -> Unit
) {
    // Estado que controla se a lista da categoria está expandida ou recolhida
    var expanded by remember { mutableStateOf(false) }

    // Estado para armazenar qual senha foi selecionada para mostrar o detalhe
    var selectedItem by remember { mutableStateOf<SenhaItem?>(null) }

    // Composable que exibe o card da categoria com seu conteúdo e ações
    CategoryCard(
        title = title,
        expanded = expanded,
        onExpandToggle = { expanded = !expanded },  // alterna estado expandido/recolhido
        items = items,
        onItemClick = { selectedItem = it },        // seta o item selecionado para mostrar detalhes
        onDeleteCategory = onDeleteCategory          // função para excluir categoria
    )

    // Se um item foi selecionado, mostra o diálogo com detalhes da senha
    selectedItem?.let {
        PasswordDetailDialog(
            item = it,
            onDismiss = { selectedItem = null },   // fecha o diálogo ao dispensar
                    onPasswordRemoved = {
                // 1) fecha o diálogo de detalhes
                selectedItem = null
                // 2) avisa ao MainScreen para recarregar
                onPasswordRemoved()
            }
        )
    }
}

@Composable
fun CategoryCard(
    title: String,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    items: List<SenhaItem>,
    onItemClick: (SenhaItem) -> Unit,
    onDeleteCategory: (String) -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Diálogo de confirmação de exclusão da categoria
    if (showDeleteConfirmation) {
        ConfirmDeleteCategoryDialog(
            categoryName = title,
            onConfirm = {
                if (items.isEmpty()) {
                    onDeleteCategory(title)
                } else {
                    Toast.makeText(
                        context,
                        "Não é possível excluir categoria com senhas",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onDismiss = { showDeleteConfirmation = false }
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // --- CABEÇALHO DA CATEGORIA (fundo = primary, mesmo verde do botão) ---
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(5.dp),
        colors = CardDefaults.cardColors(
            // Aqui usamos a cor primary do seu tema
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column {
            // Linha clicável do cabeçalho (título + ícone)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    // Texto em onPrimary para contrastar sobre primary
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Row {
                    IconButton(onClick = { onExpandToggle() }) {
                        Icon(
                            imageVector = if (expanded)
                                Icons.Default.KeyboardArrowUp
                            else
                                Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Recolher" else "Expandir",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    if (title != "Sites Web") {
                        IconButton(
                            onClick = { showDeleteConfirmation = true },
                            enabled = title != "Sites Web",
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Excluir Categoria",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            // --- LISTA DE SENHAS (quando expanded == true) ---
            if (expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        // Mantém o fundo claro (surfaceVariant) que você já gosta
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemClick(item) }
                                .padding(horizontal = 30.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.titulo,
                                fontSize = 23.sp,
                                modifier = Modifier.weight(1f),
                                // Texto em onSurfaceVariant para legibilidade sobre surfaceVariant
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Mais opções de senha",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}




// Modelo de dados para representar uma senha
data class SenhaItem(
    val id: String,
    val titulo: String,
    val login: String,
    val senha: String,
    val descricao: String,
    val categoria: String
)

// Preview da tela principal
@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES
)
@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = UI_MODE_NIGHT_NO
)@Composable
fun PreviewMainScreen() {
    SuperIDTheme(
        dynamicColor = false
    ) {
        MainScreen()
    }
}
