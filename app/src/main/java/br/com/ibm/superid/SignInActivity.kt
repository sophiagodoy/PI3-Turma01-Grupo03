// Define o pacote da aplicação
package br.com.ibm.superid

// Importações necessárias
import android.content.Context
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import br.com.ibm.superid.ui.theme.ui.theme.SuperIDTheme
import kotlin.jvm.java

// SignIpActivity: Activity responsável pela tela de login do usuário
class SignInActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Chama a função composable SignIn e aplica o padding interno do Scaffold
                    SignIn(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// Validando as credenciais do usuário
fun signInWithFirebaseAuth(
    email: String,
    password: String,
    context: Context
) {
    // Obtemos a instância do Firestore Auth
    val auth = Firebase.auth

    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            // Verifica se o login foi bem-sucedido
            if (task.isSuccessful) {
                val user = task.result?.user
                Log.i("AUTH", "Login realizado com sucesso. UID: ${user?.uid}")

                // Redireciona a tela direto para MainActivity
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            } else {
                // Se falhou será mostrado no LogCat
                Log.i("AUTH", "Falha ao fazer login.", task.exception)
            }
        }
}

// Tela de formulário onde o usuário preenche os dados de login (E-mail, Senha)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignIn(modifier: Modifier = Modifier) {
    // Cria variável para poder trocar de tela
    val context = LocalContext.current

    // Variáveis que guardam o valor digitado nos campos do formulário
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

            // Título da tela
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

                // Esconder a senha que está sendo digitada pelo usuário
                // Implementado com base na seção "Texto e tipografia > Processar entrada do usuário" da documentação oficial do Jetpack Compose
                // Fonte: https://developer.android.com/develop/ui/compose/text/user-input?hl=pt-br
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            // Recuperação de senha do usuário
            Text(
                text = "Esqueceu a senha?",
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 8.dp)
                    // Torna o texto clicável
                    // Baseado na documentação: https://developer.android.com/develop/ui/compose/touch-input/pointer-input/tap-and-press?utm_source=chatgpt.com&hl=pt-br
                    .clickable {
                        // Ao clicar vai para a tela ForgotPasswordActivity
                        val intent = Intent(context, ForgotPasswordActivity::class.java)
                        context.startActivity(intent)
                    }
            )

            // Botão para acionar o login
            Button(
                onClick = {
                    // Verifica se algum campo está em branco (se está vazio ou apenas com espaços)
                    // Baseado na documentação oficial do Kotlin: https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.text/is-blank.html
                    if (email.isBlank() && password.isBlank()) {
                        Log.i("SIGN IN", "Preencha todos os campos")
                    } else {
                        // Se não, salva no Firebase Auth
                        signInWithFirebaseAuth(email, password, context)
                        Log.i("SIGN IN", "Usuário logado com sucesso")
                    }
                }
            ) {
                Text("Entrar")
            }
        }
    }
}

