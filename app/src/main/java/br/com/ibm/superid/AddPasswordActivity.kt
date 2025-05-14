// TELA PARA O USUÁRIO ADICIONAR UMA NOVA SENHA

package br.com.ibm.superid

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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ibm.superid.ui.theme.SuperIDTheme
import br.com.ibm.superid.ui.theme.core.util.CustomOutlinedTextField
import br.com.ibm.superid.ui.theme.core.util.SuperIDHeader
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import br.com.ibm.superid.ui.theme.core.util.createacesstoken
import br.com.ibm.superid.ui.theme.core.util.encryptpassword

// Declarando a Activity (AddPasswordActivity)
class AddPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                AddPassword()
            }
        }
    }
}

// Função para adicionar uma nova senha no Firestore
fun addNewPassword(
    context: Context,
    senha: String,
    categoria: String,
    descricao: String,
    titulo: String
) {

    // Obtenho a instância do FirebaseAuth e pego o usuário logado no momento
    val user = Firebase.auth.currentUser

    // Caso não exista um usuário logado interrompe a execução da função
    if (user == null) {
        Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_LONG).show()
        return
    }

    // Só “try” na criptografia
    val (encrypted, iv) = try {
        encryptpassword(senha)
    } catch (e: Exception) {
        Toast.makeText(context, "Falha ao criptografar: ${e.message}", Toast.LENGTH_LONG).show()
        return
    }

    val accessToken = createacesstoken()

    // Criando um mapa mutável (hashMap) com informações do que quero salvar no firestore (informações da nova senha e criptografia)
    val dadosNovaSenha = hashMapOf(
        "titulo" to titulo,
        "senha" to encrypted,
        "categoria" to categoria,
        "descricao" to descricao,
        "accessToken" to accessToken,
        "iv" to iv
    )

    // Gravando os dados no banco Firestore
    Firebase.firestore
        .collection("users")
        .document(user.uid)
        .collection("senhas")
        .add(dadosNovaSenha)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Senha salva com sucesso!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Erro ao salvar: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
}

// Crio uma lista mutável que guarda as categorias lidas no Firestore
val categoriasUsuario = mutableStateListOf<String>()

// Função que lê as categorias que estão salvas no banco do usuário
fun fetchCategoriasUsuario(context: Context) {

    // Obtenho a instância do FirebaseAuth e pego o usuário logado no momento
    val user = Firebase.auth.currentUser

    // Caso não exista um usuário logado interrompe a execução da função
    if (user == null) {
        Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_LONG).show()
        return
    }

    // Acesso a propriedade uid do objeto user (pego o uid do usuário logado)
    val uid = user.uid

    // Limpo a lista categoriasUsuario
    categoriasUsuario.clear()

    // Fazendo a leitura da coleção categorias no Firestore
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


// Função Composable que apresenta o formulário de adicionar senha
@Preview
@Composable
fun AddPassword(modifier: Modifier = Modifier) {

    // Obtém o Contexto atual da Activity para usar em Toasts, Intents...
    val context = LocalContext.current

    // Declarando variáveis que guardam os valores digitados nos campos
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var titulo by remember { mutableStateOf("") }

    // Variável que controla se o DropdownMenu está aberto ou fechado
    var expanded by remember { mutableStateOf(false) }

    // Column para definir fundo, posicionar cabeçalho e botão de voltar
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // Cabeçalho visual personalizado
        SuperIDHeader()

        // Seta que volta para MainActivity
        // Baseado na documentação: https://developer.android.com/develop/ui/compose/components/app-bars?hl=pt-br#top-app-bar
        // Baseado na documentação: https://alexzh.com/visual-guide-to-topappbar-variants-in-jetpack-compose/?utm_source
        IconButton(
            onClick = {
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar para a tela principal",
                modifier = Modifier.size(35.dp)
            )
        }

        // Column que organiza os campos que permite o usuário adicionar a nova senha
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 50.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Define o título da tela
            Text(
                text = "ADICIONAR SENHA",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(24.dp))

            // Campo de texto para escolher a categoria da nova senha
            CustomOutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = "Título"
            )

            // Campo de texto para digitar a nova senha
            CustomOutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                label = "Senha",
                // Esconde os caracteres da senha
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )

            // Campo de texto para confirmar a senha digitada
            CustomOutlinedTextField(
                value = confirmarSenha,
                onValueChange = { confirmarSenha = it },
                label = "Confirmar senha",
                // Esconde os caracteres da senha
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )

            Box(
                modifier = Modifier
            ) {

                // Campo que exibe a categoria selecionada
                CustomOutlinedTextField(
                    value = categoria,
                    onValueChange = { /* não edita */ },
                    label = "Categoria"
                )

                // Ícone do “drop-down”
                IconButton(
                    onClick = {
                        fetchCategoriasUsuario(context)
                        expanded = true
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                ) {
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = "Escolher categoria"
                    )
                }

                // 3) O DropdownMenu logo abaixo, igual antes:
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },

                    // TODO: ARRUMAR TAMANHO DO DROP DOWN AQUI
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
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

            // Campo de texto para digitar a descrição da nova senha
            CustomOutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = "Descrição (opcional)"
            )

            Spacer(Modifier.height(24.dp))

            // Botão que quando clicado salva a nova senha no banco Firestore
            Button(
                onClick = {
                    when {
                        // Verfica se titulo, senha e categoria estão em branco
                        titulo.isBlank() || senha.isBlank() || categoria.isBlank() ->
                            Toast.makeText(context, "Preencha título, senha e categoria", Toast.LENGTH_SHORT).show()

                        // Verfica se as senhas são diferentes
                        confirmarSenha != senha ->
                            Toast.makeText(context, "As senhas não conferem", Toast.LENGTH_SHORT).show()

                        // Verfica se senha tem mais de 10 caracteres
                        // Baseado na documentação: https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/
                        titulo.length > 10 ->
                            Toast.makeText(context, "Título não pode ter mais de 10 caracteres!", Toast.LENGTH_SHORT).show()

                        // Verfica se descrição está em branco e se tem mais de 15 caracteres
                        descricao.isNotBlank() && descricao.length > 15 ->
                            Toast.makeText(context, "Descrição não pode ter mais de 15 caracteres!", Toast.LENGTH_SHORT).show()

                        else -> {
                            addNewPassword(context, senha, categoria, descricao, titulo)
                            context.startActivity(Intent(context, MainActivity::class.java))
                        }
                    }
                },
                modifier = Modifier
                    .height(60.dp)
                    .width(150.dp)
                    .align(Alignment.CenterHorizontally),
            ) {
                Text("Salvar")
            }
        }
    }
}