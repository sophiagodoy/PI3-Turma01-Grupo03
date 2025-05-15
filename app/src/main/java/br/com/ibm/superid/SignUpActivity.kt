// TELA PARA O USUÁRIO REALIZAR O CADASTRO

package br.com.ibm.superid

import android.annotation.SuppressLint
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

// Classe da Activity de cadastro
class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ativa o modo de tela cheia com suporte à barra de status
        enableEdgeToEdge()
        // Define o conteúdo da tela
        setContent {
            SuperIDTheme {
                SignUp()
            }
        }
    }
}

// Essa função cria a conta do usuário no Firebase Authentication
fun saveUserToAuth(email: String, password: String, name: String, context: Context) {
    val auth = Firebase.auth

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = task.result?.user!!
                val uid = user.uid
                Log.i("AUTH", "Conta criada com sucesso")

                // Envia e-mail de verificação para o usuário
                user.sendEmailVerification()
                    .addOnCompleteListener { verifyTask ->
                        if (verifyTask.isSuccessful) {
                            Log.i("AUTH", "E-mail de verificação enviado com sucesso.")
                            Toast.makeText(context, "E-mail de verificação enviado!", Toast.LENGTH_LONG).show()
                        } else {
                            Log.i("AUTH", "Erro ao enviar e-mail de verificação.", verifyTask.exception)
                        }
                    }

                // Salva os dados no Firestore
                saveUserToFirestore(uid, name, email, context)
            } else {
                // Exibe erro caso o cadastro falhe
                Log.i("AUTH", "Falha ao criar conta.", task.exception)
                Toast.makeText(context, "Erro ao criar conta: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
}

// Essa função salva os dados do usuário no Firestore
@SuppressLint("HardwareIds")
fun saveUserToFirestore(uid: String, name: String, email: String, context: Context) {
    val db = Firebase.firestore

    // Obtém o ID do dispositivo Android
    val androidId = android.provider.Settings.Secure.getString(
        context.contentResolver,
        android.provider.Settings.Secure.ANDROID_ID
    )

    // Monta o mapa com os dados que serão salvos
    val dadosCadastro = hashMapOf(
        "name" to name,
        "email" to email,
        "emailVerified" to false,
        "androidId" to androidId
    )

    // Salva os dados no documento com o UID do usuário
    db.collection("users")
        .document(uid)
        .set(dadosCadastro)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Cria as categorias padrão para o novo usuário
                createDefaultCategorias(uid, context)

                Toast.makeText(context, "Cadastro realizado com sucesso!", Toast.LENGTH_LONG).show()

                // Redireciona para a tela de verificação de e-mail
                context.startActivity(Intent(context, EmailVerificationActivity::class.java))
            } else {
                // Exibe erro caso a gravação falhe
                Toast.makeText(context, "Erro ao salvar dados do usuário", Toast.LENGTH_LONG).show()
            }
        }
}

// Essa função cria categorias padrão para novos usuários no Firestore
fun createDefaultCategorias(userId: String, context: Context) {
    val db = Firebase.firestore

    // Referência para a subcoleção de categorias do usuário
    val categoriasRef = db.collection("users").document(userId).collection("categorias")

    // Lista com as categorias iniciais
    val defaultCategorias = listOf(
        hashMapOf(
            "nome" to "Sites Web",
            "isDefault" to true,
            "undeletable" to true
        ),
        hashMapOf(
            "nome" to "Aplicativos",
            "isDefault" to true,
            "undeletable" to false
        ),
        hashMapOf(
            "nome" to "Teclados de Acesso Físico",
            "isDefault" to true,
            "undeletable" to false
        )
    )

    // Salva cada categoria no Firestore
    defaultCategorias.forEach { category ->
        categoriasRef.add(category)
            .addOnSuccessListener {
                Log.d("Category", "Categoria padrão criada: ${category["nome"]}")
            }
            .addOnFailureListener { e ->
                Log.e("Category", "Erro ao criar categoria padrão", e)
            }
    }
}

// Função Composable responsável pela interface da tela de cadastro
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SignUp() {
    val context = LocalContext.current

    // Armazena os valores inseridos pelo usuário
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Cabeçalho visual da tela
        SuperIDHeader()

        // Botão para voltar à tela anterior
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

        // Layout principal do formulário de cadastro
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 50.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Título da tela
            Text(
                text = "CADASTRO",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            // Campo de entrada para o nome
            CustomOutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nome"
            )

            // Campo de entrada para o email
            CustomOutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email"
            )

            // Campo de entrada para a senha
            CustomOutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = "Senha",
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            // Campo de entrada para confirmar a senha
            CustomOutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirmar Senha",
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botão de ação para cadastrar o usuário
            Button(
                onClick = {
                    when {
                        // Verifica se há campos vazios
                        name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                            Toast.makeText(context, "Preencha todos os campos!", Toast.LENGTH_LONG).show()
                        }

                        // Verifica se o email contém "@"
                        "@" !in email -> {
                            Toast.makeText(context, "Email inválido!", Toast.LENGTH_LONG).show()
                        }

                        // Verifica se as senhas coincidem
                        password != confirmPassword -> {
                            Toast.makeText(context, "As senhas não coincidem!", Toast.LENGTH_LONG).show()
                        }

                        // Caso tudo esteja correto, prossegue com o cadastro
                        else -> {
                            saveUserToAuth(email, password, name, context)
                        }
                    }
                },
                modifier = Modifier
                    .height(60.dp)
                    .width(150.dp)
            ) {
                Text("Cadastrar")
            }
        }
    }
}