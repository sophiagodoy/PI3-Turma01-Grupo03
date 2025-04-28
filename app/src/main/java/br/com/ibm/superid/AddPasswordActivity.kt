// TELA PARA O USUÁRIO ADICIONAR UMA NOVA SENHA

// Definição do pacote aplicativo
package br.com.ibm.superid

// Importações necessárias
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ibm.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// Declarando a Activity (AddPasswordActivity)
class AddPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Chama a função composable AddPassword e aplica o padding interno do Scaffold
                    AddPassword(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// Função para adicionar uma nova senha no Firestore
fun addNewPassword(context: Context, senha: String, categoria: String, descricao: String) {
    // Baseado na documentação: https://firebase.google.com/docs/auth/android/manage-users?hl=pt-br#get_the_currently_signed-in_user
    // Identifica o usuário atual conectado
    val user = Firebase.auth.currentUser

    // Validando campos obrigatórios
    if (senha.isBlank() || categoria.isBlank() || descricao.isBlank()) {
        Toast.makeText(context, "Preencha todos os campos!", Toast.LENGTH_LONG).show()
    }

    // Obtendo a instância do banco de dados Firestore
    val db = Firebase.firestore

    // Criando uma mapa mutável (hashMap) com informações da nova senha
    val dados_nova_senha = hashMapOf(
        "senha" to senha,
        "categoria" to categoria,
        "descricao" to descricao
    )

    // Gravando os dados no banco de dados Firestore
    db.collection("users") // acessa a coleção chamada users do banco de dados
        .document(user!!.uid) // dentro da coleção "users", aponta para o documento cujo ID é o uid do usuário autenticado
        .collection("senhas") // cria uma subcoleção dentro do documento chamada "senhas"
        .add(dados_nova_senha) // adiciona um novo documento na subcoleção "senhas" usados os dados de "dados_nova_senha"
}

// Função Composable que apresenta o formulário de adicionar senha
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPassword(modifier: Modifier = Modifier) {

    // Cria variável para poder trocar de tela
    val context = LocalContext.current

    // Variáveis que guardam o valor digitado nos campos do formulário
    var senha by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }

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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Define o título da tela em negrito e tamanho 30sp
            Text(
                text = "ADICIONAR SENHA",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            // Espaço de 24dp abaixo do título
            Spacer(Modifier.height(24.dp))

            // Campo de texto para digitar a nova senha
            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                label = { Text("Senha") },
                // Esconde os caracteres da senha
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Campo de texto para escolher a categoria da nova senha
            OutlinedTextField(
                value = categoria,
                onValueChange = { categoria = it },
                label = { Text("Categoria") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            // Campo de texto para digitar a descrição da noa senha
            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            // Espaço de 24dp antes do botão
            Spacer(Modifier.height(24.dp))

            // Botão que quando clicado salva a nova senha no banco Firestore
            Button(
                onClick = {
                    // Chama a função que valida e grava a nova senha no no Firestore
                    addNewPassword(context, senha, categoria, descricao)
                },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(48.dp)
            ) {
                // Define o texto que está dentro do botão
                Text("Salvar")
            }
        }
    }
}
