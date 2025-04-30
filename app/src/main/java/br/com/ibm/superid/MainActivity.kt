// Definição do pacote
// TODO: Decidir se vai remover a categoria pelo X ou se, removendo todas as senhas da categoria, ela e removida tambem
package br.com.ibm.superid

// Importações necessárias
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import br.com.ibm.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

// Classe principal que define a Activity inicial do aplicativo
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ativa o modo de bordas estendidas
        enableEdgeToEdge()
        // Define o conteúdo da tela com o tema do app
        setContent {
            SuperIDTheme {
                PreviewMainScreen()
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
    var passwords by remember { mutableStateOf<List<SenhaItem>>(emptyList()) } // Lista de senhas
    var showAddPopUp by remember { mutableStateOf(false) } // Exibe o popup de adicionar senha
    var showQRCodePopUp by remember { mutableStateOf(false) } // Exibe o popup do QR Code

    // Carrega as senhas do Firestore assim que a tela iniciar
    LaunchedEffect(Unit) {
        // Obtém o usuário atualmente autenticado no Firebase
        val user = Firebase.auth.currentUser

        // Verifica se há um usuário logado
        if (user != null) {
            // Acessa a subcoleção "senhas" dentro do documento do usuário logado
            Firebase.firestore.collection("users")
                .document(user.uid)
                .collection("senhas")
                .get() // Realiza a leitura de todos os documentos dessa subcoleção
                .addOnSuccessListener { result ->
                    // Lista temporária para armazenar as senhas descriptografadas
                    val tempList = mutableListOf<SenhaItem>()

                    // percorre cada documento retornado
                    for (doc in result) {
                        try {
                            // Descriptografa a senha utilizando os dados armazenados (senha e IV)
                            val decrypted = decryptPassword(
                                encrypted = doc.getString("senha") ?: "",
                                ivBase64 = doc.getString("iv") ?: ""
                            )

                            // Cria um objeto SenhaItem com os dados do documento
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
                            // Caso ocorra erro ao descriptografar, loga o erro no console
                            Log.e("Decrypt", "Erro ao descriptografar: ${e.message}")
                        }
                    }

                    // Atualiza o estado da lista de senhas com os itens carregados
                    passwords = tempList
                }
                .addOnFailureListener {
                    // Caso ocorra erro ao acessar o Firestore, loga o erro no console
                    Log.e("Firestore", "Erro ao carregar senhas: ${it.message}")
                }
        }
    }

    // Agrupa as senhas por categoria
    val categorias = passwords.groupBy { it.categoria }

    // Layout principal da tela
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Título e botão de adicionar senha
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SuperID", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                FloatingActionButton(
                    onClick = { showAddPopUp = true },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add,
                        contentDescription = "Adicionar")
                }
            }

            // Exibe os grupos por categoria
            categorias.forEach { (categoria, itens) ->
                Spacer(modifier = Modifier.height(16.dp))
                Categories(title = categoria, items = itens)
            }

            // Popup de confirmação para adicionar senha
            if (showAddPopUp) {
                AlertDialog(
                    onDismissRequest = { showAddPopUp = false },
                    confirmButton = {
                        TextButton(onClick = {
                            showAddPopUp = false
                            context.startActivity(Intent(context, AddPasswordActivity::class.java))
                        }) {
                            Text("Adicionar senha")
                        }
                        TextButton(onClick = {
                            showAddPopUp = false
                            context.startActivity(Intent(context, AddCategoryActivity::class.java))
                        }) {
                            Text("Adicionar Categoria")
                        }
                    }
                )
            }
        }

        // Botão flutuante para abrir o leitor de QR Code
        FloatingActionButton(
            onClick = { showQRCodePopUp = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            //comentei pq ta dando erro
            /*Image(
                painter = painterResource(id = R.drawable.qrcode),
                contentDescription = "QR Code",
                modifier = Modifier.size(24.dp)
           )*/
        }

        // Popup para o QR Code
        if (showQRCodePopUp) {
            AlertDialog(
                onDismissRequest = { showQRCodePopUp = false },
                confirmButton = {
                    TextButton(onClick = {
                        showQRCodePopUp = false
                    }) {
                        Text("Leitura do QRCOde")
                    }
                }
            )
        }
    }
}

// Composable que exibe cada grupo de senhas por categoria
@Composable
fun Categories(title: String, items: List<SenhaItem>) {
    var expanded by remember { mutableStateOf(false) } // Controla expansão da categoria
    var selectedItem by remember { mutableStateOf<SenhaItem?>(null) } // Item selecionado
    val context = LocalContext.current
    var showRemovePopUp by remember { mutableStateOf(false) } // Popup de remoção

    // Card que representa cada categoria
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(5.dp)
    ) {
        Column {
            // Cabeçalho da categoria (expansível)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            // Exibe os itens da categoria se expandido
            if (expanded) {
                items.forEach { item ->
                    Text(
                        text = item.titulo,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 30.dp, vertical = 8.dp)
                            .clickable { selectedItem = item }
                    )
                }
            }
        }
    }

    // Verifica se algum item foi selecionado na lista
    selectedItem?.let { item ->
        // Exibe um AlertDialog com os detalhes da senha selecionada
        AlertDialog(
            onDismissRequest = { selectedItem = null }, // Fecha o popup ao clicar fora
            confirmButton = {}, // Sem botão de confirmação principal
            title = {
                Column {
                    // Título da senha (em destaque)
                    Text(text = item.titulo, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    // Exibe a senha descriptografada
                    Text(text = "Senha: ${item.senha}")
                    Spacer(modifier = Modifier.height(4.dp))
                    // Exibe a descrição da senha
                    Text(text = "Descrição: ${item.descricao}")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Botão para ir para a tela de alteração de senha


                        TextButton(onClick = {
                            val intent = Intent(context, ChangePasswordActivity::class.java).apply{
                            putExtra("PASSWORD_ID", item.id)
                            putExtra("PASSWORD_TITLE", item.titulo)
                            putExtra("PASSWORD_VALUE", item.senha)
                            putExtra("PASSWORD_DESCRIPTION", item.descricao)
                            putExtra("PASSWORD_CATEGORY", item.categoria)

                            // Link da documentação que mostra como enviar valores de uma Activity (tela) para outra:
                            // https://developer.android.com/guide/components/activities/parcelables-and-bundles?hl=pt-br#kotlin
                        }
                            context.startActivity(intent)
                        }) {
                            Text("Alterar")
                        }
                        // Botão que ativa a exibição do popup de confirmação de remoção
                        TextButton(onClick = { showRemovePopUp = true }) {
                            Text("Remover")
                        }
                    }

                    // Verifica se o popup de confirmação de remoção deve ser exibido
                    if (showRemovePopUp) {
                        AlertDialog(
                            onDismissRequest = { showRemovePopUp = false }, // Fecha o popup se clicar fora
                            // Botões de ação no popup de confirmação
                            confirmButton = {
                                Row {
                                    // Botão "Cancelar"
                                    TextButton(onClick = {
                                        showRemovePopUp = false
                                    }) {
                                        Text("Cancelar")
                                    }
                                    // Botão "Remover"
                                    TextButton(onClick = {
                                        showRemovePopUp = false
                                        val user = Firebase.auth.currentUser
                                        user?.uid?.let { uid ->
                                            // Acessa o documento da senha e remove do Firestore
                                            Firebase.firestore
                                                .collection("users")
                                                .document(uid)
                                                .collection("senhas")
                                                .document(item.id)
                                                .delete()
                                                .addOnSuccessListener {
                                                    // Exibe confirmação e fecha o popup
                                                    Toast.makeText(
                                                        context,
                                                        "Senha excluída com sucesso!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    selectedItem = null
                                                    // TODO: Atualizar a lista após exclusão
                                                }
                                                .addOnFailureListener { e ->
                                                    // Exibe mensagem de erro caso a exclusão falhe
                                                    Toast.makeText(
                                                        context,
                                                        "Erro ao excluir: ${e.message}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                        }
                                    }) {
                                        Text("Remover")
                                    }
                                }
                            },
                            // Título do popup de confirmação
                            title = {
                                Text("Tem certeza que deseja remover essa senha?")
                            }
                        )
                    }
                }
            }
        )
    }

}

// Preview da tela principal
@Preview
@Composable
fun PreviewMainScreen() {
    MainScreen()
}
