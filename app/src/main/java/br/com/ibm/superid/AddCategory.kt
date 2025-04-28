package br.com.ibm.superid

import android.content.Context
import androidx.compose.ui.platform.LocalContext

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CheckboxDefaults.colors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast


class AddCategory : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PreviewAddCat()
        }
    }
}

fun addNewCategory(context: Context, nomeCategoria: String) {
    if (nomeCategoria.isBlank()) {
        Toast.makeText(context, "Preencha o nome da categoria!", Toast.LENGTH_SHORT).show()
        return
    }

    val db = FirebaseFirestore.getInstance()

    val categoria = hashMapOf(
        "nome" to nomeCategoria
    )

    db.collection("categorias")
        .add(categoria)
        .addOnSuccessListener {
            Toast.makeText(context, "Categoria cadastrada com sucesso!", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Erro ao cadastrar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}


@Composable
fun AddCat() {
    val context = LocalContext.current
    val categoryName = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.LightGray)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Adicionar Categoria".uppercase(),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        OutlinedTextField(
            modifier = Modifier.padding(10.dp),
            value = categoryName.value,
            onValueChange = { categoryName.value = it },
            label = { Text(text = "Nome da Categoria") }
        )

            Button(
                onClick = {
                    addNewCategory(context, categoryName.value)
                    categoryName.value = "" // Limpa o campo após a tentativa
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9DA783)
                )
            ) {
                Text(text = "Salvar")
            }
    }
}

@Preview
@Composable
fun PreviewAddCat() {
    AddCat()
}
