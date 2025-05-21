// TELA PARA O USUÁRIO ALTERAR UMA SENHA

// Definição do pacote aplicativo
package br.com.ibm.superid

// Importações necessárias
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ibm.superid.ui.theme.SuperIDTheme
import br.com.ibm.superid.ui.theme.core.util.CustomOutlinedTextField
import br.com.ibm.superid.ui.theme.core.util.SuperIDHeader
import br.com.ibm.superid.ui.theme.core.util.encryptpassword
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

// Declarando a Activity (ChangePasswordActivity)
class ChangePasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Extrai os dados que enviamos
        val passwordId       = intent.getStringExtra("PASSWORD_ID")       ?: ""
        val initialSenha     = intent.getStringExtra("PASSWORD_VALUE")    ?: ""
        val initialCategoria = intent.getStringExtra("PASSWORD_CATEGORY") ?: ""
        val initialDescricao = intent.getStringExtra("PASSWORD_DESCRIPTION") ?: ""
        val initialTitulo    = intent.getStringExtra("PASSWORD_TITLE")    ?: ""
        val initialLogin     = intent.getStringExtra("PASSWORD_LOGIN")    ?: ""

        setContent {
            SuperIDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Chama a função composable ChangePassword e aplica o padding interno do Scaffold
                    ChangePassword(
                        modifier = Modifier.padding(innerPadding),
                        // Passa os valores iniciais
                        passwordId       = passwordId,
                        initialTitulo    = initialTitulo,
                        initialLogin     = initialLogin,
                        initialSenha     = initialSenha,
                        initialDescricao = initialDescricao,
                        initialCategoria = initialCategoria,
                    )

                }
            }
        }
    }
}



// Função Composable que apresenta o formulário de adicionar senha
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePassword(
    passwordId: String,
    initialTitulo: String,
    initialLogin: String,
    initialSenha: String,
    initialCategoria: String,
    initialDescricao: String,
    modifier: Modifier = Modifier) {

    // Cria variável para poder trocar de tela
    val context = LocalContext.current

    // Variáveis que guardam o valor digitado nos campos do formulário
    var titulo by remember { mutableStateOf(initialTitulo) }
    var login by remember { mutableStateOf(initialLogin) }
    var senha by remember { mutableStateOf(initialSenha) }
    var categoria by remember { mutableStateOf(initialCategoria) }
    var descricao by remember { mutableStateOf(initialDescricao) }
    var expanded by remember { mutableStateOf(false) }

    // Seta que volta para AccessOptionActivity
    // Baseado em: https://developer.android.com/develop/ui/compose/components/app-bars?hl=pt-br#top-app-bar
    // Baseado em: https://alexzh.com/visual-guide-to-topappbar-variants-in-jetpack-compose/?utm_source=chatgpt.com

    Column (modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        // Cabeçalho visual personalizado
        SuperIDHeader()

        // Botão de voltar
        IconButton(
            onClick = {
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                modifier = Modifier.size(35.dp)
            )
        }

        // Layout em coluna que ocupa toda a tela e aplica padding de 16dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 70.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Define o título da tela em negrito e tamanho 30sp
            Text(
                text = "ALTERAR: ${initialTitulo.uppercase()}",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            // Espaço de 24dp abaixo do título
            Spacer(Modifier.height(24.dp))

            CustomOutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label ="Titulo"
            )

            CustomOutlinedTextField(
                value = login,
                onValueChange = { login = it },
                label ="Login (opcional)"
            )

            // Campo de texto para digitar a nova senha
            CustomOutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                label = "Senha",
                /*// Esconde os caracteres da senha
                visualTransformation = PasswordVisualTransformation(),*/
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = {

                        if (!expanded) {
                            fetchCategoriasUsuario(context)
                        }
                        expanded = !expanded },
                    modifier = Modifier.wrapContentWidth()
                ) {

                    CustomOutlinedTextField(
                        value = categoria,
                        onValueChange = { /* não edita */ },
                        label = "Categoria",
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        if (categoriasUsuario.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Nenhuma categoria") },
                                onClick = { expanded = false }
                            )
                        } else {
                            categoriasUsuario.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        categoria = cat
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Campo de texto para digitar a descrição da nova senha
            CustomOutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label ="Descrição (opcional)"
            )

            // Espaço de 24dp antes do botão
            Spacer(Modifier.height(24.dp))

            // Botão que quando clicado salva a nova senha no banco Firestore
            Button(
                onClick = {
                    // Chama nossa função de atualização
                    updatePassword(
                        context      = context,
                        documentId   = passwordId,
                        newTitulo    = titulo,
                        newLogin     = login,
                        newPassword  = senha,
                        newCategory  = categoria,
                        newDesc      = descricao
                    )
                },
                modifier = Modifier
                    .height(60.dp)    
                    .width(150.dp)
            ) {
                // Define o texto que está dentro do botão
                Text("Salvar")
            }
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


@Preview(showSystemUi = true, showBackground = true)
@Composable
fun ChangePasswordPreview() {
    SuperIDTheme {
        ChangePassword(
            passwordId = "id_teste",
            initialTitulo = "Minha Conta",
            initialLogin = "teste@gmail.com",
            initialSenha = "123456",
            initialCategoria = "Email",
            initialDescricao = "Conta pessoal do Gmail"
        )
    }
}