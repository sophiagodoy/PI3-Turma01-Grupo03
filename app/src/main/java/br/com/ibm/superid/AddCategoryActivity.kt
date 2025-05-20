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

//Função que adiciona uma nova categoria no Firestore
fun addNewCategory(context: Context, categoryName: String) {
    val user = Firebase.auth.currentUser // Obtém o usuário autenticado atual

    if (user == null) {
        // Caso não esteja autenticado, avisa o usuário e retorna sem fazer nada
        Toast.makeText(context, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
        return
    }

    val db = Firebase.firestore
    val categoriasRef = db.collection("users")
        .document(user.uid)
        .collection("categorias") // Referência à coleção de categorias do usuário atual

    // Verifica se já existe uma categoria com o mesmo nome para evitar duplicatas
    categoriasRef
        .whereEqualTo("nome", categoryName)
        .get()
        .addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                // Se categoria já existe, informa o usuário
                Toast.makeText(context, "Categoria já existe!", Toast.LENGTH_SHORT).show()
            } else {
                // Caso contrário, cria uma nova categoria com os campos padrão
                val novaCategoria = hashMapOf(
                    "nome" to categoryName,
                    "isDefault" to false,
                    "undeletable" to false
                )

                // Adiciona a nova categoria no Firestore
                categoriasRef
                    .add(novaCategoria)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "✅ Categoria adicionada com sucesso!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Erro ao adicionar categoria: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
        .addOnFailureListener { exception ->
            // Trata falhas na consulta Firestore (ex: problemas de conexão)
            Toast.makeText(context, "Erro ao verificar categoria: ${exception.message}", Toast.LENGTH_SHORT).show()
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

                        categoryName.length > 10 ->
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