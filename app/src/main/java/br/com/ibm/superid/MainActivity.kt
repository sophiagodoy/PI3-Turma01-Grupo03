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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        SuperIDHeader()

        // Seta de voltar para a AcessOptionActivity (saindo do aplicativo)
        IconButton(
            onClick = { showExitDialog = true }, // Altera a variável de estado para ativo
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 10.dp, top = 95.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Sair do aplicativo",
                modifier = Modifier.size(35.dp)
            )
        }

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
            onClick = { showAddPopUp = true }, // showAddPopUp é ativada
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

        // Botão flutuante do QR Code
        FloatingActionButton(
            onClick = { showQRCodePopUp = true }, // showQRCodePopUp é ativada
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

        /*TODO: Mudar o POP-UP para abir a camera*/

        // Popup de leitura do QR Code
        if (showQRCodePopUp) {
            AlertDialog(
                onDismissRequest = { showQRCodePopUp = false },
                title = {
                    // Faixa verde com o botão de voltar
                    BackButtonBar(onBackClick = {
                        showAddPopUp = false
                    })
                },
                confirmButton = {
                    TextButton(onClick = {
                        showQRCodePopUp = false
                    }) {
                        Text("Esse pop-up nao vai existir, ele abre a camera direto.")
                    }
                }
            )
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
