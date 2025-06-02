package br.com.ibm.superid.ui.theme.core.util

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import br.com.ibm.superid.EmailVerificationActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Funções utilitárias para lidar com verificação de e-mail do Firebase.
 */

fun resendVerificationEmail(context: Context) {
    val user = Firebase.auth.currentUser
    if (user != null && !user.isEmailVerified) {
        user.sendEmailVerification()
    }
}

fun checkEmailVerified(
    uid: String,
    onResult: (Boolean) -> Unit
) {
    //  faz a consulta no Firestore
    Firebase.firestore
        .collection("users")
        .document(uid)
        .get()
        .addOnSuccessListener { doc ->
            val verified = doc?.getBoolean("emailVerified") == true
            onResult(verified) // callback passa true ou false
        }
        .addOnFailureListener {
            onResult(false) // em caso de erro
        }
}

fun checkEmailVerification(email: String, context: Context, onSuccess: () -> Unit){
    val db = Firebase.firestore

    db.collection("users")
        .whereEqualTo("email", email)
        .get()
        .addOnSuccessListener { result ->
            if ( result.documents.isNotEmpty()) {
                // Pega o primeiro documento que contenha esse email
                val userDoc =  result.documents[0]
                // Lê o campo "emailVerified" (ou false, se não existir)
                val isVerified = userDoc.getBoolean("emailVerified") ?: false

                if (isVerified) {
                    // E-mail verificado: prossegue com o envio do link
                    sendEmail(email, context)
                    onSuccess()
                } else {
                    // Ainda não confirmou o e-mail
                    Toast.makeText(
                        context,
                        "Por favor, verifique seu e-mail antes de redefinir a senha.", Toast.LENGTH_LONG).show()
                    val intent = Intent(context, EmailVerificationActivity::class.java)
                    context.startActivity(intent)
                }
            } else {
                // Nenhum usuário encontrado com esse e-mail
                Toast.makeText(context, "E-mail não encontrado. Cadastre-se antes de tentar redefinir a senha.", Toast.LENGTH_LONG).show()
            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Erro ao verificar o status de verificação: ${e.message}", Toast.LENGTH_LONG).show()
        }
}


fun sendEmail(email: String, context: Context ){

    Firebase.auth.sendPasswordResetEmail(email)
        .addOnSuccessListener {
            Log.d(TAG, "Email enviado para $email.")
            Toast.makeText(
                context, "Link de redefinição enviado ", Toast.LENGTH_LONG).show()
            /*
            dps levará a pessoa pra outra tela pra redefinir a senha
             */
        }
        .addOnFailureListener{ e ->
            Log.e(TAG, "Erro no envio do email para $email", e)
            Toast.makeText(context, "Falha ao enviar link de redefinição: ${e.message}", Toast.LENGTH_LONG).show()

        }
}


fun sendPasswordResetEmailByEmail(context: Context, email: String) {
    if (email.isBlank()) {
        Toast.makeText(context, "E-mail inválido", Toast.LENGTH_LONG).show()
        return
    }

    // Usa FirebaseAuth para enviar o e-mail de redefinição
    FirebaseAuth.getInstance()
        .sendPasswordResetEmail(email)
        .addOnSuccessListener {
            Toast.makeText(context, "E-mail de redefinição enviado para $email", Toast.LENGTH_LONG).show()
        }
        .addOnFailureListener { e ->
            // Pode ser erro de formato de e-mail ou usuário não cadastrado, etc.
            Toast.makeText(context, "Falha ao reenviar: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
}

