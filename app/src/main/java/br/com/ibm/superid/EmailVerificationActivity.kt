// TELA PARA VERIFICAÇÃO DE CÓDIGO POR EMAIL

// Definição do pacote aplicativo
package br.com.ibm.superid

// Importações necessárias
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ibm.superid.ui.theme.SuperIDTheme
import br.com.ibm.superid.ui.theme.core.util.CustomOutlinedTextField
import br.com.ibm.superid.ui.theme.core.util.SuperIDHeader

// Declarando a Activity (EmailVerificationActivity)
class EmailVerificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                // Utiliza Scaffold para manter consistência de layout
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Chama a função composable EmailVerificationScreen
                    EmailVerificationScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// Função Composable que exibe a tela de verificação do código
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailVerificationScreen(modifier: Modifier = Modifier) {
    // Recupera o contexto atual para navegação
    val context = LocalContext.current
    // Variável de estado para armazenar o código digitado
    var verificationCode by remember { mutableStateOf("") }

    // Layout principal da tela
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Define o fundo com o tema do app
    ) {
        // Cabeçalho visual personalizado
        SuperIDHeader()

        // Botão de voltar
        IconButton(
            onClick = {
                // Ao clicar, retorna para a tela de esqueci minha senha
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
        // Coluna para alinhar os elementos principais da tela
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título da tela
            Text(
                text = "VERIFICAÇÃO DE E-MAIL",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Mensagem explicativa para o usuário
            Text(
                text = "Digite o código recebido no seu email",
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                textAlign = TextAlign.Center
            )

            // Campo para o usuário digitar o código de verificação
            CustomOutlinedTextField(
                value = verificationCode,
                onValueChange = {
                    // Limita o código a no máximo 6 caracteres
                    if (it.length <= 6) {
                        verificationCode = it
                    }
                },
                label = "Código de verificação",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .width(300.dp)
                    .padding(vertical = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botão que realiza a verificação e redireciona para a tela de nova senha
            Button(
                onClick = {
                    // Fazer funçao que verifica se o código é o mesmo que o enviado no E-Mail

                    // Codigo para alterar de tela apos o codigo ser verificado
                    // val intent = Intent(context, ChangePasswordActivity::class.java)
                    // context.startActivity(intent)
                },
                modifier = Modifier
                    .height(50.dp)
                    .width(250.dp),
                enabled = verificationCode.length == 6 // Só habilita o botão se tiver 6 dígitos
            ) {
                Text("Verificar Código")
            }

            // Texto clicável para reenviar código
            Text(
                text = "Não recebeu o código? Reenviar",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 24.dp)
                    .clickable {
                        // Aqui precisa colocar a logica para reenviar o codigo
                    }
            )
        }
    }
}

// Função de preview para testes visuais no Android Studio
@Preview(showBackground = true)
@Composable
fun EmailVerificationPreview() {
    SuperIDTheme {
        EmailVerificationScreen()
    }
}