// TELA PARA ALTERAR / REMOLVER SENHA
package br.com.ibm.superid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ibm.superid.ui.theme.SuperIDTheme

class EditPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ShowPasswordInfo(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowPasswordInfo(modifier: Modifier = Modifier) {

    var mostrarDialogo by remember { mutableStateOf(false) }


    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            confirmButton = {
                Button(onClick = { mostrarDialogo = false}

                    // lógica para excluir a senha no banco

                ) {
                    Text("Confirmar")
                }
            },

            dismissButton = {
                Button(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text(text = "Excluir senha") },
            text = { Text("Tem certeza que deseja excluir esta senha? Esta ação não pode ser desfeita.") }
        )

    }


    // Cria variável para poder trocar de tela
    val context = LocalContext.current

    // Seta que volta para MainActivity
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
                            val intent = Intent(context, MainActivity::class.java)
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
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Define o título da tela em negrito e tamanho 30sp
            Text(
                text = "AQUI VAI O TITULO DA SENHA",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            // Espaço de 24dp abaixo do título
            Spacer(Modifier.height(24.dp))

            // Campo de texto para digitar a nova senha
            OutlinedTextField(
                value = "aqui o usuário vê as infos da senha",
                onValueChange = { },
                label = { Text("Senha") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            // Campo de texto para escolher a categoria da nova senha
            OutlinedTextField(
                value = "aqui o usuário vê as infos da senha",
                onValueChange = {  },
                label = { Text("Categoria") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            // Campo de texto para digitar a descrição da noa senha
            OutlinedTextField(
                value = "aqui o usuário vê as infos da senha",
                onValueChange = { },
                label = { Text("Descrição") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            // Espaço de 24dp antes do botão
            Spacer(Modifier.height(24.dp))

            Row (modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Botão que quando clicado envia para a tela de alteração dos dados da senha
                Button(
                    onClick = {

                        val intent = Intent(context, ChangePasswordActivity::class.java)
                        context.startActivity(intent)

                    },
                    modifier = Modifier
                        .height(48.dp)
                ) {
                    // Define o texto que está dentro do botão
                    Text("Alterar")
                }


                // Botão que quando clicado abre pop-up de confirmação de exclusão
                Button(
                    onClick = { mostrarDialogo = true },
                    modifier = Modifier
                        .height(48.dp)
                ) {
                    // Define o texto que está dentro do botão
                    Text("Excluir")
                } }




        }
    }
}
