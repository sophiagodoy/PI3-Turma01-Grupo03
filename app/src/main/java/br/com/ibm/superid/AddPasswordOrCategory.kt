package br.com.ibm.superid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class AddPasswordOrCategory : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PreviewAddPasswordOrCat()
        }
    }
}

@Composable
fun AddPasswordOrCat(){
    val context = LocalContext.current
    var categoria by remember { mutableStateOf(false) }
    var senha by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.LightGray)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            if(categoria){
                val intent = Intent(context, AddCategory::class.java) //
                context.startActivity(intent)
            }
        },
            enabled = categoria
        ) {
            Text(text = "Adicionar Categoria: ")
        }
        Button(onClick = {
            val intent = Intent(context, AddPasswordActivity::class.java) //
            context.startActivity(intent)
        },
            enabled = senha
        ) {
            Text(text = "Adicionar senha: ")
        }
    }
}

@Preview
@Composable
fun PreviewAddPasswordOrCat(){
    AddPasswordOrCat()
}
