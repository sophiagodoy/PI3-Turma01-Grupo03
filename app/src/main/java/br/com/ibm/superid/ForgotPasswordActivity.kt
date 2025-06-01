// TELA PARA RECUPERAÇÃO DE SENHA DO USUÁRIO

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
                PreviewForgorPasswordActivity()
            }
        }
    }
}

fun checkEmailVerification(email: String, context: Context, onSuccess: () -> Unit){
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
                    onSuccess()
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

// Função que apresenta o formulário de recuperação de senha
@SuppressLint("SuspiciousIndentation")
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
                    if (email.isNotBlank() && email.contains("@")){
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
                enabled = email.isNotBlank()
            ) {
                Text("Enviar Link de Redefinição")
            }
        }
    }
}

@Composable
@Preview
fun PreviewForgorPasswordActivity(){
    SuperIDTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            // Chama a função composable SignIn e aplica o padding interno do Scaffold
            ForgotPasswordScreen(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}