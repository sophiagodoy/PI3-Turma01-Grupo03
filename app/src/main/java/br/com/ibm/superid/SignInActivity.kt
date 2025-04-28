// TELA PARA O USUÁRIO REALIZAR O LOGIN

// Definição do pacote aplicativo
package br.com.ibm.superid

// Importações necessárias
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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

// Declarando a Activity (signInActivity)
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
fun signInWithFirebaseAuth(email: String, password: String, context: Context) {
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
                // Exibe uma mensagem Toast
                // Baseado na documentação oficial do Android: https://developer.android.com/guide/topics/ui/notifiers/toasts?hl=pt-br
                Toast.makeText(context, "Falha ao fazer login!", Toast.LENGTH_LONG).show()
            }
        }
}

// Função Composable que apresenta o formulário de login do usuário
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignIn(modifier: Modifier = Modifier) {

    // Cria variável para poder trocar de tela
    val context = LocalContext.current

    // Variáveis que guardam o valor digitado nos campos do formulário
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Seta que volta para AccessOptionActivity
    // Baseado em: https://developer.android.com/develop/ui/compose/components/app-bars?hl=pt-br#top-app-bar
    // Baseado em: https://alexzh.com/visual-guide-to-topappbar-variants-in-jetpack-compose/?utm_source=chatgpt.com
    // Estrutura básica da tela utilizando Scaffold para organizar a barra superior e o conteúdo principal
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
                            val intent = Intent(context, AccessOptionActivity::class.java)
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Define o título da tela em negrito e tamanho 30sp
            Text(
                text = "Login",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            // Campo de texto para digitar o email do usuário
            OutlinedTextField(
                modifier = Modifier.padding(10.dp),
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") }
            )

            // Campo de texto para digitar a senha do usuário
            OutlinedTextField(
                modifier = Modifier.padding(10.dp),
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },

                // Esconde os caracteres da senha
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

            // Botão que quando clicado salva no FirebaseAuth
            Button(
                onClick = {
                    // Verifica se algum campo está em branco (se está vazio ou apenas com espaços)
                    if (email.isBlank() || password.isBlank()) {
                        Log.i("SIGN IN", "Preencha todos os campos")

                        // Toast para avisar que precisa preencher todos os dados
                        Toast.makeText(context, "Preencha todos os campos!", Toast.LENGTH_LONG).show()

                    } else {
                        // Salva no Firebase Auth
                        signInWithFirebaseAuth(email, password, context)
                        Log.i("SIGN IN", "Usuário logado com sucesso")
                    }
                }
            ) {
                // Define o texto que está dentro do botão
                Text("Entrar")
            }
        }
    }
}

