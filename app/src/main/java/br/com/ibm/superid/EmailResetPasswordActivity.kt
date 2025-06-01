// TELA QUE REPRESENTA A CONFIRMAÇÃO DE EMAIL ENVIADO PARA A RECUPERAÇÃO DE SENHA

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

// Declarando a Activity que exibe o formulário para adicionar uma nova senha
class EmailResetPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                PasswordResetVerification()
            }
        }
    }
}

// Função que apresenta as informações sobre a confirmação do email de recuperar senha
@Preview
@Composable
fun PasswordResetVerification(modifier: Modifier = Modifier) {

    val context = LocalContext.current

    Column (modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {

        // Função que define o cabeçalho visual personalizado
        SuperIDHeader()

        // Seta de voltar para a ForgotPasswordActivity
        IconButton(
            onClick = {
                val intent = Intent(context, ForgotPasswordActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar para a tela de esqueceu senha"
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 110.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Text(
                text = "Verifique sua caixa de e-mail altere sua senha se ainda desejar!",
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Reenviar e-mail de redefinição",
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 16.sp,
                style = TextStyle(textDecoration = TextDecoration.Underline), // Aplica sublinado ao texto

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