// Definição do pacote da aplicação
package br.com.ibm.superid

// Importações necessárias para a Activity, Jetpack Compose, Firebase e demais componentes
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ibm.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

// SignUpActivity: Activity responsável pela tela de cadastro de usuário
class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Habilita o uso total da tela, inclusive áreas atrás das barras do sistema
        enableEdgeToEdge()
        // Define o conteúdo da Activity utilizando Jetpack Compose
        setContent {
            // Aplica o tema customizado da aplicação
            SuperIDTheme {
                // Scaffold provê a estrutura básica da tela, garantindo consistência de layout
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Chama a função composable SignUp e aplica o padding interno do Scaffold
                    SignUp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

/**
 * Função para criar uma conta de usuário utilizando o Firebase Auth.
 * Após a criação bem-sucedida, os dados do usuário são salvos no Firestore.
 *
 * @param email O endereço de e-mail do usuário.
 * @param password A senha escolhida pelo usuário.
 * @param name O nome do usuário.
 */
fun saveUserToAuth(email: String, password: String, name: String) {
    // Obtemos a instância do Firebase Auth
    val auth = Firebase.auth

    // Cria um novo usuário com e-mail e senha
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Se a criação da conta for bem-sucedida, obtemos o usuário e seu UID
                val user = task.result.user
                val uid = user!!.uid
                Log.i("AUTH", "Conta criada com sucesso. UID: $uid")

                // Chama a função para salvar os dados do usuário no Firestore
                saveUserToFirestore(name, email, uid)
            } else {
                // Em caso de falha, registra o erro
                Log.i("AUTH", "Falha ao criar conta.", task.exception)
            }
        }
}

/**
 * Função para salvar os dados do usuário no Firestore.
 *
 * @param name O nome do usuário.
 * @param email O endereço de e-mail do usuário.
 * @param uid O identificador único do usuário obtido no Firebase Auth.
 */
fun saveUserToFirestore(name: String, email: String, uid: String) {
    // Obtemos a instância do Firestore
    val db = Firebase.firestore

    // Prepara os dados do usuário em um HashMap
    val userData = hashMapOf(
        "uid" to uid,
        "name" to name,
        "email" to email
    )

    // Adiciona os dados na coleção "users"
    db.collection("users").add(userData)
        .addOnSuccessListener { documentReference ->
            Log.i("Firestore", "Dados do usuário salvos com sucesso. ID do documento: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Erro ao adicionar documento", e)
        }
}

/**
 * Função composable SignUp responsável por exibir a tela de cadastro.
 * Possui campos de entrada para o nome, e-mail e senha, além de um botão para criar a conta.
 *
 * @param modifier Modificador para personalizar o layout do componente.
 */
@Composable
fun SignUp(modifier: Modifier = Modifier) {
    // Variáveis de estado para armazenar os valores digitados pelo usuário
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Organiza os componentes verticalmente em uma Column
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Título da tela de cadastro
        Text(
            text = "Cadastro",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        // Campo para entrada do nome do usuário
        OutlinedTextField(
            modifier = Modifier.padding(10.dp),
            value = name,
            onValueChange = { name = it },
            label = { Text(text = "Nome") }
        )

        // Campo para entrada do e-mail do usuário
        OutlinedTextField(
            modifier = Modifier.padding(10.dp),
            value = email,
            onValueChange = { email = it },
            label = { Text(text = "Email") }
        )

        // Campo para entrada da senha do usuário
        OutlinedTextField(
            modifier = Modifier.padding(10.dp),
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Senha") }
        )

        // Campo para entrada de confirmação de senha do usuário
        OutlinedTextField(
            modifier = Modifier.padding(10.dp),
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text(text = "Confirmar Senha") }
        )

        // Botão para criar a conta utilizando os dados informados
        Button(
            onClick = {
                if (password == confirmPassword) {
                    saveUserToAuth(email, password, name)
                } else {
                    Log.i("SIGN UP", "As senhas não coincidem.")
                }
            }
        ) {
            Text(text = "Cadastrar")
        }
    }
}
