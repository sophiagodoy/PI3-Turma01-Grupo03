package com.example.experimentofirebase

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.experimentofirebase.ui.theme.ExperimentoFirebaseTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExperimentoFirebaseTheme {
                SignUpScreen(
                    onBackPressed = { finish() },
                    onSignUpComplete = {
                        startActivity(Intent(this, SignInActivity::class.java))
                        finish()
                    },
                    onError = { error ->
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
    onSignUpComplete: () -> Unit,
    onError: (String) -> Unit = {}
) {
    val nome = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val senha = remember { mutableStateOf("") }
    val rg = remember { mutableStateOf("") }
    val cpf = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(false) }
    // Seta pra voltar para WelcomeActivity
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Criar Conta") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Nome
            OutlinedTextField(
                value = nome.value,
                onValueChange = { nome.value = it },
                label = { Text("Nome Completo") },
                modifier = Modifier.fillMaxWidth()
            )
            // E-Mail
            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("E-mail") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            // Senha
            OutlinedTextField(
                value = senha.value,
                onValueChange = { senha.value = it },
                label = { Text("Senha (mínimo 6 caracteres)") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            // RG
            OutlinedTextField(
                value = rg.value,
                onValueChange = { rg.value = it },
                label = { Text("RG (apenas números)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            // CPF
            OutlinedTextField(
                value = cpf.value,
                onValueChange = {
                    if (it.length <= 11) cpf.value = it
                },
                label = { Text("CPF (apenas números)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            // Botão de cadastro
            Button(
                onClick = {
                    if (email.value.isBlank() || senha.value.isBlank()) {
                        onError("Preencha e-mail e senha")
                        return@Button
                    }
                    if (senha.value.length < 6) {
                        onError("A senha deve ter pelo menos 6 caracteres")
                        return@Button
                    }
                    isLoading.value = true
                    // Insere o dado no banco de dados
                    Firebase.auth.createUserWithEmailAndPassword(email.value, senha.value)
                        .addOnCompleteListener { authTask ->
                            if (authTask.isSuccessful) {
                                val userId = authTask.result.user?.uid ?: run {
                                    onError("Erro ao obter ID do usuário")
                                    isLoading.value = false
                                    return@addOnCompleteListener
                                }
                                val userData = hashMapOf(
                                    "nome" to nome.value,
                                    "email" to email.value,
                                    "rg" to rg.value,
                                    "cpf" to cpf.value,
                                    "createdAt" to FieldValue.serverTimestamp()
                                )
                                Firebase.firestore.collection("users")
                                    .document(userId)
                                    .set(userData)
                                    .addOnSuccessListener {
                                        onSignUpComplete()
                                    }
                                    .addOnFailureListener { dbError ->
                                        onError("Erro ao salvar dados: ${dbError.message}")
                                    }
                            } else {
                                onError("Erro no cadastro: ${authTask.exception?.message}")
                            }
                            isLoading.value = false
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                enabled = !isLoading.value
            ) {
                if (isLoading.value) {
                    CircularProgressIndicator()
                } else {
                    Text("Criar Minha Conta")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    ExperimentoFirebaseTheme {
        SignUpScreen(
            onBackPressed = {},
            onSignUpComplete = {},
            onError = {}
        )
    }
}