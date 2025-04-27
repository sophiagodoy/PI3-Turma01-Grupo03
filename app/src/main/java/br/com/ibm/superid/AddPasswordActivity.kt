// TELA PARA CADASTRO DE SENHA
package br.com.ibm.superid

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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

class AddPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AddPassword(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AddPassword(modifier: Modifier = Modifier) {

    // Variáveis que guardam o valor digitado nos campos do formulário
    var senha by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }

    // Layout em coluna, centralizado na tela
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título da tela
        Text(
            text = "ADICIONAR SENHA",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        // Campo de texto para o nome
        OutlinedTextField(
            modifier = Modifier.padding(10.dp),
            value = senha,
            onValueChange = { senha = it },
            label = { Text(text = "Senha") }
        )

        // Campo de texto para o e-mail
        OutlinedTextField(
            modifier = Modifier.padding(10.dp),
            value = categoria,
            onValueChange = { categoria = it },
            label = { Text(text = "Categoria") }
        )

        // Campo de texto para a senha
        OutlinedTextField(
            modifier = Modifier.padding(10.dp),
            value = descricao,
            onValueChange = { descricao = it },
            label = { Text(text = "Descrição") },
        )

        // Botão para enviar o formulário
        Button(
            onClick = {
                // IMPLEMENTAR LÓGICA PARA SALVAR NO BANCO
            }
        ) {
            Text(text = "Salvar")
        }
    }
}