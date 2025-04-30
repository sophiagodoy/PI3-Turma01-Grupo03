// TELA PARA O USUÁRIO ALTERAR UMA SENHA

// Definição do pacote aplicativo
package br.com.ibm.superid

// Importações necessárias
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ibm.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore

// Declarando a Activity (ChangePasswordActivity)
class ChangePasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Extrai os dados que enviamos
        val passwordId       = intent.getStringExtra("PASSWORD_ID")       ?: ""
        val initialSenha     = intent.getStringExtra("PASSWORD_VALUE")    ?: ""
        val initialCategoria = intent.getStringExtra("PASSWORD_CATEGORY") ?: ""
        val initialDescricao = intent.getStringExtra("PASSWORD_DESCRIPTION") ?: ""
        val initialTitulo    = intent.getStringExtra("PASSWORD_TITLE")    ?: ""

        setContent {
            SuperIDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Chama a função composable ChangePassword e aplica o padding interno do Scaffold
                    ChangePassword(
                        modifier = Modifier.padding(innerPadding),
                        // Passa os valores iniciais
                        passwordId       = passwordId,
                        initialTitulo    = initialTitulo,
                        initialSenha     = initialSenha,
                        initialDescricao = initialDescricao,
                        initialCategoria = initialCategoria,
                    )
                }
            }
        }
    }
}



// Função Composable que apresenta o formulário de adicionar senha
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePassword(
    passwordId: String,
    initialTitulo: String,
    initialSenha: String,
    initialCategoria: String,
    initialDescricao: String,
    modifier: Modifier = Modifier) {

    // Cria variável para poder trocar de tela
    val context = LocalContext.current

    // Variáveis que guardam o valor digitado nos campos do formulário
    var senha by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }

    // Seta que volta para (COLOCAR O NOME DA TELA Q O ARTHUR CRIOU)
    Scaffold(
        // Define que a tela terá uma barra superior, onde vamos colocar o TopAppBar
        topBar = {
            // Começa a criação da barra de app superior (TopAppBar)
            TopAppBar(
                title = { }, // Indica que não terá texto no meio da barra
                // Define o ícone de navegação da TopAppBar
                navigationIcon = {
                    //  Cria um botão que será clicável, o botão envolverá o ícone de voltar
                    IconButton(
                        onClick = {

                            val intent = Intent(context, MainActivity::class.java) //
                            context.startActivity(intent)

                        }
                    ) {
                        // Cria o ícone da seta de voltar
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar" // Usado para acessibilidade (leitores de tela vão anunciar "Voltar" para deficientes visuais)
                        )
                    }
                }
            )
        }
    ) { innerPadding -> // Fecha o Scaffold e começa a definir o conteúdo principal da tela

        // Layout em coluna que ocupa toda a tela e aplica padding de 16dp
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Define o título da tela em negrito e tamanho 30sp
            Text(
                text = "ALTERAR: ${initialTitulo.uppercase()}",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            // Espaço de 24dp abaixo do título
            Spacer(Modifier.height(24.dp))

            // Campo de texto para digitar a nova senha
            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                label = { Text("Senha") },
                // Esconde os caracteres da senha
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Campo de texto para escolher a categoria da nova senha
            OutlinedTextField(
                value = categoria,
                onValueChange = { categoria = it },
                label = { Text("Categoria") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            // Campo de texto para digitar a descrição da nova senha
            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            // Espaço de 24dp antes do botão
            Spacer(Modifier.height(24.dp))

            // Botão que quando clicado salva a nova senha no banco Firestore
            Button(
                onClick = {
                    // Chama nossa função de atualização
                    updatePassword(
                        context      = context,
                        documentId   = passwordId,
                        newPassword  = senha,
                        newCategory  = categoria,
                        newDesc      = descricao
                    )

                    if(senha.isNotBlank())// volta para a Main
                    context.startActivity(Intent(context, MainActivity::class.java))
                },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(48.dp)
            ) {
                // Define o texto que está dentro do botão
                Text("Salvar")
            }
        }
    }
}

// Baseado na documentação: https://www.baeldung.com/kotlin/advanced-encryption-standard
// Criptografa uma senha usando AES, tendo como a chave "ProjetoIntegrador3Semestre062025"
@OptIn(ExperimentalEncodingApi::class)
fun encryptPassword(password: String, encryptionKey: String = "ProjetoIntegrador3Semestre062025"): Pair<String, String> {
    // Converte a chave para bytes UTF-8 e ajusta para 32 bytes (256 bits)
    val keyBytes = encryptionKey.toByteArray(Charsets.UTF_8).copyOf(32)
    // Cria a especificação da chave secreta para o algoritmo AES
    val secretKey = SecretKeySpec(keyBytes, "AES")
    // Gera 16 bytes aleatórios (tamanho pedido pela AES)
    val iv = ByteArray(16)
    // Preenche com bytes criptografados
    SecureRandom().nextBytes(iv)
    // Cria o objeto IV
    val ivSpec = IvParameterSpec(iv)
    // Configuração da criptografia, aonde obtem a instancia do cipher
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    // Inicializa para a criptografia com a chave e IV
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
    // Converte a senha para bytes UTF-8 e criptografa
    val encryptedBytes = cipher.doFinal(password.toByteArray(Charsets.UTF_8))

    // Utilizado "Return Pair", para que consiga dar return em dois elementos ncessarios para descriptografar
    // Retorna Pair com a senha criptografada e IV, ambos em Base64
    return Pair(
        Base64.encode(encryptedBytes),
        Base64.encode(iv)
    )
}


fun updatePassword(
    context: Context,
    documentId: String,
    newPassword: String,
    newCategory: String,
    newDesc: String
) {
    val user = Firebase.auth.currentUser
    if (user == null) {
        Toast.makeText(context, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
        return
    }
    if (newPassword.isBlank() || newCategory.isBlank()) {
        Toast.makeText(context, "Senha e Categoria não podem estar em branco!", Toast.LENGTH_SHORT).show()
        return
    }

    // Encripta a nova senha
    val (encrypted, iv) = encryptPassword(newPassword)

    // Prepara o map pra atualizar
    val updates = mapOf(
        "senha"     to encrypted,
        "iv"        to iv,
        "categoria" to newCategory,
        "descricao" to newDesc
    )

    // Executa o update no Firestore
    Firebase.firestore
        .collection("users")
        .document(user.uid)
        .collection("senhas")
        .document(documentId)
        .update(updates)
        .addOnSuccessListener {
            Toast.makeText(context, "Senha atualizada com sucesso!", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Erro ao atualizar: ${e.message}", Toast.LENGTH_LONG).show()
        }
}