// TELA PARA EXIBIR E GERENCIAR O PERFIL DO USUÁRIO

// Definição do pacote do aplicativo
package br.com.ibm.superid

// Importações necessárias
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import br.com.ibm.superid.ui.theme.SuperIDTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// Declarando a Activity (ProfileActivity)
class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Define o conteúdo da tela usando Compose
        setContent {
            SuperIDTheme {
                // Surface é um container básico que preenche toda a tela
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Chama a função composable ProfileScreen
                    ProfileScreen()
                }
            }
        }
    }
}

// Modelo de dados para representar o perfil do usuário
data class UserProfile(
    val name: String = "",          // Nome do usuário
    val email: String = "",         // Email do usuário
    val emailVerified: Boolean = false // Status de verificação do email
)

// Função Composable principal da tela de perfil
@Composable
fun ProfileScreen() {
    // Obtém o contexto atual para navegação e outros recursos
    val context = LocalContext.current
    // Obtém a instância do Firebase Authentication
    val auth = Firebase.auth
    // Obtém a instância do Firestore
    val db = Firebase.firestore

    // Estado para armazenar os dados do perfil do usuário
    var userProfile by remember { mutableStateOf(UserProfile()) }
    // Estado para controlar o carregamento dos dados
    var isLoading by remember { mutableStateOf(true) }

    // Efeito colateral para carregar os dados do usuário quando a tela é aberta
    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Busca os dados do usuário no Firestore
            db.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Atualiza o estado com os dados obtidos
                        userProfile = UserProfile(
                            name = document.getString("name") ?: "",
                            email = document.getString("email") ?: "",
                            emailVerified = currentUser.isEmailVerified
                        )
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        } else {
            // Se não estiver autenticado, redireciona para a tela de acesso
            context.startActivity(Intent(context, AccessOptionActivity::class.java))
        }
    }

    // Layout principal em coluna
    Column(modifier = Modifier.fillMaxSize()) {
        // Header com botão de voltar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botão de voltar para a MainActivity
            IconButton(
                onClick = {
                    context.startActivity(Intent(context, MainActivity::class.java))
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar"
                )
            }

            // Título da tela
            Text(
                text = "Meu Perfil",
                style = MaterialTheme.typography.headlineSmall
            )

            // Espaçador para centralizar o título
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Exibe um indicador de carregamento enquanto os dados são buscados
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Exibe os dados do usuário quando o carregamento estiver completo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Componente para exibir o nome do usuário
                ProfileItem(label = "Nome", value = userProfile.name)
                // Componente para exibir o email do usuário
                ProfileItem(label = "Email", value = userProfile.email)
                // Componente para exibir o status de verificação do email
                VerificationStatus(verified = userProfile.emailVerified)
            }
        }
    }
}

// Componente Composable para exibir um item do perfil
@Composable
fun ProfileItem(label: String, value: String) {
    Column {
        // Rótulo do item (ex: "Nome", "Email")
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        // Valor do item (ex: "João Silva", "joao@email.com")
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
        // Divisor visual
        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            thickness = 1.dp,  // Espessura da linha
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f) // Cor sutil
        )
    }
}

// Componente Composable para exibir o status de verificação do email
@Composable
fun VerificationStatus(verified: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Texto indicativo
        Text(
            text = "Email verificado:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        // Exibe "Sim" ou "Não" conforme o status
        if (verified) {
            Text(
                text = "Sim",
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Text(
                text = "Não",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
