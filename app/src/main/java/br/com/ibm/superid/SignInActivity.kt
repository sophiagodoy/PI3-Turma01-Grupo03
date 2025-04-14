// Define o pacote da aplicação
package br.com.ibm.superid

// Importações necessárias para Android, Jetpack Compose e Firebase
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import br.com.ibm.superid.ui.theme.ui.theme.SuperIDTheme
import kotlin.jvm.java

/**
 * Activity responsável pela tela de login (SignIn).
 * Utiliza Jetpack Compose para renderizar a interface e Firebase para autenticação.
 */
class SignInActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Habilita o uso total da tela, inclusive áreas atrás das barras do sistema
        enableEdgeToEdge()
        // Define o conteúdo da Activity utilizando Jetpack Compose
        setContent {
            // Aplica o tema customizado da aplicação
            SuperIDTheme{
                SignInScreen(
                    onSignInComplete = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    },
                    onForgotPassword = {
                        startActivity(Intent(this, ForgotPasswordActivity::class.java))
                    }
                )
            }
        }
    }
}

/**
 * Realiza a autenticação do usuário no Firebase Auth utilizando email e senha.
 * @param email    Endereço de e-mail informado pelo usuário.
 * @param password Senha informada pelo usuário.
 */
fun signInWithFirebase(
    email: String,
    password: String,
    onSuccess: () -> Unit
) {
    val auth = Firebase.auth

    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = task.result?.user
                Log.i("AUTH", "Login realizado com sucesso. UID: ${user?.uid}")
                onSuccess()
            } else {
                Log.e("AUTH", "Falha ao fazer login.", task.exception)
            }
        }
}

/**
 * Função composable que define a interface de login com TopAppBar.
 * @param modifier Modificador para personalizar o layout do composable.
 * @param onSignInComplete Callback para quando o login é concluído com sucesso
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    modifier: Modifier = Modifier,
    onSignInComplete: () -> Unit,
    onForgotPassword: () -> Unit = {}
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }


    // Seta pra voltar para WelcomeActivity
    Scaffold(
        topBar = {
            // "Função" para que tenha um elemento no topo da tela que possa ser clicado
            TopAppBar(
                title = { Text("Voltar") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            val intent = Intent(context, WelcomeActivity::class.java)
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
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
                label = { Text("Email") }
            )

            // Campo de entrada para a senha do usuário
            OutlinedTextField(
                modifier = Modifier.padding(10.dp),
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },
                visualTransformation = PasswordVisualTransformation()
            )

            // Campo para a recuperação de senha
            Text(
                text = "Esqueceu a sua senha?",
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { onForgotPassword() },
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            // Botão para acionar o login
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        Log.i("Login", "Tentando login com email: $email")
                        signInWithFirebase(
                            email = email,
                            password = password,
                            onSuccess = {
                                val intent = Intent(context, MainActivity::class.java)
                                context.startActivity(intent)
                                onSignInComplete()
                            }
                        )
                    } else {
                        Log.i("Login", "Campos de email ou senha estão em branco")
                    }
                }
            ) {
                Text("Entrar")
            }
        }
    }
}