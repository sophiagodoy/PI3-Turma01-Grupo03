// TELA PRINCIPAL DO APLICATIVO

// TODO: Decidir se vai remover a categoria pelo X ou se, removendo todas as senhas da categoria, ela e removida tambem
package br.com.ibm.superid

import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
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


// Classe principal que define a Activity inicial do aplicativo
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ativa o modo de bordas estendidas
        enableEdgeToEdge()
        // Define o conteúdo da tela com o tema do app
        setContent {
            SuperIDTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    PreviewMainScreen()
                }
            }
        }
    }
}

@Composable
fun StandardAddPopup(
    onDismiss: () -> Unit,
    onAddPassword: () -> Unit,
    onAddCategory: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 8.dp,
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            BackButtonBar(onBackClick = onDismiss)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Button(
                        onClick = onAddPassword,
                        modifier = Modifier
                            .height(50.dp)
                            .width(250.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Adicionar Senha")
                    }

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = onAddCategory,
                        modifier = Modifier
                            .height(50.dp)
                            .width(250.dp)
                    ) {
                        Text("Adicionar Categoria")
                    }
                }
            }
        }
    }
}

@Composable
fun BottomBar(
    onLogoutClick: () -> Unit,
    onQRClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    BottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp), // padding horizontal para afastar das bordas
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícone de Logout
            IconButton(onClick = onLogoutClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Logout",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Ícone de QR Code
            IconButton(onClick = onQRClick) {
                Icon(
                    painter = painterResource(id = R.drawable.qrcode),
                    contentDescription = "QR Code",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            // Ícone de Perfil
            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Perfil",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}


// Modelo de dados para representar uma senha
data class SenhaItem(
    val id: String,
    val titulo: String,
    val senha: String,
    val descricao: String,
    val categoria: String
)

// função que representa a tela principal do app
@Composable
fun MainScreen() {
    // Pega o context da minha Activity
    val context = LocalContext.current

    // Declarando variáveis
    var passwords by remember { mutableStateOf<List<SenhaItem>>(emptyList()) }

    // Variáveis que controlam a visibilidade de três diálogos (pop-up)
    var showAddPopUp by remember { mutableStateOf(false) }
    var showQRCodePopUp by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Carrega as senhas do Firestore assim que a tela iniciar
    LaunchedEffect(Unit) {
        val user = Firebase.auth.currentUser
        if (user != null) {
            Firebase.firestore.collection("users")
                .document(user.uid)
                .collection("senhas")
                .get()
                .addOnSuccessListener { result ->
                    val tempList = mutableListOf<SenhaItem>()
                    for (doc in result) {
                        try {
                            val decrypted = decryptPassword(
                                encrypted = doc.getString("senha") ?: "",
                                ivBase64 = doc.getString("iv") ?: ""
                            )
                            tempList.add(
                                SenhaItem(
                                    titulo = doc.getString("titulo") ?: "",
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

    // Agrupa as senhas por categoria
    val categorias = passwords.groupBy { it.categoria }

    // Estrutura do layout principal
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Conteúdo principal (scrollável)
        Box(
            modifier = Modifier.weight(1f)
        ) {
            SuperIDHeader()

            // Column responsável por montar lista de categorias e senha
            Column(modifier = Modifier.padding(16.dp)) {
                Spacer(modifier = Modifier.height(130.dp))
                categorias.forEach { (categoria, itens) ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Categories(title = categoria, items = itens)
                }
            }

            // Botão flutuante de adicionar senha e categoria
            FloatingActionButton(
                onClick = { showAddPopUp = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 90.dp, end = 15.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Adicionar Senha e Categoria"
                )
            }

            // Pop-up de adicionar senha e categoria
            if (showAddPopUp) {
                Dialog(
                    onDismissRequest = { showAddPopUp = false }
                ) {
                    StandardAddPopup(
                        onDismiss = { showAddPopUp = false },
                        onAddPassword = {
                            showAddPopUp = false
                            context.startActivity(Intent(context, AddPasswordActivity::class.java))
                        },
                        onAddCategory = {
                            showAddPopUp = false
                            context.startActivity(Intent(context, AddCategoryActivity::class.java))
                        }
                    )
                }
            }

            // Popup de leitura do QR Code
            if (showQRCodePopUp) {
                AlertDialog(
                    onDismissRequest = { showQRCodePopUp = false },
                    title = {
                        BackButtonBar(onBackClick = { showQRCodePopUp = false })
                    },
                    confirmButton = {
                        TextButton(onClick = { showQRCodePopUp = false }) {
                            Text("Esse pop-up nao vai existir, ele abre a camera direto.")
                        }
                    }
                )
            }

            // Diálogo de confirmação de saída
            if (showExitDialog) {
                AlertDialog(
                    onDismissRequest = { showExitDialog = false },
                    title = { Text("Deseja sair do SuperID?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showExitDialog = false
                                val intent = Intent(context, AccessOptionActivity::class.java)
                                context.startActivity(intent)
                            }
                        ) {
                            Text(
                                text = "Sim",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showExitDialog = false }
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

            // Diálogo de confirmação de logout
            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("Confirmar Logout") },
                    text = { Text("Tem certeza que deseja sair da sua conta?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showLogoutDialog = false
                                Firebase.auth.signOut()
                                context.startActivity(Intent(context, AccessOptionActivity::class.java))
                            }
                        ) {
                            Text("Sair", color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showLogoutDialog = false }
                        ) {
                            Text("Cancelar", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            }
        }

        // BottomBar fixa na parte inferior
        BottomBar(
            onLogoutClick = { showLogoutDialog = true },
            onQRClick = { showQRCodePopUp = true },
            onProfileClick = {
                context.startActivity(Intent(context, ProfileActivity::class.java))
            }
        )
    }
}

// Função que controla a expansão/recolhimento de um diálogo
@Composable
fun Categories(title: String, items: List<SenhaItem>) {

    // Declarando as variáveis
    var expanded by remember { mutableStateOf(false) } // Controla se a lista dentro de categoria está aberta ou fechada
    var selectedItem by remember { mutableStateOf<SenhaItem?>(null) } // Guarda o item selecionado para mostrar o diálogo (pop-up)

    // Exibição de um Card com as informações de cada categoria
    CategoryCard(
        title = title,
        expanded = expanded,
        onExpandToggle = { expanded = !expanded },
        items = items,
        onItemClick = { selectedItem = it }
    )

    selectedItem?.let {
        PasswordDetailDialog(
            item = it,
            onDismiss = { selectedItem = null }
        )
    }
}

// Função que mostra uma categoria e sua lista de senhas quando está expandido
@Composable
fun CategoryCard(
    title: String,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    items: List<SenhaItem>,
    onItemClick: (SenhaItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(5.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = {
                        onExpandToggle()
                    }
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            // Exibe a lista de itens se expanded = true
            if (expanded) {
                items.forEach { item ->
                    Text(
                        text = item.titulo,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 30.dp, vertical = 8.dp)
                            .clickable { onItemClick(item) }
                    )
                }
            }
        }
    }
}

// Função que mostra um pop-up com todos os detalhes de uma senha que o usuário clicou na lista
@Composable
fun PasswordDetailDialog(item: SenhaItem, onDismiss: () -> Unit) {
    // Controlo o context da minha Activity
    val context = LocalContext.current

    // Variável que controla se vai aparecer o diálogo interno de confirmação de remoção da senha
    var showRemovePopUp by remember { mutableStateOf(false) }

    // Exibe o diálogo (pop-up)
    Dialog(onDismissRequest = onDismiss) {

        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.background
        ) {

            Column(modifier = Modifier.fillMaxWidth()) {

                // Seta de voltar
                BackButtonBar(onBackClick = onDismiss)

                Column(modifier = Modifier.padding(16.dp)) {

                    // Exibo o título da senha
                    Text(
                        text = item.titulo,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Exibo a senha
                    Text("Senha:", fontWeight = FontWeight.Bold)
                    StandardBoxPopUp {
                        Text(item.senha)
                    }

                    // Exibo a descrição da senha
                    Text("Descrição:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    StandardBoxPopUp {
                        Text(item.descricao)
                    }

                    // Exibo a categoria da senha
                    Text("Categoria:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    StandardBoxPopUp {
                        Text(item.categoria)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Crio um Rox (linha) com o botão alterar e remover
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        // Botão alterar que abre a ChangePasswordActivity com os dados da senha
                        Button(
                            onClick = {
                                val intent = Intent(context, ChangePasswordActivity::class.java).apply {
                                    putExtra("PASSWORD_ID", item.id)
                                    putExtra("PASSWORD_TITLE", item.titulo)
                                    putExtra("PASSWORD_VALUE", item.senha)
                                    putExtra("PASSWORD_DESCRIPTION", item.descricao)
                                    putExtra("PASSWORD_CATEGORY", item.categoria)
                            }
                            context.startActivity(intent)
                        },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text("Alterar")
                        }

                        // Botão remover que apenas ativa o diálogo de confirmação (se deseja ou não remover a senha)
                        Button(
                            onClick = { showRemovePopUp = true },
                                colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                                )
                        ) {
                            Text("Remover")
                        }
                    }

                    // Diálogo de confirmação de remoção de senha
                    // Ativado quando showRemovePopUp = true
                    if (showRemovePopUp) {
                        RemovePasswordDialog(
                            item = item,
                            onDismiss = { showRemovePopUp = false },
                            onSuccess = {
                                Toast.makeText(context, "Senha excluída com sucesso!", Toast.LENGTH_SHORT).show()
                                showRemovePopUp = false
                                onDismiss()
                            },
                            onError = { e ->
                                Toast.makeText(context, "Erro ao excluir: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

// Função que exibe um diálogo de confirmação para excluir definitivamente a senha do Firestore
@Composable
fun RemovePasswordDialog(
    item: SenhaItem,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    onError: (Exception) -> Unit
) {

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                // Seta de voltar
                BackButtonBar(onBackClick = onDismiss)

                Column(modifier = Modifier.padding(16.dp)) {

                    // Mensagem de confirmação
                    Text(
                        text = "Certeza que deseja remover a senha?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Crio um Row (linha) com botão cancelar e remover
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {

                        // Botão cancelar (penas decha o diálogo)
                        Button(
                            onClick = onDismiss
                        ) {
                            Text("Cancelar")
                        }

                        // Botão remover (executa a exclusão no Firestore)
                        Button(
                            onClick = {
                                val user = Firebase.auth.currentUser
                                user?.uid?.let { uid ->
                                    Firebase.firestore
                                        .collection("users")
                                        .document(uid)
                                        .collection("senhas")
                                        .document(item.id)
                                        .delete()
                                        .addOnSuccessListener { onSuccess() }
                                        .addOnFailureListener { e -> onError(e) }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Remover")
                        }
                    }
                }
            }
        }
    }
}

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