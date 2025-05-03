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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ibm.superid.ui.theme.SuperIDTheme
import br.com.ibm.superid.ui.theme.core.util.SuperIDHeader
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

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
@Preview
@Composable
fun EmailVerification(modifier: Modifier = Modifier) {

    // Cria variável para poder trocar de tela e mostrar toast
    val context = LocalContext.current


    Column (modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        // Cabeçalho visual personalizado
        SuperIDHeader()

        // Botão de voltar
        IconButton(
            onClick = {
                val intent = Intent(context, ForgotPasswordActivity::class.java)
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
                .padding(top = 110.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
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
            },
                modifier = Modifier
                    .height(60.dp)    // altura maior
                    .width(150.dp)
            ) {
                // Define o texto que está dentro do botão
                Text("Continuar")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Reenviar e-mail de verificação",
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 16.sp,
                style = TextStyle(
                    textDecoration = TextDecoration.Underline),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable {
                        val user = Firebase.auth.currentUser
                        if (user != null) {
                            user.sendEmailVerification()
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        context,
                                        "E-mail de verificação reenviado!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        context,
                                        "Falha ao reenviar",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        } else {
                            Toast.makeText(
                                context,
                                "Usuário não autenticado!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            )

        }
    }
}
