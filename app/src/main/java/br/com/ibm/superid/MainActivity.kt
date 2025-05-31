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
import br.com.ibm.superid.ui.theme.core.util.CustomOutlinedTextField
import br.com.ibm.superid.ui.theme.core.util.reauthenticateUser

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

// função que representa a tela principal do app
@Composable
fun MainScreen() {

    // Pega o context da minha Activity
    val context = LocalContext.current

    // Declarando variáveis
    var passwords by remember { mutableStateOf<List<SenhaItem>>(emptyList()) }
    var categoriesList by remember { mutableStateOf<List<String>>(emptyList()) }

    // Variáveis que controlam a visibilidade de três diálogos (pop-up)
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

    val scrollState = rememberScrollState()

    // Estrutura do layout principal
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background)
    ) {
        SuperIDHeader()
        IconButton(
            onClick = { showExitDialog = true },
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
                                },
                                onError = { e ->
                                    Toast.makeText(context, "Erro ao excluir categoria: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            )
                        } else {
                            Toast.makeText(context, "Não é possível excluir categoria com senhas", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            // Adiciona espaçamento no fim para o botão de QR Code não sobrepor
            Spacer(modifier = Modifier.height(110.dp))
        }

        // Botão flutuante de adicionar senha e categoria
        FloatingActionButton(
            onClick = { showAddPopUp = true }, // showAddPopUp é ativada
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 90.dp, end = 15.dp)
                .size(70.dp), // aumenta o tamanho do botão
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
                showConfirmPasswordDialog = true
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
        // Se showAddPopUp = true abre o pop-up
        if (showAddPopUp) {

            // Exibe uma caixa de diálogo (pop-up)
            Dialog(
                onDismissRequest = {
                    showAddPopUp = false // Fecha o pop-up ao tocar fora da tela
                }
            ) {

                // Aplicando cor de fundo, forma e elevação
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {

                        // Exibe um ícone de voltar e facha o pop-up (false)
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

                            // Criando os botões em forma de coluna
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {

                                // Botão de adicionar categoria
                                Button(
                                    // Fecha o botão ao clicar e vai para a AddCategoryActivity
                                    onClick = {
                                        showAddPopUp = false
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
    onDeleteCategory: (String) -> Unit
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
            onDismiss = { selectedItem = null }    // fecha o diálogo ao dispensar
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
    // Estado para mostrar o diálogo de confirmação de exclusão da categoria
    var showDeleteConfirmation by remember { mutableStateOf(false) }


    val context = LocalContext.current

    // Diálogo de confirmação de exclusão que aparece se showDeleteConfirmation for true
    if (showDeleteConfirmation) {
        ConfirmDeleteCategoryDialog(
            categoryName = title,
            onConfirm = {
                // Só permite deletar se a categoria estiver vazia
                if (items.isEmpty()) {
                    onDeleteCategory(title)
                } else {
                    // Mensagem de aviso caso tenha senhas na categoria
                    Toast.makeText(
                        context,
                        "Não é possível excluir categoria com senhas",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onDismiss = { showDeleteConfirmation = false } // fecha diálogo ao dispensar
        )
    }

    Spacer(modifier = Modifier.height(16.dp)) // Espaço antes do card

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(5.dp),
        colors = CardDefaults.cardColors(
            // Cor do card muda se estiver expandido para dar destaque visual
            containerColor = if (expanded) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Column {
            // Cabeçalho da categoria que é clicável para expandir/recolher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Título da categoria em fonte grande e negrito
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row {
                    // Ícone para expandir/recolher a lista
                    IconButton(
                        onClick = { onExpandToggle() }
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                            else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                    if (title != "Sites Web") {
                        IconButton(
                            onClick = { showDeleteConfirmation = true },
                            // Desabilita exclusão para a categoria padrão "Sites Web"
                            enabled = title != "Sites Web",
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Excluir Categoria",
                                // Cor do ícone muda se estiver desabilitado para indicar visualmente
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Se estiver expandido, lista as senhas da categoria como texto clicável
            if (expanded) {
                items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemClick(item) } // ao clicar abre detalhe da senha
                            .padding(horizontal = 30.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.titulo,
                            fontSize = 23.sp,
                            modifier = Modifier.weight(1f) // ocupa o espaço restante
                        )
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Mais opções de senha"
                        )
                    }
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

    // Estado que controla se a senha está visível ou oculta
    var passwordVisible by remember { mutableStateOf(false) }

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

                    // Exibo o Login
                    Text("Login:", fontWeight = FontWeight.Bold)
                    StandardBoxPopUp {
                        Text(item.login)
                    }

                    // Exibo a senha
                    // Text("Senha:", fontWeight = FontWeight.Bold)
                    // StandardBoxPopUp {
                    // Text(item.senha)
                    // }

                    // Exibo a senha
                    Text("Senha:", fontWeight = FontWeight.Bold)
                    StandardBoxPopUp {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                // Se passwordVisible for true, mostra a senha real, senão mostra bullets (•)
                                text = if (passwordVisible) item.senha else "•".repeat(item.senha.length),
                                // Ocupa o espaço disponível na Row (exceto o espaço do ícone)
                                modifier = Modifier.weight(1f)
                            )
                            // Botão com ícone para alternar a visibilidade da senha
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                // OBSERVAÇÃO: Há um IconButton duplicado aqui (erro que deve ser removido)
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    // Ícone que muda conforme o estado da senha
                                    Icon(
                                        // Escolhe o ícone baseado no passwordVisible
                                        imageVector = if (passwordVisible) {
                                            Icons.Default.Visibility // Ícone de "olho cortado" (senha visível)
                                        } else {
                                            Icons.Default.VisibilityOff // Ícone de "olho aberto" (senha oculta)
                                        },
                                        // Texto descritivo para acessibilidade
                                        contentDescription = if (passwordVisible) {
                                            "Ocultar senha"  // Descrição quando senha está visível
                                        } else {
                                            "Mostrar senha"  // Descrição quando senha está oculta
                                        }
                                    )
                                }
                            }
                        }
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
                                    putExtra("PASSWORD_LOGIN", item.login)
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

                        // Botão cancelar (apenas fecha o diálogo)
                        Button(
                            onClick = onDismiss
                        ) {
                            Text("Cancelar")
                        }

                        // Botão remover (executa a exclusão no Firestore)
                        Button(
                            onClick = {
                                deletePasswordById(item.id)
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



@Composable
fun ConfirmDeleteCategoryDialog(
    categoryName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    // Diálogo de alerta para confirmar a exclusão de uma categoria
    AlertDialog(
        onDismissRequest = onDismiss, // Ação ao clicar fora do diálogo ou botão voltar
        title = { Text("Confirmar exclusão") }, // Título do diálogo
        text = { Text("Tem certeza que deseja excluir a categoria '$categoryName'?") }, // Mensagem de confirmação com o nome da categoria
        confirmButton = {
            // Botão para confirmar a exclusão
            TextButton(
                onClick = {
                    onConfirm()  // Executa ação de confirmação (excluir categoria)
                    onDismiss()  // Fecha o diálogo após confirmar
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error // Cor vermelha para destacar ação perigosa
                )
            ) {
                Text("Excluir") // Texto do botão
            }
        },
        dismissButton = {
            // Botão para cancelar e fechar o diálogo
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

fun resendVerificationEmail(context: Context) {
    val user = Firebase.auth.currentUser
    if (user != null && !user.isEmailVerified) {
        user.sendEmailVerification()
    }
}



fun checkEmailVerified(
    uid: String,
    onResult: (Boolean) -> Unit
) {
    //  faz a consulta no Firestore
    Firebase.firestore
        .collection("users")
        .document(uid)
        .get()
        .addOnSuccessListener { doc ->
            val verified = doc?.getBoolean("emailVerified") == true
            onResult(verified) // callback passa true ou false
        }
        .addOnFailureListener {
            onResult(false) // em caso de erro
        }
}



@Composable
fun ConfirmPasswordDialog(
    onDismiss: () -> Unit,
    onConfirmed: () -> Unit
) {
    val context = LocalContext.current
    var senha by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Obtém o email do usuário logado
    val email = Firebase.auth.currentUser?.email ?: "Email não disponível"

    // Caixa de diálogo com campos personalizados
    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(
                onClick = {
                    val uid = Firebase.auth.currentUser?.uid
                    if (uid != null) {
                        checkEmailVerified(uid) { isVerified -> // passo uma lambda para onResult de checkEmailVerified
                            if (isVerified) { reauthenticateUser(senha, context, onSuccess = {
                                onConfirmed() // <- ativa o popup do QR Code
                                onDismiss()  // <- fecha o popup atual
                                    }, onFailure = {
                                        senha = ""// limpa o campo de senha
                                    })
                            } else {

                                resendVerificationEmail(context)

                                Toast
                                    .makeText(
                                        context, "E-mail não confirmado. Verifique sua caixa de entrada.",
                                        Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            ) {

                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancelar")
            }
        },
        title = { Text("Confirmação de senha") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Campo de exibição do email (desabilitado apenas para mostrar o valor)
                CustomOutlinedTextField(
                    value = email,
                    onValueChange = {},
                    label = "Email",
                    enabled = false // campo somente leitura
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo de entrada de senha com ícone de visibilidade
                CustomOutlinedTextField(
                    value = senha,
                    onValueChange = { senha = it },
                    label = "Senha",
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar senha" else "Mostrar senha"
                            )
                        }
                    }
                )
            }
        }
    )
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

fun deletePasswordById(senhaId: String) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    if (user != null) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(user.uid)
            .collection("senhas")
            .document(senhaId)
            .delete()
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
