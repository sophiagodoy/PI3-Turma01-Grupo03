// Definição do pacote do aplicativo
package br.com.ibm.superid

// Importações necessárias para Android, Jetpack Compose, Firebase entre outras
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ibm.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation

/**
 * Esta classe representa a tela de Cadastro (SignUp).
 * Ela herda de ComponentActivity, que é uma tela tradicional do Android.
 */
class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Define o conteúdo da tela usando Jetpack Compose
        setContent {
            // Aplica o tema visual da aplicação
            SuperIDTheme {
                // Recupera o contexto atual
                val context = LocalContext.current

                // Mostra a tela com a seta de voltar e o formulário de cadastro.
                // Quando o usuário clica na seta de voltar, ele retorna para a tela anterior.
                // Chamamos o composable TopBarNavigationExample, passando a função lambda que faz o Intent
                TopBarNavigationExample(
                    navigateBack = {
                        // Ao clicar na seta, volta para a tela anterior (AccessOptionActivity)
                        val intent = Intent(context, AccessOptionActivity::class.java)
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

/**
 * Este Composable representa a estrutura visual da tela de cadastro:
 * - Uma barra no topo (TopAppBar)
 * - O formulário de cadastro abaixo
 *
 * A função navigateBack é chamada quando o usuário clica na seta de voltar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarNavigationExample(
    navigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    // Título que aparece no centro da barra superior
                    Text("Cadastro")
                },
                navigationIcon = {
                    // Ícone de voltar (seta), que chama a função navigateBack ao ser clicado
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar" // Descrição para leitores de tela
                        )
                    }
                },
            )
        }
    ) { innerPadding ->
        // Aqui mostramos o formulário de cadastro, com padding ajustado pela barra superior
        SignUp(modifier = Modifier.padding(innerPadding))
    }
}


/**
 * Essa função cria uma nova conta no Firebase Authentication.
 * Se a conta for criada com sucesso, ela salva os dados do usuário no banco de dados.
 */
fun saveUserToAuth(email: String, password: String, name: String) {
    // Obtemos a instância do Firebase Auth
    val auth = Firebase.auth

    // Cria um novo usuário com e-mail e senha
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            // Se a criação da conta for bem-sucedida, obtemos o usuário e seu UID
            if (task.isSuccessful) {
                val user = task.result.user
                val uid = user!!.uid
                Log.i("AUTH", "Conta criada com sucesso. UID: $uid")

                // Chama a função para salvar os dados do usuário no Firestore
                saveUserToFirestore(name, email, uid)
            } else {
                // Em caso de falha, registra o erro
                Log.i("AUTH", "Falha ao criar conta.", task.exception)
            }
        }
}

/**
 * Esta função salva os dados do usuário (nome, e-mail e UID) no banco Firestore.
 * Os dados são gravados na coleção chamada "users".
 */
fun saveUserToFirestore(name: String, email: String, uid: String) {
    // Obtemos a instância do Firestore
    val db = Firebase.firestore

    // Prepara os dados do usuário em um HashMap
    val userData = hashMapOf(
        "uid" to uid,
        "name" to name,
        "email" to email
    )

    // Adiciona os dados na coleção "users"
    db.collection("users").add(userData)
        .addOnSuccessListener { documentReference ->
            Log.i("Firestore", "Dados do usuário salvos com sucesso. ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Erro ao adicionar documento", e)
        }
}

/**
 * Tela de formulário onde o usuário preenche os dados de cadastro.
 * Campos: Nome, E-mail, Senha, Confirmar Senha
 */
@Composable
fun SignUp(modifier: Modifier = Modifier) {
    // Cria variável para poder trocar de tela
    val context = LocalContext.current

    // Variáveis que guardam o valor digitado nos campos do formulário
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Layout em coluna, centralizado na tela
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título da tela
        Text(
            text = "Cadastro",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        // Campo de texto para o nome
        OutlinedTextField(
            modifier = Modifier.padding(10.dp),
            value = name,
            onValueChange = { name = it },
            label = { Text(text = "Nome") }
        )

        // Campo de texto para o e-mail
        OutlinedTextField(
            modifier = Modifier.padding(10.dp),
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "Email") }
        )

        // Campo de texto para a senha
        OutlinedTextField(
            modifier = Modifier.padding(10.dp),
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Senha") },

            // Esconde a senha
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        // Campo de texto para confirmação da senha
        OutlinedTextField(
            modifier = Modifier.padding(10.dp),
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text(text = "Confirmar Senha") },

            // Esconde a senha
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        // Botão para enviar o formulário
        Button(
            onClick = {
                // Só envia se a senha e a confirmação forem iguais
                if (password == confirmPassword) {
                    // Cria a conta no Firebase e redireciona para a tela de login
                    saveUserToAuth(email, password, name)
                    val intent = Intent(context, SignInActivity::class.java)
                    context.startActivity(intent)
                } else {
                    Log.i("SIGN UP", "As senhas não coincidem.")
                }
            }
        ) {
            Text(text = "Cadastrar")
        }
    }
}
