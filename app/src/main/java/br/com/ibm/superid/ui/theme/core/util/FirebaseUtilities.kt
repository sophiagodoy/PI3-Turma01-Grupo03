package br.com.ibm.superid.ui.theme.core.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import br.com.ibm.superid.EmailVerificationActivity
import br.com.ibm.superid.MainActivity
import br.com.ibm.superid.SenhaItem
import br.com.ibm.superid.categoriasUsuario
import br.com.ibm.superid.ui.theme.core.util.createDefaultCategorias
import br.com.ibm.superid.ui.theme.core.util.saveUserToFirestore
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
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

// Adiciona uma nova categoria no Firestore
fun addNewCategory(context: Context, categoryName: String) {

    // Obtém o usuário autenticado
    val user = Firebase.auth.currentUser

    if (user == null) {
        Toast.makeText(context, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
        return
    }

    // Obtém a instância do Firestore
    val db = Firebase.firestore

    val categoriasRef =
        db.collection("users") // Acessa a coleção users
        .document(user.uid) // Dentro de users seleciona o documento que contém o usuário atual
        .collection("categorias") // Aponta para a coleção categorias

    // Verifica se já existe uma categoria com o mesmo nome
    categoriasRef
        .whereEqualTo("nome", categoryName) // Filtrando documentos cujo campo "nome" seja igual ao texto informado
        .get() // Executa essa consulta

        .addOnSuccessListener { documents ->

            if (!documents.isEmpty) {
                Toast.makeText(context, "Categoria já existe!", Toast.LENGTH_SHORT).show()
            }

            else {
                // Cria uma nova categoria com os campos padrão
                val novaCategoria = hashMapOf(
                    "nome" to categoryName,
                    "isDefault" to false,
                    "undeletable" to false
                )

                // Adiciona a nova categoria no Firestore
                categoriasRef
                    .add(novaCategoria)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "✅ Categoria adicionada com sucesso!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Erro ao adicionar categoria: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        .addOnFailureListener { exception ->
            Toast.makeText(context, "Erro ao verificar categoria: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
}

// Função que adiciona uma nova senha criptografada no Firestore
fun addNewPassword(context: Context, senha: String, categoria: String, descricao: String, titulo: String, login: String) {

    // Obtém a instância de autenticação do Firebase
    val auth = Firebase.auth
    val user = auth.currentUser

    // Verifica se o usuário está logado
    if (user == null) {
        Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_LONG).show()
        return
    }

    // Criptografa a nova senha
    val (encrypted, iv) = try {
        encryptpassword(senha)
    } catch (e: Exception) {
        Toast.makeText(context, "Falha ao criptografar: ${e.message}", Toast.LENGTH_LONG).show()
        return
    }

    // Gera um token de acesso exclusivo para essa senha
    val accessToken = createacesstoken()

    // Cria um mapa com os dados que serão salvos no Firestore
    val dadosNovaSenha = hashMapOf(
        "titulo" to titulo,
        "login" to login,
        "senha" to encrypted,
        "categoria" to categoria,
        "descricao" to descricao,
        "accessToken" to accessToken,
        "iv" to iv
    )

    // Referência para a subcoleção "senhas" dentro do usuário logado
    val senhasRef = com.google.firebase.ktx.Firebase.firestore
        .collection("users")
        .document(user.uid)
        .collection("senhas")

    // Verifica se já existe uma senha com o mesmo título e categoria
    senhasRef
        .whereEqualTo("titulo", titulo)
        .whereEqualTo("categoria", categoria)
        .get()
        .addOnSuccessListener { document ->
            if (!document.isEmpty) {
                Toast.makeText(context, "Já existe uma senha com esse título nessa categoria!", Toast.LENGTH_LONG).show()
            } else {
                senhasRef
                    .add(dadosNovaSenha)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Senha salva com sucesso!", Toast.LENGTH_SHORT).show()

                            val intent = Intent(context, MainActivity::class.java)
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "Erro ao salvar: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
        .addOnFailureListener { exception ->
            Toast.makeText(context, "Erro ao verificar duplicidade: ${exception.message}", Toast.LENGTH_LONG).show()
        }
}

// Função que busca as categorias salvas no Firestore para o usuário atual
fun fetchCategoriasUsuario(context: Context) {

    // Obtém o usuário atualmente autenticado
    val user = Firebase.auth.currentUser

    // Se não houver usuário logado, exibe mensagem e interrompe a função
    if (user == null) {
        Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_LONG).show()
        return
    }

    // Pega o UID do usuário autenticado
    val uid = user.uid

    // Limpa a lista antes de buscar dados novos para evitar duplicação
    categoriasUsuario.clear()

    // Realiza a leitura da coleção "categorias" do usuário no Firestore
    Firebase.firestore
        .collection("users")
        .document(uid)
        .collection("categorias")
        .get()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.documents?.forEach { doc ->
                    doc.getString("nome")?.let { categoriasUsuario.add(it) }
                }
            } else {
                Toast.makeText(context, "Erro ao ler categorias: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
}

fun updatePassword(
    context: Context,
    documentId: String,
    newTitulo:  String,
    newLogin:  String,
    newPassword: String,
    newCategory: String,
    newDesc: String
) {
    val user = Firebase.auth.currentUser
    if (user == null) {
        Toast.makeText(context, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
        return
    }
    if (newPassword.isBlank() || newTitulo.isBlank() || newCategory.isBlank()) {
        Toast.makeText(context, "Título, senha e categoria são obrigatórios!", Toast.LENGTH_SHORT).show()
        return
    }

    // Encripta a nova senha
    val (encrypted, iv) = encryptpassword(newPassword)

    // Prepara o map pra atualizar
    val updates = mapOf(
        "senha" to encrypted,
        "iv" to iv,
        "titulo" to newTitulo,
        "login" to newLogin,
        "categoria" to newCategory,
        "descricao" to newDesc
    )


    if (newTitulo.isNotBlank() && newPassword.isNotBlank() && newCategory.isNotBlank()) {
        // Executa o update no Firestore
        Firebase.firestore
            .collection("users")
            .document(user.uid)
            .collection("senhas")
            .document(documentId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(context, "Senha atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                context.startActivity(Intent(context, MainActivity::class.java))
            }

            .addOnFailureListener { e ->
                Toast.makeText(context, "Erro ao atualizar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}

fun deletePasswordById(senhaId: String) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    if (user != null) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(user.uid)
            .collection("senhas")
            .document(senhaId)
            .delete()
    }
}



