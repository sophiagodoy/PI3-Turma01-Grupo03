// TELA PARA O USUÁRIO REALIZAR O LOGIN

package br.com.ibm.superid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import br.com.ibm.superid.ui.theme.SuperIDTheme
import br.com.ibm.superid.ui.theme.core.util.CustomOutlinedTextField
import br.com.ibm.superid.ui.theme.core.util.SuperIDHeader
import br.com.ibm.superid.ui.theme.core.util.signInWithFirebaseAuth
import kotlin.jvm.java

// Declarando a Activity para realizar o login do usuário
class SignInActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                SignIn()
            }
        }
    }
}

// Função responsável pela interface da tela de login
@Composable
fun SignIn(modifier: Modifier = Modifier) {

    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) } // Variável para ver a senha

    Column (
        modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background
        )
    ) {

        // Cabeçalho visual personalizado
        SuperIDHeader()

        // Seta de voltar
        // Baseado em: https://developer.android.com/develop/ui/compose/components/app-bars?hl=pt-br#top-app-bar
        // Baseado em: https://alexzh.com/visual-guide-to-topappbar-variants-in-jetpack-compose/
        IconButton(
            onClick = {
                val intent = Intent(context, AccessOptionActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar"
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ){

            // Define o título da tela
            Text(
                text = "LOGIN",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            // Campo para digitar o email
            CustomOutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email"
            )

            // Campo para digitar a senha
            CustomOutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = "Senha",

                // Ocultação e exibição de senha
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None // Exibe o texto normalmente
                } else {
                    PasswordVisualTransformation() // Substitui cada caracter por "."
                },

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),

                // Alterando a visibilidade da imagem
                trailingIcon = {
                    IconButton(
                        onClick = {
                            passwordVisible = !passwordVisible
                        }
                    ) {
                        Icon(
                            imageVector = if (passwordVisible){
                                Icons.Default.Visibility // Olho aberto
                            } else {
                                Icons.Default.VisibilityOff // Olho fechado
                            },
                            contentDescription = if (passwordVisible){
                                "Ocultar senha"
                            } else {
                                "Mostrar senha"
                            }
                        )
                    }
                }
            )

            // Texto clicável para a recuperação de senha do usuário
            Text(
                text = "Esqueceu a senha?",
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 16.sp,
                style = TextStyle(textDecoration = TextDecoration.Underline),
                modifier = Modifier
                    .padding(top = 8.dp)

                    // Torna o texto clicável
                    // Baseado na documentação: https://developer.android.com/develop/ui/compose/touch-input/pointer-input/tap-and-press?hl=pt-br
                    .clickable {
                        val intent = Intent(context, ForgotPasswordActivity::class.java)
                        context.startActivity(intent)
                    }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botão de ação para logar o usuário
            Button(
                onClick = {
                    // Verifica se algum campo está em branco (se está vazio ou apenas com espaços)
                    if (email.isBlank() || password.isBlank()) {
                        Log.i("SIGN IN", "Preencha todos os campos")
                        Toast.makeText(context, "Preencha todos os campos!", Toast.LENGTH_LONG).show()

                    } else {
                        signInWithFirebaseAuth(email, password, context)
                        Log.i("SIGN IN", "Usuário logado com sucesso")
                    }
                },
                modifier = Modifier
                    .height(60.dp)
                    .width(150.dp)
            ) {
                Text("Entrar")
            }
        }
    }
}

@Preview
@Composable
fun SignInPreview(){
    SuperIDTheme {
        SignIn()
    }
}

