// TELA PARA O USUÁRIO REALIZAR O CADASTRO

// Definição do pacote aplicativo
package br.com.ibm.superid

// Importações necessárias
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import br.com.ibm.superid.ui.theme.core.util.CustomOutlinedTextField
import br.com.ibm.superid.ui.theme.core.util.SuperIDHeader

// Declarando a Activity (signUpActivity)
class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
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

// Essa função cria uma nova conta no Firebase Authentication
fun saveUserToAuth(email: String, password: String, name: String, context: Context) {

    // Obtemos a instância do Firebase Auth
    val auth = Firebase.auth

    // Cria um novo usuário com e-mail e senha
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            // Se a criação da conta for bem-sucedida, obtemos o usuário e seu UID
            if (task.isSuccessful) {
                val user = task.result?.user!!
                Log.i("AUTH", "Conta criada com sucesso")

                // Enviar email para confirmar a conta
                // Implementado com base na seção "Gerenciar usuários > Enviar e-mail de verificação" da documentação oficial do Firebase Authentication
                // Fonte: https://firebase.google.com/docs/auth/web/manage-users?hl=pt-br#web_12
                user.sendEmailVerification()
                    .addOnCompleteListener { verifyTask ->
                        if (verifyTask.isSuccessful) {
                            Log.i("AUTH", "E-mail de verificação enviado com sucesso.")

                            // Exibe uma mensagem Toast para confirmar envio da verificação
                            // Baseado na documentação oficial do Android: https://developer.android.com/guide/topics/ui/notifiers/toasts?hl=pt-br
                            Toast.makeText(context, "E-mail de verificação enviado!", Toast.LENGTH_LONG).show()
                        } else {
                            Log.i("AUTH", "Erro ao enviar e-mail de verificação.", verifyTask.exception)
                        }
                    }

                // Chama a função para salvar os dados do usuário no Firestore
                saveUserToFirestore(name, email, context)
            } else {
                // Em caso de falha, registra o erro
                Log.i("AUTH", "Falha ao criar conta.", task.exception)
            }
        }
}

// Função para salvar os dados necessários durante o cadastro no Firestore
fun saveUserToFirestore(name: String, email: String, context: Context) {
    // Obtendo a instância do banco de dados Firestore
    val db = Firebase.firestore

    // Criando um mapa mutável (hashMap) com informações do cadastro
    val dadosCadastro = hashMapOf(
        "name"  to name,
        "email" to email
    )

    db.collection("users")
        .add(dadosCadastro)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Usuário Criado com sucesso!", Toast.LENGTH_LONG).show()
                // Se salvou com sucesso vai para a EmailVerificationActivity
                context.startActivity(Intent(context, EmailVerificationActivity::class.java))
            } else {
                Toast.makeText(context, "Erro ao criar usuário", Toast.LENGTH_LONG).show()
            }
        }
}

// Função Composable que apresenta o formulário de cadastro do usuário
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SignUp(modifier: Modifier = Modifier) {

    // Cria variável para poder trocar de tela
    val context = LocalContext.current

    // Variáveis que guardam o valor digitado nos campos do formulário
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    
    Column (modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        // Cabeçalho visual personalizado
        SuperIDHeader()

        // Botão de voltar
        IconButton(
            onClick = {
                val intent = Intent(context, AccessOptionActivity::class.java)
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
                .padding(top = 50.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {

            // Define o título da tela em negrito e tamanho 30sp
            Text(
                text = "CADASTRO",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            // Campo de texto para digitar o nome do usuário
            CustomOutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nome"
            )

            // Campo de texto para digitar o email do usuário
            CustomOutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email"
            )

            // Campo de texto para digitar a senha do usuário

            CustomOutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = "Senha",

                // Esconde os caracteres da senha
                // Baseado na documentação: https://developer.android.com/develop/ui/compose/text/user-input?hl=pt-br
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            // Campo de texto para confirmar a senha do usuário
            CustomOutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirmar Senha",

                // Esconde os caracteres da senha
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botão que quando clicado salva informações do cadastro no banco Firestore e verifica se tem algo errado
            // Baseado na documentação: https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.text/is-blank.html
            // Baseado na documentação: https://developer.android.com/guide/topics/ui/notifiers/toasts?hl=pt-br
            Button(onClick = {
                when {
                    // Verifica se os campos estão em brancos ou apenas com espaços
                    name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                        Toast.makeText(context, "Preencha todos os campos!", Toast.LENGTH_LONG).show()
                    }

                    //  Verifica se é um email válido
                    "@" !in email -> {
                        Toast.makeText(context, "Email inválido!", Toast.LENGTH_LONG).show()
                    }

                    // Verifica se as senhas são diferentes
                    password != confirmPassword -> {
                        Toast.makeText(context, "As senhas não coincidem!", Toast.LENGTH_LONG).show()
                    }

                    // Caso não tenha erro, salva no banco de dados Firestore
                    else -> {
                        saveUserToAuth(email, password, name, context)
                    }
                }
            },
                modifier = Modifier
                    .height(60.dp)    // altura maior
                    .width(150.dp)  // largura maior
            ) {
                Text("Cadastrar")
            }
        }
    }
}


