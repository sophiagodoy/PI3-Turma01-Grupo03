// TELA PARA O USUÁRIO REALIZAR O CADASTRO

package br.com.ibm.superid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import br.com.ibm.superid.ui.theme.core.util.saveUserToAuth

// Declarando a Activity para realizar o cadastro do usuário
class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                SignUpPreview()
            }
        }
    }
}

// Classe de dados que reúne os resultados da validação de senha
data class PasswordCriteria(
    val hasUppercase: Boolean, // contém pelo menos uma letra maiúscula?
    val hasLowercase: Boolean, // contém pelo menos uma letra minúscula?
    val hasDigit: Boolean, // contém ao menos um dígito numérico?
    val hasSpecialChar: Boolean, // contém ao menos um caractere especial?
    val hasMinLength: Boolean // possui tamanho mínimo de 8 caracteres?
)

// Função que valida a senha com base em critérios
fun checkPasswordCriteria(password: String): PasswordCriteria {
    return PasswordCriteria(
        hasUppercase = password.any { it.isUpperCase() },
        hasLowercase = password.any { it.isLowerCase() },
        hasDigit = password.any { it.isDigit() },
        hasSpecialChar = password.any { "!@#\$%^&*()_+-=[]|,.<>?".contains(it) },
        hasMinLength = password.length >= 8
    )
}

// Composable que exibe cada regra com ✅ ou ❌
@Composable
fun PasswordRequirementItem(text: String, isMet: Boolean) {
    Text(
        text = (if (isMet) "✅ " else "❌ ") + text,

        color = if (isMet) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.error
        },

        fontSize = 12.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

// Função responsável pela interface da tela de cadastro
@Composable
fun SignUp() {

    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val passwordCriteria = checkPasswordCriteria(password)

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // Cabeçalho visual personalizado
        SuperIDHeader()

        // Seta de voltar
        IconButton(
            onClick = {
                val intent = Intent(context, AccessOptionActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar"
            )
        }

        Box(
            modifier = Modifier
                .weight(1f) // ocupa o espaço restante abaixo do cabeçalho
                .verticalScroll(scrollState) // permite rolar o conteúdo se ultrapassar a tela
                .padding( // aplica margens internas
                    top = 50.dp, // afasta o formulário do topo (abaixo do cabeçalho)

                    //“lê” exatamente quanto espaço a barra de navegação está ocupando naquela tela. Depois,
                    // somamos mais 16dp, garante uma folga extra para o usuário não sentir que o botão
                    // “Cadastrar” ou outro item fica apertado junto à base do celular.
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp,
                )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally,   // ← aqui, dentro dos parênteses do Column
                verticalArrangement = Arrangement.Top                // ← se precisar, também dentro dos parênteses
            ) {

                // Define o título da tela
                Text(
                    text = "CADASTRO",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )

                // Campo para digitar o nome
                CustomOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nome Completo"
                )

                // Campo para digitar o email
                CustomOutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email"
                )

                // Campo para digitar a senha
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
                                } else {
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

                // Campo para digitar a confirmação de senha
                CustomOutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirmar Senha",

                    // Ocultação e exibição de senha
                    visualTransformation = if (confirmPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
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

                Column(
                    modifier = Modifier
                        .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 8.dp)
                ) {

                    Text(
                        "A senha deve conter:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Exibir cada critério de validação de senha na tela
                    PasswordRequirementItem(
                        "Pelo menos 8 caracteres",
                        passwordCriteria.hasMinLength
                    )
                    Spacer(modifier = Modifier.height(2.dp))

                    PasswordRequirementItem("Letra maiúscula (A-Z)", passwordCriteria.hasUppercase)
                    Spacer(modifier = Modifier.height(2.dp))

                    PasswordRequirementItem("Letra minúscula (a-z)", passwordCriteria.hasLowercase)
                    Spacer(modifier = Modifier.height(2.dp))

                    PasswordRequirementItem("Número (0-9)", passwordCriteria.hasDigit)
                    Spacer(modifier = Modifier.height(2.dp))

                    PasswordRequirementItem(
                        "Caractere especial (!@#...)",
                        passwordCriteria.hasSpecialChar
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botão de ação para cadastrar o usuário
                Button(
                    onClick = {
                        when {
                            // Verifica se algum campo está em branco (se está vazio ou apenas com espaços)
                            name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                                Toast.makeText(context, "Preencha todos os campos!", Toast.LENGTH_LONG).show()
                            }

                            // Verifica se o email contém "@"
                            "@" !in email -> {
                                Toast.makeText(context, "Email inválido!", Toast.LENGTH_LONG).show()
                            }

                            // Verifica se as senhas coincidem
                            password != confirmPassword -> {
                                Toast.makeText(context, "As senhas não coincidem!", Toast.LENGTH_LONG).show()
                            }

                            // Verifica se a senha atende a todos os critérios
                            !passwordCriteria.hasMinLength ||
                                    !passwordCriteria.hasUppercase ||
                                    !passwordCriteria.hasLowercase ||
                                    !passwordCriteria.hasDigit ||
                                    !passwordCriteria.hasSpecialChar -> {
                                Toast.makeText(context, "A senha não atende aos requisitos mínimos!", Toast.LENGTH_LONG).show()
                            }

                            else -> {
                                saveUserToAuth(email, password, name, context)
                            }
                        }
                    },

                    modifier = Modifier
                        .height(60.dp)
                        .width(150.dp)
                ) {
                    Text("Cadastrar")
                }
            }
        }
    }
}

@Preview
@Composable
fun SignUpPreview(){
    SuperIDTheme {
        SignUp()
    }
}
