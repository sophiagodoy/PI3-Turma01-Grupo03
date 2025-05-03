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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import br.com.ibm.superid.ui.theme.SuperIDTheme
import br.com.ibm.superid.ui.theme.core.util.CustomOutlinedTextField
import br.com.ibm.superid.ui.theme.core.util.SuperIDHeader
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore


class AddCategoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ativa o modo de bordas estendidas
        enableEdgeToEdge()
        // Define o conteúdo da tela com o tema do app
        setContent {
            SuperIDTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    PreviewAddCat()
                }
            }
        }
    }
}

fun addNewCategory(context: Context, nomeCategoria: String) {
    val user = Firebase.auth.currentUser

    if (user == null) {
        Toast.makeText(context, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
        return
    }

    if (nomeCategoria.isBlank()) {
        Toast.makeText(context, "Preencha o nome da categoria!", Toast.LENGTH_SHORT).show()
        return
    }

    val db = Firebase.firestore
    val novaCategoria = hashMapOf("nome" to nomeCategoria)

    db.collection("users")
        .document(user.uid)
        .collection("categorias")
        .add(novaCategoria)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Categoria adicionada com sucesso!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Erro ao adicionar categoria: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
}


@Composable
fun AddCat() {
    val context = LocalContext.current
    val categoryName = remember { mutableStateOf("") }

    // Seta que volta para AccessOptionActivity
    // Baseado em: https://developer.android.com/develop/ui/compose/components/app-bars?hl=pt-br#top-app-bar
    // Baseado em: https://alexzh.com/visual-guide-to-topappbar-variants-in-jetpack-compose/?utm_source=chatgpt.com

    Column (modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        // Cabeçalho visual personalizado
        SuperIDHeader()

        // Botão de voltar
        IconButton(
            onClick = {
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar"
            )
        }

        // Layout em coluna que ocupa toda a tela e aplica padding de 16dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 110.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Text(
                text = "Adicionar Categoria".uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )

            CustomOutlinedTextField(
                value = categoryName.value,
                onValueChange = { categoryName.value = it },
                label = "Nome da Categoria"
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    addNewCategory(context, categoryName.value)
                    categoryName.value = ""
                },
                modifier = Modifier
                    .height(60.dp)    // altura maior
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
    AddCat()
}
