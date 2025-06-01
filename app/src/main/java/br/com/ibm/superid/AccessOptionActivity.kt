// TELA QUE O USUÁRIO ESCOLHE QUAL ACESSO DESEJA (LOGIN OU CADASTRO)

package br.com.ibm.superid

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.ibm.superid.ui.theme.SuperIDTheme
import br.com.ibm.superid.ui.theme.core.util.SuperIDHeaderImage

// Declaração da Activity que permite que o usuário escolha qual acesso deseja
class AccessOptionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                AccessOptions()
            }
        }
    }
}


// Função responsável pela interface da tela de escolher opção de acesso
@Preview
@Composable
fun AccessOptions(modifier: Modifier = Modifier) {

    val context = LocalContext.current

    Column (modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {

        // Função que define o cabeçalho visual personalizado
        SuperIDHeaderImage()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 150.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Botão que leva para a tela de Login
            Button(
                onClick = {
                    val intent = Intent(context, SignInActivity::class.java)
                    context.startActivity(intent)
            },
                modifier = Modifier
                    .height(60.dp)
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
            Button(
                onClick = {
                    val intent = Intent(context, SignUpActivity::class.java)
                    context.startActivity(intent)
            },
                modifier = Modifier
                    .height(60.dp)
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