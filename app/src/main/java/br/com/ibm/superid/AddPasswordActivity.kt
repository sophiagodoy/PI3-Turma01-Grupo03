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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.VisualTransformation

// Declarando a Activity que exibe o formulário para adicionar uma nova senha
class AddPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ativa o modo de tela cheia com suporte à barra de status
        enableEdgeToEdge()
        // Define o conteúdo da tela usando Compose e tema personalizado
        setContent {
            SuperIDTheme {
                AddPassword()
            }
        }
    }
}

// Função que adiciona uma nova senha criptografada no Firestore
fun addNewPassword(
    context: Context,
    senha: String,
    categoria: String,
    descricao: String,
    titulo: String,
    login: String
) {

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
    val senhasRef = Firebase.firestore
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

// Lista mutável que armazenará as categorias do usuário carregadas do Firestore
val categoriasUsuario = mutableStateListOf<String>()

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
                // Se a leitura for bem-sucedida, adiciona cada nome de categoria à lista
                task.result?.documents?.forEach { doc ->
                    doc.getString("nome")?.let { categoriasUsuario.add(it) }
                }
            } else {
                // Em caso de falha, exibe mensagem com o motivo
                Toast.makeText(context, "Erro ao ler categorias: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
}

// Composable que cria o formulário para adicionar uma nova senha
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPassword(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Estados que armazenam os valores dos campos do formulário
    var login by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var titulo by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Ao iniciar o composable, busca as categorias do usuário no Firestore
    LaunchedEffect(Unit) {
        fetchCategoriasUsuario(context)
    }

    // Container principal que define fundo e organiza os elementos verticalmente

    val scrollState = rememberScrollState() //rolagem de tela

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 100.dp)
            .verticalScroll(scrollState)
    ) {

        // Cabeçalho visual personalizado do aplicativo
        SuperIDHeader()

        // Botão para voltar para a tela principal (MainActivity)
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

        // Coluna que organiza os campos do formulário centralizados e espaçados
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 50.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Título principal da tela
            Text(
                text = "ADICIONAR SENHA",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(24.dp))


            // Campo de texto para o título da senha
            CustomOutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = "Título"
            )

            // Campo de texto para o login da senha
            CustomOutlinedTextField(
                value = login,
                onValueChange = { login = it },
                label = "Login (opcional)"
            )

            // Campo de entrada para a senha
            CustomOutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                label = "Senha",
                //define se o texto vai ser visivel ou oculto
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None }
                else {
                    PasswordVisualTransformation() },
                // Define o tipo de teclado (neste caso, teclado para senha)
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            // Alterna entre o ícone de "visível" e "não visível"
                            imageVector = if (passwordVisible){
                                Icons.Default.VisibilityOff } // Icone do "olho cortado"}
                            else{
                                Icons.Default.Visibility }, // Icone do olho
                            contentDescription = if (passwordVisible){
                                "Ocultar senha" }
                            else {
                                "Mostrar senha"}
                        )
                    }
                }
            )



            // Campo de entrada para confirmar a senha
            CustomOutlinedTextField(
                value = confirmarSenha,
                onValueChange = { confirmarSenha = it },
                label = "Confirmar Senha",
                visualTransformation = if (confirmPasswordVisible)
                { VisualTransformation.None }
                else
                {PasswordVisualTransformation()},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible)
                            {Icons.Default.VisibilityOff}
                            else
                            { Icons.Default.Visibility},
                            contentDescription = if (confirmPasswordVisible)
                            {"Ocultar senha"}
                            else
                            {"Mostrar senha"}
                        )
                    }
                }
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

            // Campo para descrição opcional da senha
            CustomOutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = "Descrição (opcional)"
            )

            Spacer(Modifier.height(24.dp))

            // Botão para salvar a senha, realizando validações antes
            Button(
                onClick = {
                    when {
                        // Verifica se campos obrigatórios estão preenchidos
                        titulo.isBlank() || senha.isBlank() || categoria.isBlank() ->
                            Toast.makeText(
                                context,
                                "Preencha título, senha e categoria",
                                Toast.LENGTH_SHORT
                            ).show()

                        // Verifica se as senhas não coincidem
                        confirmarSenha != senha ->
                            Toast.makeText(context, "As senhas não conferem", Toast.LENGTH_SHORT)
                                .show()

                        // Verifica se o título tem mais de 10 caracteres
                        titulo.length > 30 ->
                            Toast.makeText(
                                context,
                                "Título não pode ter mais de 30 caracteres!",
                                Toast.LENGTH_SHORT
                            ).show()

                        // Verifica se a senha tem menos de 8 caracteres
                        senha.length < 4 ->
                            Toast.makeText(
                                context,
                                "Senha deve ter pelo menos 4 caracteres!",
                                Toast.LENGTH_SHORT
                            ).show()

                        // Se tudo válido, adiciona a nova senha e volta à tela principal
                        else -> {
                            addNewPassword(context, senha, categoria, descricao, titulo, login)
                        }
                    }
                },
                modifier = Modifier
                    .padding(
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp // faz com que o botão apareca na tela do celular
                    )
                    .height(60.dp)
                    .width(150.dp)
            ){
                Text("SALVAR")
            }
        }
    }
}

@Composable
@Preview
fun PreviewAddPassword(){
    SuperIDTheme {
        AddPassword()
    }
}

