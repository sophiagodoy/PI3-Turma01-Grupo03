package br.com.ibm.superid.ui.theme.core.util

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import br.com.ibm.superid.MainActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.auth

fun reauthenticateUser(password: String, context: Context, onSuccess: () -> Unit, onFailure: () -> Unit) {
    val user = Firebase.auth.currentUser
    val email = user?.email

    if (user != null && email != null) {
        val credential = EmailAuthProvider.getCredential(email, password)

        user.reauthenticate(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("AUTH", "Reautenticado com sucesso.")
                    onSuccess()
                } else {
                    Log.i("AUTH", "Falha na reautenticação", task.exception)
                    Toast.makeText(context, "Senha incorreta!", Toast.LENGTH_LONG).show()
                    onFailure()
                }
            }
    }
}

// Validando as credenciais do usuário
fun signInWithFirebaseAuth(email: String, password: String, context: Context) {
    // Obtemos a instância do Firebase Auth
    val auth = Firebase.auth

    // Tantando realizar o login
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->

            // Verifica se o login foi bem-sucedido
            if (task.isSuccessful) {
                // Guardo o usário autenticado
                val user = task.result.user

                Log.i("AUTH", "Login realizado com sucesso. UID: ${user?.uid}")

                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            } else {

                Log.i("AUTH", "Falha ao fazer login.", task.exception)

                // Exibe uma mensagem Toast
                // Baseado na documentação oficial do Android: https://developer.android.com/guide/topics/ui/notifiers/toasts?hl=pt-br
                Toast.makeText(context, "Email ou senha incorreta!", Toast.LENGTH_LONG).show()
            }
        }
}

