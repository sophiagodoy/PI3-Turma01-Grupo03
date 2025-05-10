// TELA PARA O USUÁRIO ADICIONAR UMA NOVA SENHA

package br.com.ibm.superid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import br.com.ibm.superid.ui.theme.core.util.CustomOutlinedTextField
import br.com.ibm.superid.ui.theme.core.util.SuperIDHeader
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
fun addNewPassword(
    context: Context,
    senha: String,
    categoria: String,
    descricao: String,
    titulo: String
) {
    // Baseado na documentação: https://firebase.google.com/docs/auth/android/manage-users?hl=pt-br#get_the_currently_signed-in_user
    // Identifica o usuário atual conectado
    val user = Firebase.auth.currentUser

    // Validando campos obrigatórios
    if (senha.isBlank() || categoria.isBlank() || descricao.isBlank() || titulo.isBlank()) {
        Toast.makeText(context, "Preencha todos os campos!", Toast.LENGTH_LONG).show()
        return
    }

    try {
        // Obtendo a instância do banco de dados Firestore
        val db = Firebase.firestore

        // Criptografa a senha e obtém o IV
        val (encryptpassword, iv) = encryptpassword(senha)
        val accessToken = createacesstoken()

        // Mapa com informações da nova senha
        val dadosNovaSenha = hashMapOf(
            "titulo"      to titulo,
            "senha"       to encryptpassword,
            "categoria"   to categoria,
            "descricao"   to descricao,
            "accessToken" to accessToken,
            "iv"          to iv
        )

        // Adiciona o documento e usa addOnCompleteListener
        db.collection("users")
            .document(user!!.uid)
            .collection("senhas")
            .add(dadosNovaSenha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Se salvou com sucesso, obtém a referência do documento
                    val documentReference = task.result
                    // Atualiza o próprio documento adicionando o campo "id"
                    documentReference.update("id", documentReference.id)
                    Toast.makeText(context, "Senha salva com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    // Em caso de erro, exibe a mensagem
                    val e = task.exception
                    Toast.makeText(context, "Erro ao salvar senha: ${e?.message}", Toast.LENGTH_LONG).show()
                }
            }
    } catch (e: Exception) {
        // Trata possíveis erros de criptografia
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

// Lista mutável que guarda as categorias lidas no Firestore do tipo String
val categoriasUsuario = mutableStateListOf<String>()

// Função que lê as categorias do Firestore
fun fetchCategoriasUsuario(context: Context) {
    // Pega a instância de autenticação do Firebase
    val auth = Firebase.auth

    // Recupera o usuário atualmente autenticado
    val currentUser = auth.currentUser

    // Se não houver usuário logado, interrompe a função
    if (currentUser == null) return

    // A partir daqui sabemos que currentUser não é null, então pegamos o UID
    val uid = currentUser.uid

    // Limpa a lista antes de buscar novas categorias
    categoriasUsuario.clear()

    // Acessa o banco de dados Firestore
    Firebase.firestore
        .collection("users")
        .document(uid)
        .collection("categorias")
        .get()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.documents?.forEach { doc ->
                    doc.getString("nome")?.let { categoriasUsuario.add(it) }
                }
            } else {
                Toast.makeText(context, "Erro ao ler categorias: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
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

    // Variável que controla se o meu DropdownMenu está aberto ou fechado
    var expanded by remember { mutableStateOf(false) }


    // Layout em coluna que ocupa toda a tela e aplica padding de 16dp
    // Seta que volta para AccessOptionActivity
    // Baseado em: https://developer.android.com/develop/ui/compose/components/app-bars?hl=pt-br#top-app-bar
    // Baseado em: https://alexzh.com/visual-guide-to-topappbar-variants-in-jetpack-compose/?utm_source=chatgpt.com

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Cabeçalho visual personalizado
        SuperIDHeader()

        // Botão de voltar
        IconButton(
            onClick = {
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar"
            )
        }

        // Layout em coluna que ocupa toda a tela e aplica padding de 16dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 50.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
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
            CustomOutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = "Título"
            )

            // Campo de texto para digitar a nova senha
            CustomOutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                label = "Senha",
                // Esconde os caracteres da senha
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )

            Box(
                modifier = Modifier
            ) {
                // 1) Seu campo customizado, sem precisar mudar nada nele:
                CustomOutlinedTextField(
                    value = categoria,
                    onValueChange = { /* não edita */ },
                    label = "Categoria"
                )

                // 2) Ícone “drop-down” alinhado à direita, por cima do campo:
                IconButton(
                    onClick = {
                        fetchCategoriasUsuario(context)
                        expanded = true
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)      // encaixa bem com o padding interno do campo
                ) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Escolher")
                }

                // 3) O DropdownMenu logo abaixo, igual antes:
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },

                    // TODO: ARRUMAR TAMANHO DO DROP DOWN AQUI
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                ) {
                    if (categoriasUsuario.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Nenhuma categoria") },
                            onClick = { expanded = false }
                        )
                    } else {
                        categoriasUsuario.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    categoria = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Campo de texto para digitar a descrição da noa senha
            CustomOutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = "Descrição"
            )


            // Espaço de 24dp antes do botão
            Spacer(Modifier.height(24.dp))

            // Botão que quando clicado salva a nova senha no banco Firestore
            Button(
                onClick = {
                    addNewPassword(context, senha, categoria, descricao, titulo)
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .height(60.dp)
                    .width(150.dp)
                    .align(Alignment.CenterHorizontally),
            ) {
                Text("Salvar")
            }

        }
    }
}

