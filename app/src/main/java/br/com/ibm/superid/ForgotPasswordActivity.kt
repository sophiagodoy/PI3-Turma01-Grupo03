// TELA PARA RECUPERAÇÃO DE SENHA

// Definição do pacote aplicativo
package br.com.ibm.superid

// Importações necessárias
import android.annotation.SuppressLint
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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

// Declarando a Activity (ForgotPasswordActivity)
class ForgotPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Chama a função composable SignIn e aplica o padding interno do Scaffold
                    ForgotPasswordScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

fun checkEmailVerification(email: String, context: Context){
    val db = Firebase.firestore

    db.collection("users")
        .whereEqualTo("email", email)
        .get()
        .addOnSuccessListener { result ->
            if ( result.documents.isNotEmpty()) {
                // Pega o primeiro documento que contenha esse email
                val userDoc =  result.documents[0]
                // Lê o campo "emailVerified" (ou false, se não existir)
                val isVerified = userDoc.getBoolean("emailVerified") ?: false

                if (isVerified) {
                    // E-mail verificado: prossegue com o envio do link
                    sendEmail(email, context)
                } else {
                    // Ainda não confirmou o e-mail
                    Toast.makeText(
                        context,
                        "Por favor, verifique seu e-mail antes de redefinir a senha.", Toast.LENGTH_LONG).show()
                        val intent = Intent(context, EmailVerificationActivity::class.java)
                        context.startActivity(intent)
                }
            } else {
                // Nenhum usuário encontrado com esse e-mail
                Toast.makeText(context, "E-mail não encontrado. Cadastre-se antes de tentar redefinir a senha.", Toast.LENGTH_LONG).show()
            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Erro ao verificar o status de verificação: ${e.message}", Toast.LENGTH_LONG).show()
        }
}

fun sendEmail(email: String, context: Context ){

    Firebase.auth.sendPasswordResetEmail(email)
        .addOnSuccessListener {
                Log.d(TAG, "Email enviado para $email.")
                Toast.makeText(
                    context, "Link de redefinição enviado ", Toast.LENGTH_LONG).show()
            /*
            dps levará a pessoa pra outra tela pra redefinir a senha
             */
            }
        .addOnFailureListener{ e ->
            Log.e(TAG, "Erro no envio do email para $email", e)
            Toast.makeText(context, "Falha ao enviar link de redefinição: ${e.message}", Toast.LENGTH_LONG).show()

        }
        }

// Função Composable que apresenta o formulário de recuperação de senha
@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreen(modifier: Modifier = Modifier) {
    // Cria variável para poder trocar de tela
    val context = LocalContext.current
    // Variável que armazena o email digitado pelo usuário
    var email by remember { mutableStateOf("") }

    Column (modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        // Cabeçalho visual personalizado
        SuperIDHeader()

        // Botão de voltar
        IconButton(
            onClick = {
                val intent = Intent(context, SignInActivity::class.java)
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
                .padding(top = 100.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Define o título da tela em negrito e tamanho 30sp
            Text(
                text = "Recuperar Senha",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(24.dp))

            // Texto explicativo para o usuário
            Text(
                text = "Digite seu email cadastrado para realizar a redefinição de senha",
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth() //
                    // Aplica padding horizontal
                    .padding(horizontal = 16.dp)
                    // Centraliza o conteúdo horizontalmente no centro
                    .wrapContentWidth(Alignment.CenterHorizontally),
                // Alinha o texto ao centro
                textAlign = TextAlign.Center
            )
            // Campo de texto para digitar o email do usuário
            CustomOutlinedTextField(
                modifier = Modifier.padding(10.dp),
                value = email,
                onValueChange = { email = it },
                label ="Email",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(Modifier.height(16.dp))

            // Botão que será clicado para enviar o link de redefinição
            Button(
                onClick = {
                    // Ao clicar no botão, chama a função para enviar o link de redefinição


                    if (email.isNotBlank() && email.contains("@")){

                        checkEmailVerification(email, context)

                    } else{
                        // Mostra uma mensagem de erro ou feedback visual
                        Toast.makeText(context, "Por favor, insira seu email", Toast.LENGTH_SHORT).show()
                    }
                },
                // Se o email estiver em branco, desabilita o botão
                modifier = Modifier
                    .height(50.dp)
                    .width(250.dp),
                enabled = email.isNotBlank()
            ) {
                // Texto exibido no botão
                Text("Enviar Link de Redefinição")
            }
        }
    }
}