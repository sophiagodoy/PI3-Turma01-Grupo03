// TELA PARA RECUPERAÇÃO DE SENHA DO USUÁRIO

package br.com.ibm.superid

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import br.com.ibm.superid.ui.theme.SuperIDTheme
import br.com.ibm.superid.ui.theme.core.util.CustomOutlinedTextField
import br.com.ibm.superid.ui.theme.core.util.SuperIDHeader
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.jvm.java
import br.com.ibm.superid.ui.theme.core.util.checkEmailVerification

// Declarando a Activity para recuperação de senha do usuário
class ForgotPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                ForgotPasswordScreen()
            }
        }
    }
}

// Função que apresenta o formulário de recuperação de senha
@Preview
@Composable
fun ForgotPasswordScreen(modifier: Modifier = Modifier) {

    val context = LocalContext.current

    var email by remember { mutableStateOf("") }

    Column (modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {

        // Cabeçalho visual personalizado
        SuperIDHeader()

        // Seta de voltar para o login
        IconButton(
            onClick = {
                val intent = Intent(context, SignInActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar para o login"
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Define o título da tela
            Text(
                text = "RECUPERAR SENHA",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Digite seu email cadastrado para realizar a redefinição de senha!",
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth() //
                    .padding(horizontal = 16.dp)
                    .wrapContentWidth(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center
            )

            // Campo para digitar o email
            CustomOutlinedTextField(
                modifier = Modifier.padding(10.dp),
                value = email,
                onValueChange = { email = it },
                label ="Email",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(Modifier.height(16.dp))

            // Botão que ao ser clicado manda o link de redefinição de senha no email
            Button(
                onClick = {
                    // Verifica se o campo "email" não está em branco e não contém o caractere '@'
                    if (email.isNotBlank() && '@' in email) {
                        checkEmailVerification(email, context){
                            val intent = Intent(context, EmailResetPasswordActivity::class.java)
                            context.startActivity(intent)
                        }
                    } else{
                        Toast.makeText(context, "Por favor, insira seu email", Toast.LENGTH_SHORT).show()
                    }
                },

                // Se o email estiver em branco, desabilita o botão
                modifier = Modifier
                    .height(50.dp)
                    .width(250.dp),
                enabled = email.isNotBlank() // Controla se o botão está ativo ou inativo
            ) {
                Text("Enviar Link de Redefinição")
            }
        }
    }
}