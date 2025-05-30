// TELA QUE O USUÁRIO ESCOLHE QUAL ACESSO DESEJA (LOGIN, CADASTRO)

package br.com.ibm.superid

// Importações necessárias para a Activity, Jetpack Compose e componentes visuais
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.ibm.superid.ui.theme.SuperIDTheme
import br.com.ibm.superid.ui.theme.core.util.SuperIDHeaderImage

// AccessOptionActivity - Tela base do app que permite ao usuário fazer cadastro, login ou entrar sem conta
class AccessOptionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
                PreviewAcessOption()
        }
    }
}

// AccessOptions - Função composable que exibe os botões de acesso: Login, Cadastro e Continuar sem conta
@Composable
fun AccessOptions(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column (modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        // Cabeçalho visual personalizado
        SuperIDHeaderImage()

        // Layout em coluna que ocupa toda a tela e aplica padding de 16dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 150.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botão que leva para a tela de Login
            Button(onClick = {
                val intent = Intent(context, SignInActivity::class.java)
                context.startActivity(intent)
            },
                modifier = Modifier
                    .height(60.dp)    // altura maior
                    .width(150.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(
                    text = "Login",
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.height(18.dp))

            // Botão que leva para a tela de Cadastro
            Button(onClick = {
                val intent = Intent(context, SignUpActivity::class.java)
                context.startActivity(intent)
            },
                modifier = Modifier
                    .height(60.dp)    // altura maior
                    .width(150.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(
                    text = "Cadastro",
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
@Preview
fun PreviewAcessOption(){
    SuperIDTheme {
        // Scaffold provê a estrutura básica da tela, garantindo consistência de layout
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            // Chama a função composable AccessOptions e aplica o padding interno do Scaffold
            AccessOptions(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}