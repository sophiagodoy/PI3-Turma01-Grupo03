package br.com.ibm.superid.ui.theme.core.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import br.com.ibm.superid.EmailVerificationActivity
import br.com.ibm.superid.MainActivity
import br.com.ibm.superid.ui.theme.core.util.createDefaultCategorias
import br.com.ibm.superid.ui.theme.core.util.saveUserToFirestore
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore

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

// Validando as credenciais do usuário para o login
fun signInWithFirebaseAuth(email: String, password: String, context: Context) {

    // Obtemos a instância do Firebase Authentication
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
                // Baseado na documentação oficial do Android: https://developer.android.com/guide/topics/ui/notifiers/toasts?hl=pt-br
                Toast.makeText(context, "Email ou senha incorreta!", Toast.LENGTH_LONG).show()
            }
        }
}


// Criando a conta do usuário no Firebase Authentication
fun saveUserToAuth(email: String, password: String, name: String, context: Context) {

    // Obtemos a instância do Firebase Authentication
    val auth = Firebase.auth

    // Tentando criar o usuário com email e senha
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->

            // Verifica se o login foi bem-sucedido
            if (task.isSuccessful) {

                // Retorna o usuário que está autenticado
                val user = auth.currentUser

                if (user != null) {

                    // Extrai o identificador único do usuário autenticado (UID) gerado pelo Firebase
                    val uid = user.uid
                    Log.i("AUTH", "Conta criada com sucesso")

                    // Envia o e-mail de verificação
                    user.sendEmailVerification()
                        .addOnCompleteListener { verifyTask ->
                            if (verifyTask.isSuccessful) {
                                Log.i("AUTH", "E-mail de verificação enviado com sucesso.")
                                Toast.makeText(context, "E-mail de verificação enviado!", Toast.LENGTH_LONG).show()
                            } else {
                                Log.i("AUTH", "Erro ao enviar e-mail de verificação.", verifyTask.exception)
                            }
                        }

                    saveUserToFirestore(uid, name, email, context)
                }

            } else {
                // Verificando se o email do usuário já está cadastrado
                // Baseado na documentação: https://firebase.google.com/docs/reference/kotlin/com/google/firebase/auth/FirebaseAuthUserCollisionException

                // Pegando o erro que aconteceu durante o cadastro
                val exception = task.exception

                // Verficando se o erro é porque o email já está cadastrado no banco
                if (exception is FirebaseAuthUserCollisionException) {
                    Toast.makeText(context, "Este e-mail já está em uso, faça login!", Toast.LENGTH_LONG).show()
                }
            }
        }
}

// Salva os dados do usuário no Firestore
@SuppressLint("HardwareIds")
fun saveUserToFirestore(uid: String, name: String, email: String, context: Context) {

    // Obtém a instância do Firestore
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

    // Salva os dados no documento cujo ID é o UID do usuário
    db.collection("users") // Acesso a coleção users
        .document(uid) // Crio o ducumento cujo ID é a variável uid
        .set(dadosCadastro) // Gravo os campos definidos no mapa dadosCadastro

        .addOnCompleteListener { task ->

            if (task.isSuccessful) {
                createDefaultCategorias(uid, context)
                Toast.makeText(context, "Cadastro realizado com sucesso!", Toast.LENGTH_LONG).show()
                context.startActivity(Intent(context, EmailVerificationActivity::class.java))
            }

            else {
                Toast.makeText(context, "Erro ao salvar dados do usuário", Toast.LENGTH_LONG).show()
            }
        }
}

// Cria categorias padrão para novos usuários no Firestore
fun createDefaultCategorias(userId: String, context: Context) {

    // Obtém a instância do Firestore
    val db = Firebase.firestore

    val categoriasRef =
        db.collection("users") // Acesso a coleção users
            .document(userId) // Acesso o documento com o ID do usuário
            .collection("categorias") // Acesso a coleção categorias

    // Crio uma lista com três categorias
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
            "nome" to "Teclados Físico",
            "isDefault" to true,
            "undeletable" to false
        )
    )

    // Para cada mapa de categoria na lista...
    defaultCategorias.forEach { category ->

        // Adiciona um novo documento em categorias
        categoriasRef.add(category)
            .addOnSuccessListener {
                Log.i("Category", "Categoria padrão criada: ${category["nome"]}")
            }

            // Se ocorrer algum erro
            .addOnFailureListener { e ->
                Log.i("Category", "Erro ao criar categoria padrão", e)
            }
    }
}