// TELA PARA O USUÁRIO ADICIONAR UMA NOVA SENHA

// Definição do pacote aplicativo
package br.com.ibm.superid

// Importações necessárias
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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


// Declarando a Activity (AddPasswordActivity)
class AddPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme{
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Chama a função composable AddPassword e aplica o padding interno do Scaffold
                    AddPassword(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// Função para adicionar uma nova senha no Firestore
@OptIn(ExperimentalEncodingApi::class)
fun addNewPassword(context: Context, senha: String, categoria: String, descricao: String, titulo: String) {
    // Baseado na documentação: https://firebase.google.com/docs/auth/android/manage-users?hl=pt-br#get_the_currently_signed-in_user
    // Identifica o usuário atual conectado
    val user = Firebase.auth.currentUser

    // Validando campos obrigatórios
    if (senha.isBlank() || categoria.isBlank() || descricao.isBlank() || titulo.isBlank()) {
        Toast.makeText(context, "Preencha todos os campos!", Toast.LENGTH_LONG).show()
        return
    }

    // Testa a criptografia, verificando se está funcionando
    try {
        // Obtendo a instância do banco de dados Firestore
        val db = Firebase.firestore
        // Chama EncryptPassword e obtém tanto a senha criptografada quanto o IV (Vetor de Inicialização)
        // Utilizado para criptografar a senha quando o IV
        val (encryptpassword, iv) = encryptpassword(senha)
        val accessToken = createacesstoken()

        // Criando uma mapa mutável (hashMap) com informações da nova senha
        val dadosNovaSenha = hashMapOf(
            "titulo" to titulo,
            "senha" to encryptpassword,
            "categoria" to categoria,
            "descricao" to descricao,
            "accessToken" to accessToken,
            "iv" to iv // Vetor de Inicialização
        )

        // Gravando os dados no banco de dados Firestore
        // Acessa a coleção chamada users do banco de dados
        db.collection("users")
            // Dentro da coleção "users", aponta para o documento cujo ID é o uid do usuário autenticado
            .document(user!!.uid)
            // Cria uma subcoleção dentro do documento chamada "senhas"
            .collection("senhas")
            // Adiciona um novo documento na subcoleção "senhas" usados os dados de "dadosNovaSenha"
            .add(dadosNovaSenha)
            // Caso a senha seja adicionada com sucesso, chama um Toast dando o aviso
            .addOnSuccessListener { documentReference ->
                // Captura o ID gerado automaticamente
                val documentId = documentReference.id
                // Atualiza o mesmo documento adicionando o campo "id"
                documentReference.update("id", documentId)
                Toast.makeText(context, "Senha salva com sucesso!", Toast.LENGTH_SHORT).show()
            }
            // Caso a senha de erro ao adicionar no banco de dados, chama um Toast dando o aviso
            .addOnFailureListener { e ->
                Toast.makeText(context, "Erro ao salvar senha: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
    // Tratar dos possiveis erros de criptografia
    catch (e: Exception) {
        Toast.makeText(context, "Erro de criptografia: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

// Baseado na documentação: https://www.baeldung.com/kotlin/advanced-encryption-standard
// Gera um token de acesso aleatório para a senha
@OptIn(ExperimentalEncodingApi::class)
fun createacesstoken(): String {
    // Gerador da criptografia
    val random = SecureRandom()
    // Criaçao de uma array para que tenha um tamanho adequado de 256 bytes
    val bytes = ByteArray(32) // 32 bytes = 256 bits
    // Gerador de bytes aleatórios
    random.nextBytes(bytes)
    // Conversão dos bytes binários para string ASCII
    return Base64.encode(bytes)
}

// Baseado na documentação: https://www.baeldung.com/kotlin/advanced-encryption-standard
// Criptografa uma senha usando AES, tendo como a chave "ProjetoIntegrador3Semestre062025"
@OptIn(ExperimentalEncodingApi::class)
fun encryptpassword(password: String, encryptionKey: String = "ProjetoIntegrador3Semestre062025"): Pair<String, String> {
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

// Função Composable que apresenta o formulário de adicionar senha
@Preview(showBackground = true)
@Composable
fun AddPassword(modifier: Modifier = Modifier) {

    // Cria variável para poder trocar de tela
    val context = LocalContext.current

    // Variáveis que guardam o valor digitado nos campos do formulário
    var senha by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var titulo by remember { mutableStateOf("") }

    // Layout em coluna que ocupa toda a tela e aplica padding de 16dp
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Define o título da tela em negrito e tamanho 30sp
        Text(
            text = "ADICIONAR SENHA",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        // Espaço de 24dp abaixo do título
        Spacer(Modifier.height(24.dp))

        // Campo de texto para escolher a categoria da nova senha
        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        // Campo de texto para digitar a nova senha
        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Senha") },
            // Esconde os caracteres da senha
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        // Campo de texto para escolher a categoria da nova senha
        OutlinedTextField(
            value = categoria,
            onValueChange = { categoria = it },
            label = { Text("Categoria") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        // Campo de texto para digitar a descrição da noa senha
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
                // Chama a função que valida e grava a nova senha no no Firestore
                addNewPassword(context, senha, categoria, descricao, titulo)
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