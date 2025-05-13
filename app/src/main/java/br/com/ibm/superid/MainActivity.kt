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
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
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

// Função que descriptografa a senha
@OptIn(ExperimentalEncodingApi::class)
fun decryptPassword(encrypted: String, ivBase64: String, key: String = "ProjetoIntegrador3Semestre062025"): String {
    val keyBytes = key.toByteArray(Charsets.UTF_8).copyOf(32)
    val secretKey = SecretKeySpec(keyBytes, "AES")
    val iv = Base64.decode(ivBase64)
    val ivSpec = IvParameterSpec(iv)
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
    val decryptedBytes = cipher.doFinal(Base64.decode(encrypted))
    return decryptedBytes.toString(Charsets.UTF_8)
}

// função que representa a tela principal do app
@Composable
fun MainScreen() {
    val context = LocalContext.current

    // Declarando variáveis
    var passwords by remember { mutableStateOf<List<SenhaItem>>(emptyList()) }
    var showAddPopUp by remember { mutableStateOf(false) }
    var showQRCodePopUp by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) } // Controla o estado do diálogo de saída

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

    val categorias = passwords.groupBy { it.categoria }

    Box(modifier = Modifier
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

        Column(modifier = Modifier.padding(16.dp)) {
            Spacer(modifier = Modifier.height(130.dp))
            categorias.forEach { (categoria, itens) ->
                Spacer(modifier = Modifier.height(16.dp))
                Categories(title = categoria, items = itens)
            }
        }

        // Botão flutuante de adicionar
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

        // Botão flutuante do QR Code
        FloatingActionButton(
            onClick = { showQRCodePopUp = true },
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Image(
                painter = painterResource(id = R.drawable.qrcode),
                contentDescription = "QR Code",
                modifier = Modifier.size(60.dp)
            )
        }

        // Pop-up de adicionar senha e categoria (showAddPopUp = true)
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

                        // Sua faixa verde com botão de voltar
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

@Composable
fun Categories(title: String, items: List<SenhaItem>) {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<SenhaItem?>(null) }

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
                Text(text = title, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { onExpandToggle() }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

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

@Composable
fun PasswordDetailDialog(item: SenhaItem, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var showRemovePopUp by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                BackButtonBar(onBackClick = onDismiss)

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = item.titulo,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text("Senha:", fontWeight = FontWeight.Bold)
                    StandardBoxPopUp {
                        Text(item.senha)
                    }

                    Text("Descrição:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    StandardBoxPopUp {
                        Text(item.descricao)
                    }

                    Text("Categoria:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    StandardBoxPopUp {
                        Text(item.categoria)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(onClick = {
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
                            )) {
                            Text("Alterar")
                        }

                        Button(onClick = { showRemovePopUp = true },
                                colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                                )){
                            Text("Remover")
                        }
                    }

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
                BackButtonBar(onBackClick = onDismiss)

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Certeza que deseja remover a senha?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = onDismiss) {
                            Text("Cancelar")
                        }

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

