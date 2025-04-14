// Define o pacote da aplicação
package br.com.ibm.superid

// Importações necessárias para Android, Jetpack Compose e Firebase
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ibm.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * Activity responsável pela tela de login (SignIn).
 * Utiliza Jetpack Compose para renderizar a interface e Firebase para autenticação.
 */
class SignInActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Chama o composable SignIn, passando o padding interno do Scaffold
                    SignIn(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

/**
 * Realiza a autenticação do usuário no Firebase Auth utilizando email e senha.
 * @param email    Endereço de e-mail informado pelo usuário.
 * @param password Senha informada pelo usuário.
 */
fun signInWithFirebase(email: String, password: String) {
    // Obtém a instância do Firebase Auth
    val auth = Firebase.auth

    // Tenta autenticar o usuário com as credenciais fornecidas
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            // Se a autenticação for bem-sucedida...
            if (task.isSuccessful) {
                // Obtém o usuário autenticado
                val user = task.result?.user
                // Registra no log o sucesso do login e o UID do usuário
                Log.i("AUTH", "Login realizado com sucesso. UID: ${user?.uid}")
            } else {
                // Em caso de falha, registra o erro no log, informando a exceção ocorrida
                Log.e("AUTH", "Falha ao fazer login.", task.exception)
            }
        }
}

/**
 * Função composable que define a interface de login.
 * @param modifier Modificador para personalizar o layout do composable.
 */
@Composable
fun SignIn(modifier: Modifier = Modifier) {
    //criar variável para poder trocar de tela
    val context = LocalContext.current
    // Estados para armazenar os valores digitados nos campos de email e senha
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Utiliza uma Column para organizar os componentes verticalmente
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Exibe o título da tela de login com fonte grande e em negrito
        Text(
            text = "Login",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        // Campo de entrada para o email do usuário
        OutlinedTextField(
            modifier = Modifier.padding(10.dp),
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "Email") }
        )

        // Campo de entrada para a senha do usuário
        OutlinedTextField(
            modifier = Modifier.padding(10.dp),
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Senha") }
        )

        // Botão para acionar o login
        Button(
            onClick = {
                // Ao clicar, chama a função de autenticação passando email e senha
                //leva para a tela principal
                signInWithFirebase(email, password)
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            }
        ) {
            // Texto exibido dentro do botão
            Text(text = "Entrar")
        }
    }
}
