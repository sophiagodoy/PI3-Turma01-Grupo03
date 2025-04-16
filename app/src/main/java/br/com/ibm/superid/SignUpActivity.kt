// Definição do pacote do aplicativo
package br.com.ibm.superid

// Importações necessárias para Android, Jetpack Compose, Firebase entre outras
import android.content.Context
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
import androidx.compose.material3.Button
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
// SignUpActivity: Activity responsável pela tela de cadastro de usuário
class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Habilita o uso total da tela, inclusive áreas atrás das barras do sistema
        enableEdgeToEdge()
        // Define o conteúdo da Activity utilizando Jetpack Compose
        setContent {
            // Aplica o tema customizado da aplicação
            SuperIDTheme {
                // Scaffold provê a estrutura básica da tela, garantindo consistência de layout
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Chama a função composable SignUp e aplica o padding interno do Scaffold
                    SignUp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

/**
 * Essa função cria uma nova conta no Firebase Authentication.
 * Se a conta for criada com sucesso, ela salva os dados do usuário no banco de dados.
 */
fun saveUserToAuth(email: String, password: String, name: String, context: Context) {
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
                saveUserToFirestore(name, email, uid, context)
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
fun saveUserToFirestore(name: String, email: String, uid: String, context: Context) {
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

            // Vai para a tela de Login
            val intent = Intent(context, SignInActivity::class.java)
            context.startActivity(intent)
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

            // Esconder a senha que está sendo digitada pelo usuário
            // Implementado com base na seção "Texto e tipografia > Processar entrada do usuário" da documentação oficial do Jetpack Compose
            // Fonte: https://developer.android.com/develop/ui/compose/text/user-input?hl=pt-br
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        // Campo de texto para confirmação da senha
        OutlinedTextField(
            modifier = Modifier.padding(10.dp),
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text(text = "Confirmar Senha") },

            // Esconder a senha que está sendo digitada pelo usuário
            // Implementado com base na seção "Texto e tipografia > Processar entrada do usuário" da documentação oficial do Jetpack Compose
            // Fonte: https://developer.android.com/develop/ui/compose/text/user-input?hl=pt-br
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        // Botão para enviar o formulário
        Button(
            onClick = {
                // Verifica se algum campo está em branco (se está vazio ou apenas com espaços)
                // Baseado na documentação oficial do Kotlin: https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.text/is-blank.html
                if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                    Log.i("SIGN UP", "Preencha todos os campos!")
                }

                // Se "@" não estiver contido "!in" no email
                if ("@" !in email) {
                    Log.i("SIGN UP", "Você digitou um email que não é válido!")
                }

                // Se as senhas forem iguais
                else if (password == confirmPassword) {
                    // Cria a conta no Firebase
                    saveUserToAuth(email, password, name, context)
                } else {
                    // Se forem senhas diferentes
                    Log.i("SIGN UP", "As senhas não coincidem.")
                }
            }
        ) {
            Text(text = "Cadastrar")
        }
    }
}
