// TELA PARA O USUÁRIO SER INFORMADO SOBRE A VERIFICAÇÃO DO EMAIL E SUAS CONDIÇÕES

// Definição do pacote aplicativo
package br.com.ibm.superid

// Importações necessárias
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.ibm.superid.ui.theme.SuperIDTheme

// Declarando a Activity (EmailVerificationActivity)
class EmailVerificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Chama a função composable EmailVerification e aplica o padding interno do Scaffold
                    EmailVerification(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// Função Composable que apresenta as informações sobre a confirmação do email
@Composable
fun EmailVerification(modifier: Modifier = Modifier) {

    // Cria variável para poder trocar de tela e mostrar toast
    val context = LocalContext.current

    // Layout em coluna que ocupa toda a tela e aplica padding de 24dp
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Exibe um texto na tela sobre as informações do email
        Text(
            text = "Verifique a sua caixa de email e confirme o email.\n" +
                    "Se o email não for confirmado não será possível usar a funcionalidade \"Login Sem Senha\".",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Botão para continuar para o login
        Button(onClick = {
            context.startActivity(Intent(context, SignInActivity::class.java))
        }) {
            // Define o texto que está dentro do botão
            Text("Continuar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botão para reenviar o email de verificação
        Button(onClick = {
            Toast.makeText(context, "Email reenviado!", Toast.LENGTH_SHORT).show()
        }) {
            // Define o texto que está dentro do botão
            Text("Reenviar email")
        }
    }
}
