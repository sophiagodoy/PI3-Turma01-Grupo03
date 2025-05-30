// TELA PARA O USUÁRIO ADICIONAR CATEGORIA

package br.com.ibm.superid

import android.content.Context
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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import br.com.ibm.superid.ui.theme.core.util.addNewCategory


class AddCategoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
               PreviewAddCat()
            }
        }
    }
}

//Composable que constrói a UI para adicionar uma categoria
@Composable
fun AddCat() {
    val context = LocalContext.current
    var categoryName by remember { mutableStateOf("") } // Estado para armazenar o nome da categoria digitada

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Define fundo conforme o tema
    ) {
        SuperIDHeader() // Cabeçalho visual customizado da aplicação

        // Botão para voltar para a tela principal
        IconButton(
            onClick = {
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                modifier = Modifier.size(35.dp)
            )
        }

        // Conteúdo centralizado da tela para adicionar a categoria
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 110.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ADICIONAR CATEGORIA",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            // Campo de texto customizado para o nome da categoria
            CustomOutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = "Nome da Categoria"
            )

            Spacer(Modifier.height(24.dp))

            // Botão para salvar a categoria
            Button(
                onClick = {
                    when {
                        categoryName.isBlank() ->
                            Toast.makeText(context, "Preencha o nome da categoria!", Toast.LENGTH_SHORT).show()

                        categoryName.length < 3 ->
                            Toast.makeText(context, "Nome da categoria deve ter pelo menos 3 caracteres!", Toast.LENGTH_SHORT).show()

                        categoryName.length > 31 ->
                            Toast.makeText(context, "Nome da categoria deve ter no máximo 30 caracteres!", Toast.LENGTH_SHORT).show()

                        else -> {
                            // Chama função para adicionar categoria no banco
                            addNewCategory(context, categoryName)
                            categoryName = "" // Limpa o campo após salvar
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

@Preview
@Composable
fun PreviewAddCat() {
    SuperIDTheme {
        AddCat()
    }
}