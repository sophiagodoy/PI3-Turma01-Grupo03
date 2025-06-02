// TELA PARA O USUÁRIO ALTERAR UMA SENHA

package br.com.ibm.superid

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ibm.superid.ui.theme.SuperIDTheme
import br.com.ibm.superid.ui.theme.core.util.CustomOutlinedTextField
import br.com.ibm.superid.ui.theme.core.util.SuperIDHeader
import br.com.ibm.superid.ui.theme.core.util.fetchCategoriasUsuario
import br.com.ibm.superid.ui.theme.core.util.updatePassword

// Declarando a Activity que exibe o formulário alterar a senha
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

// Função que apresenta o formulário de adicionar senha
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
    var password by remember { mutableStateOf(initialSenha) }
    var categoria by remember { mutableStateOf(initialCategoria) }
    var descricao by remember { mutableStateOf(initialDescricao) }
    var expanded by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Rolagem de tela
    val scrollState = rememberScrollState()

    Column (modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {

        // Função que define o cabeçalho visual personalizado
        SuperIDHeader()

        // Seta de voltar para a MainActivity
        // Baseado em: https://developer.android.com/develop/ui/compose/components/app-bars?hl=pt-br#top-app-bar
        // Baseado em: https://alexzh.com/visual-guide-to-topappbar-variants-in-jetpack-compose/
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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 70.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Título principal da tela com o titulo da senha que será alterada em letra maiúscula
                Text(
                    text = "ALTERAR: ${initialTitulo.uppercase()}",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(24.dp))

                // Campo de texto para o título
                CustomOutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = "Titulo"
                )

                // Campo de texto para o login
                CustomOutlinedTextField(
                    value = login,
                    onValueChange = { login = it },
                    label = "Login (opcional)"
                )

                // Campo de texto para senha
                CustomOutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
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
                                imageVector = if (passwordVisible) {
                                    Icons.Default.Visibility // Olho aberto
                                }
                                else {
                                    Icons.Default.VisibilityOff // Olho fechado
                                },
                                contentDescription = if (passwordVisible) {
                                    "Ocultar senha"
                                } else {
                                    "Mostrar senha"
                                }
                            )
                        }
                    }
                )

                // Dropdown de categorias
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
                                    onClick = { expanded = false } // Quando clicado em "Nenhuma categoria" apenas fecha o menu
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

                // Campo de texto para a descrição
                CustomOutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = "Descrição (opcional)"
                )

                Spacer(Modifier.height(24.dp))

                // Botão que quando clicado salva a nova senha no banco de dados Firestore
                Button(
                    onClick = {
                        // Chama nossa função de atualização
                        updatePassword(
                            context = context,
                            documentId = passwordId,
                            newTitulo = titulo,
                            newLogin = login,
                            newPassword = password,
                            newCategory = categoria,
                            newDesc = descricao
                        )
                    },
                    modifier = Modifier
                        .height(60.dp)
                        .width(150.dp)
                ) {
                    Text("Salvar")
                }
            }
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