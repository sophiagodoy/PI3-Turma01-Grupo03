// TELA PARA RECUPERAÇÃO DE SENHA

// Definição do pacote aplicativo
package br.com.ibm.superid

// Importações necessárias
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import br.com.ibm.superid.ui.theme.ui.theme.SuperIDTheme
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

// Função para enviar email de redefinição de senha (Arthur)


// Função Composable que apresenta o formulário de recuperação de senha
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreen(modifier: Modifier = Modifier) {
    // Cria variável para poder trocar de tela
    val context = LocalContext.current
    // Variável que armazena o email digitado pelo usuário
    var email by remember { mutableStateOf("") }

    // Seta que volta para SignInActivity
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Define o título da tela em negrito e tamanho 30sp
            Text(
                text = "Recuperar Senha",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
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
            OutlinedTextField(
                modifier = Modifier.padding(10.dp),
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            // Botão que será clicado para enviar o link de redefinição
            Button(
                onClick = {
                    // A ação de envio ainda será implementada
                }
            ) {
                // Texto exibido no botão
                Text("Enviar Link de Redefinição")
            }
        }
    }
}
