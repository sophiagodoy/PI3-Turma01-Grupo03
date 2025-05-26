package br.com.ibm.superid

import android.app.Application
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Application que observa o ciclo de vida do app para
 * sincronizar o status de verificação de e-mail do usuário
 * no Firestore sempre que o app volta ao primeiro plano.
 */
class EmailVerificationSyncApplication : Application(), DefaultLifecycleObserver {

    /**
     * Chamado uma única vez quando o processo do app é criado.
     * Aqui registramos este objeto como observador do ciclo
     * de vida da aplicação para capturar eventos de foreground/background.
     */
    override fun onCreate() {
        super<Application>.onCreate()
        // Inicia o monitoramento do ciclo de vida para callbacks onStart/onStop
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    /**
     * Disparado sempre que o app volta ao primeiro plano (foreground).
     * Verifica se há um usuário autenticado e, caso o e-mail ainda
     * não esteja marcado como verificado, força um reload dos dados
     * no Firebase Auth e, em caso de sucesso, atualiza o campo
     * "emailVerified" no Firestore.
     */
    override fun onStart(owner: LifecycleOwner) {
        val user = Firebase.auth.currentUser

        // Se existir usuário autenticado e e-mail não verificado
        if (user != null && !user.isEmailVerified) {
            // Solicita ao Firebase Auth que recarregue os dados do usuário,
            // garantindo que o status de verificação seja atualizado
            user.reload()
                .addOnSuccessListener {
                    // Após recarregar, verifica novamente isEmailVerified
                    if (user.isEmailVerified) {
                        // Se agora estiver verificado, persiste essa informação
                        // no documento correspondente no Firestore
                        Firebase.firestore
                            .collection("users")
                            .document(user.uid)
                            .update("emailVerified", true)
                            .addOnSuccessListener {
                                Log.i("EmailSync", "Status de verificação de e-mail atualizado no Firestore.")
                            }
                            .addOnFailureListener { e ->
                                Log.e("EmailSync", "Falha ao atualizar campo emailVerified no Firestore", e)
                            }
                    } else{
                        // Se ainda não estiver, envia o e-mail de verificação
                        user.sendEmailVerification()
                    }
                }
                .addOnFailureListener { e ->
                    // Se não conseguir recarregar o usuário, registra o erro
                    Log.e("EmailSync", "Erro ao recarregar dados do usuário no Firebase Auth", e)
                }
        }
    }
}