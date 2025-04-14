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
import br.com.ibm.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme

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
                SignInScreen(
                    onBackPressed = { finish() },
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
 * Função composable que define a interface de login com TopAppBar.
 * @param modifier Modificador para personalizar o layout do composable.
 * @param onBackPressed Callback para ação de voltar
 * @param onSignInComplete Callback para quando o login é concluído com sucesso
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    onSignInComplete: () -> Unit,
    onForgotPassword: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Seta pra voltar para WelcomeActivity
    Scaffold(
        topBar = {
            // "Função" para que tenha um elemento no topo da tela que possa ser clicado
            TopAppBar(
                title = { Text("Voltar") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        // Icone de uma setar de voltar utilizada para a tela anterior
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
                        // Ve se os campos de email e senha não estão em branco
                        Log.i("Login", "Tentando login com email: $email")
                        // Faz a conexao com o firebase
                        Firebase.auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.i("Login", "Login bem-sucedido")
                                    onSignInComplete()
                                } else {
                                    Log.i("Login", "Falha no login: ${task.exception?.message}")
                                }
                            }
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