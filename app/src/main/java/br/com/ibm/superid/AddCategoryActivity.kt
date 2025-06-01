// TELA PARA O USUÁRIO ADICIONAR CATEGORIA

package br.com.ibm.superid

import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import br.com.ibm.superid.ui.theme.SuperIDTheme
import br.com.ibm.superid.ui.theme.core.util.CustomOutlinedTextField
import br.com.ibm.superid.ui.theme.core.util.SuperIDHeader
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import br.com.ibm.superid.ui.theme.core.util.addNewCategory

// Declaração da Activity que permite que o usuário adicione uma nova categoria
class AddCategoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
               AddCat()
            }
        }
    }
}

// Função responsável pela interface da tela de adicionar categoria
@Preview
@Composable
fun AddCat(modifier: Modifier = Modifier) {

    val context = LocalContext.current

    var categoryName by remember { mutableStateOf("") } // Variável para armazenar o nome da categoria digitada

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // Função que define o cabeçalho visual personalizado
        SuperIDHeader()

        // Seta de voltar para a MainActivity
        IconButton(
            onClick = {
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar para a tela principal do aplicativo",
                modifier = Modifier.size(35.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 110.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Titulo da tela
            Text(
                text = "ADICIONAR CATEGORIA",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            // Campo para digitar a nova categoria
            CustomOutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = "Nome da Categoria"
            )

            Spacer(Modifier.height(24.dp))

            // Botão que salva a nova categoria no banco
            Button(
                onClick = {
                    when {
                        // Verifica se o campo está em branco (se está vazio ou apenas com espaços)
                        categoryName.isBlank() ->
                            Toast.makeText(context, "Preencha o nome da categoria!", Toast.LENGTH_SHORT).show()

                        // Verifica se o nome da categoria tem menos de 3 caracteres
                        categoryName.length < 3 ->
                            Toast.makeText(context, "Nome da categoria deve ter pelo menos 3 caracteres!", Toast.LENGTH_SHORT).show()

                        // Verifica se a categoria tem mais de 31 caracteres
                        categoryName.length > 31 ->
                            Toast.makeText(context, "Nome da categoria deve ter no máximo 30 caracteres!", Toast.LENGTH_SHORT).show()

                        else -> {
                            addNewCategory(context, categoryName)
                            categoryName = "" // Limpa o campo após salvar, para não continuar sendo exibido na tela
                        }
                    }
                },
                modifier = Modifier
                    .height(60.dp)
                    .width(150.dp)
            ) {
                Text(text = "Salvar")
            }
        }
    }
}