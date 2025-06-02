// TELA PARA O USUÁRIO SER INFORMADO SOBRE A VERIFICAÇÃO DO EMAIL E SUAS CONDIÇÕES

package br.com.ibm.superid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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

// Declarando a Activity para informar sobre verificação do email e suas condições
class EmailVerificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                EmailVerification()
            }
        }
    }
}

// Função Composable que apresenta as informações sobre a confirmação do email
@Preview
@Composable
fun EmailVerification(modifier: Modifier = Modifier) {

    val context = LocalContext.current

    Column (modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {

        // Cabeçalho visual personalizado
        SuperIDHeader()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 110.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {

            Text(
                text = "Verifique sua caixa de e-mail e confirme o endereço fornecido.\n\n" +
                        "Sem a confirmação, não será possível utilizar o login sem senha nem redefinir sua senha no aplicativo.\n\n" +
                        "Caso já tenha confirmado, por favor, feche e abra novamente o aplicativo para que as atualizações sejam aplicadas.",
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botão para continuar para o login
            Button(
                onClick = {
                    context.startActivity(
                        Intent(context, SignInActivity::class.java)
                    )
                },
                modifier = Modifier
                    .height(60.dp)
                    .width(150.dp)
            ) {
                Text("Continuar")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Reenviar e-mail de verificação",
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 16.sp,
                style = TextStyle(
                    textDecoration = TextDecoration.Underline), // Aplica sublinado ao texto

                modifier = Modifier
                    .padding(top = 8.dp)

                    // Torna o texto clicável
                    .clickable {

                        // Obtém o usuário logado
                        val user = Firebase.auth.currentUser

                        // Verifica se realmente existe um usuário logado
                        if (user != null) {
                            user.sendEmailVerification()
                                .addOnSuccessListener {
                                    Toast.makeText(context, "E-mail de verificação reenviado!", Toast.LENGTH_LONG).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Falha ao reenviar", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            Toast.makeText(context, "Usuário não autenticado!", Toast.LENGTH_LONG).show()
                        }
                    }
            )
        }
    }
}