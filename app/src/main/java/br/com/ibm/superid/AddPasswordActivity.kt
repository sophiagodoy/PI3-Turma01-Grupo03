// TELA PARA O USUÁRIO ADICIONAR UMA NOVA SENHA

package br.com.ibm.superid

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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.VisualTransformation
import br.com.ibm.superid.ui.theme.core.util.addNewPassword
import br.com.ibm.superid.ui.theme.core.util.fetchCategoriasUsuario

// Declarando a Activity que exibe o formulário para adicionar uma nova senha
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

// Lista mutável que armazenará as categorias do usuário carregadas do Firestore
val categoriasUsuario = mutableStateListOf<String>()

// Composable que cria o formulário para adicionar uma nova senha
@OptIn(ExperimentalMaterial3Api::class) // Para ExposedDropdownMenuBox que ainda não foi estabilizado oficialmente na biblioteca Jetpack Compose (considerados APIs experimentais)
@Preview
@Composable
fun AddPassword(modifier: Modifier = Modifier) {

    val context = LocalContext.current

    var login by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var titulo by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) } // Controla o Dropdown
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }


    // Ao iniciar o composable, busca as categorias do usuário no Firestore uma única vez (Unit)
    LaunchedEffect(Unit) {
        fetchCategoriasUsuario(context)
    }

    // Rolagem de tela
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 100.dp)
    ) {

        // Função que define o cabeçalho visual personalizado
        SuperIDHeader()

        // Seta de voltar para a MainActivity
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

        Box(
            modifier = Modifier
                .weight(1f) // ocupa o espaço restante abaixo do cabeçalho
                .verticalScroll(scrollState) // permite rolar o conteúdo se ultrapassar a tela
                .padding( // aplica margens internas

                    //“lê” exatamente quanto espaço a barra de navegação está ocupando naquela tela. Depois,
                    // somamos mais 16dp, garante uma folga extra para o usuário não sentir que o botão
                    // “Cadastrar” ou outro item fica apertado junto à base do celular.
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp
                )
        ){
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


            // Campo de texto para o título
            CustomOutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = "Título"
            )

            // Campo de texto para o login
            CustomOutlinedTextField(
                value = login,
                onValueChange = { login = it },
                label = "Login (opcional)"
            )

            // Campo de texto para a senha
            CustomOutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                label = "Senha",

                // Ocultação e exibição de senha
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None // Exibe o texto normalmente
                } else {
                    PasswordVisualTransformation() // Substitui cada caracter por "."
                },

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),

                // Alterando a visibilidade da imagem
                trailingIcon = {
                    IconButton(
                        onClick = {
                            passwordVisible = !passwordVisible
                        }
                    ) {
                        Icon(
                            imageVector = if (passwordVisible){
                                Icons.Default.Visibility // Olho aberto
                            } else {
                                Icons.Default.VisibilityOff // Olho fechado
                            },
                            contentDescription = if (passwordVisible){
                                "Ocultar senha"
                            } else {
                                "Mostrar senha"
                            }
                        )
                    }
                }
            )

            // Campo de texto para confirmação de senha
            CustomOutlinedTextField(
                value = confirmarSenha,
                onValueChange = { confirmarSenha = it },
                label = "Confirmar Senha",

                // Ocultação e exibição de senha
                visualTransformation = if (confirmPasswordVisible) {
                    VisualTransformation.None // Exibe o texto normalmente
                } else {
                    PasswordVisualTransformation() // Substitui cada caracter por "."
                },

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),

                // Alterando a visibilidade da imagem
                trailingIcon = {
                    IconButton(
                        onClick = {
                            confirmPasswordVisible = !confirmPasswordVisible
                        }
                    ) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) {
                                Icons.Default.Visibility // Olho aberto
                            } else {
                                Icons.Default.VisibilityOff // Olho fechado
                            },
                            contentDescription = if (confirmPasswordVisible) {
                                "Ocultar senha"
                            } else {
                                "Mostrar senha"
                            }
                        )
                    }
                }
            )

            // Dropdown de categorias
            // Baseado na documentação: https://developer.android.com/develop/ui/compose/components/menu?hl=pt-br
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.TopCenter
            ) {

                // Quando o usuário toca no campo categoria para abrir o dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded, // Controla se o menu está aberto ou fechado

                    // Se o dropdown for aberto
                    onExpandedChange = {
                        if (!expanded) {
                            fetchCategoriasUsuario(context) // Chama a função para atualizar as categorias do usuário
                        }
                        expanded = !expanded // Trasnforma o dropdown em true (aberto)
                    },
                    modifier = Modifier.wrapContentWidth() // Faz com que o dropdown ocupe exatamente o mesmo tamanho do CustomOutlinedTextField
                ) {

                    // Campo não editável para selecionar categoria em forma de Dropdown
                    CustomOutlinedTextField(
                        value = categoria,
                        onValueChange = { /* não edita */ },
                        label = "Categoria",

                        readOnly = true, // Não permite que o usuário digite no campo (campo apenas para leitura)
                        modifier = Modifier
                            .menuAnchor(), // Faz com que o ExposedDropdownMenu abra exatamente abaixo do CustomOutlinedTextField

                        // Exibição do ícone de seta (para cima/baixo)
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        }
                    )

                    // Menu suspenso aparece com as opções
                    ExposedDropdownMenu(
                        expanded = expanded, // Controla se o menu está aberto ou fechado
                        onDismissRequest = { expanded = false } // Caso o usuário faça qualquer ação fora do menu suspenso, ele será fechado
                    ) {

                        // Verifica se não existe nenhuma categoria no banco de dados do usuário
                        if (categoriasUsuario.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Nenhuma categoria") },
                                onClick = {
                                    expanded = false // Quando clicado em "Nenhuma categoria" apenas fecha o menu
                                }
                            )
                        } else {
                            // Caso exista categorias no banco de dados do usuário
                            categoriasUsuario.forEach { cat -> // Para cada elemento dentro de categoriasUsuario, é criada uma variável chamada cat
                                DropdownMenuItem(
                                    text = { Text(cat) }, // Exibe o texto da categoria
                                    onClick = {
                                        categoria = cat // Quando clicado na categoria, define a veriável com o valor selecionado
                                        expanded = false // Fecha o menu (agora só aparece a categoria que foi selecionada)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Campo de texto para descrição
            CustomOutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = "Descrição (opcional)"
            )

            Spacer(Modifier.height(24.dp))

            // Botão para salvar a nova senha no banco de dados Firestore
            Button(
                onClick = {
                    when {
                        // Verifica se algum dos campos está em branco (se está vazio ou apenas com espaços)
                        titulo.isBlank() || senha.isBlank() || categoria.isBlank() ->
                            Toast.makeText(context, "Preencha título, senha e categoria", Toast.LENGTH_SHORT).show()

                        // Verifica se as senhas não coincidem
                        confirmarSenha != senha ->
                            Toast.makeText(context, "As senhas não conferem", Toast.LENGTH_SHORT).show()

                        // Verifica se o título tem mais de 30 caracteres
                        titulo.length > 30 ->
                            Toast.makeText(context, "Título não pode ter mais de 30 caracteres!", Toast.LENGTH_SHORT).show()

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
}