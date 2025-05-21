package br.com.ibm.superid.ui.theme.core.util

import android.content.Context
import android.util.Log
import android.widget.Toast
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

