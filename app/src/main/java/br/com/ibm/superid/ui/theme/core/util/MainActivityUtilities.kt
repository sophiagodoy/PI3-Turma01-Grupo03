package br.com.ibm.superid.ui.theme.core.util

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import br.com.ibm.superid.ChangePasswordActivity
import br.com.ibm.superid.SenhaItem
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// Pop-up de confirmação de remoção da categoria
@Composable
fun ConfirmDeleteCategoryDialog(
    categoryName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    // Diálogo de alerta para confirmar a exclusão de uma categoria
    AlertDialog(
        onDismissRequest = onDismiss,

        // Textos que são exibidos no diálogo
        title = {
            Text("Confirmar exclusão")
        },

        text = {
            Text("Tem certeza que deseja excluir a categoria '$categoryName'?")
        },

        // Botão para confirmar a exclusão da categoria
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()  // Executa ação de confirmação (excluir categoria)
                    onDismiss()  // Fecha o diálogo após confirmar
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Excluir")
            }
        },

        // Botão para cancelar e fechar o diálogo
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// Pop-up de confirmação de exclusão de senha
@Composable
fun RemovePasswordDialog(
    item: SenhaItem,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    onError: (Exception) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                // Seta de voltar e fechar o pop-up
                BackButtonBar(onBackClick = onDismiss)

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Certeza que deseja remover a senha?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {

                        // Botão cancelar (apenas fecha o diálogo)
                        Button(
                            onClick = onDismiss
                        ) {
                            Text("Cancelar")
                        }

                        // Botão remover (executa a exclusão no Firestore)
                        Button(
                            onClick = {
                                deletePasswordById(item.id)
                                onSuccess()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Remover")
                        }
                    }
                }
            }
        }
    }
}

// Pop-up para confirmar a senha antes da utilização do QRCode
@Composable
fun ConfirmPasswordDialog(
    onDismiss: () -> Unit,
    onConfirmed: () -> Unit
) {
    val context = LocalContext.current

    var senha by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Obtém o email do usuário logado
    val email = Firebase.auth.currentUser?.email ?: "Email não disponível"

    // Caixa de diálogo com campos personalizados
    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(
                onClick = {
                    val uid = Firebase.auth.currentUser?.uid
                    if (uid != null) {
                        checkEmailVerified(uid) { isVerified ->
                            if (isVerified) { reauthenticateUser(senha, context, onSuccess = {
                                onConfirmed()
                                onDismiss()
                            }, onFailure = {
                                senha = ""
                            })
                            } else {

                                resendVerificationEmail(context)

                                Toast.makeText(context, "E-mail não confirmado. Verifique sua caixa de entrada.",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            ) {

                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancelar")
            }
        },
        title = { Text("Confirmação de senha") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                CustomOutlinedTextField(
                    value = email,
                    onValueChange = {},
                    label = "Email",
                    enabled = false
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo de entrada de senha com ícone de visibilidade
                CustomOutlinedTextField(
                    value = senha,
                    onValueChange = { senha = it },
                    label = "Senha",
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar senha" else "Mostrar senha"
                            )
                        }
                    }
                )
            }
        }
    )
}

// Função que exibe um pop-up com todas as informações sobre a senha
@Composable
fun PasswordDetailDialog(
    item: SenhaItem,
    onDismiss: () -> Unit,
    onPasswordRemoved: () -> Unit
) {
    val context = LocalContext.current

    var showRemovePopUp by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Abre o pop-up com detalhes da senha
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                // Botão de voltar
                BackButtonBar(onBackClick = onDismiss)

                // Campo do login
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Login:", fontWeight = FontWeight.Bold)
                    StandardBoxPopUp {
                        Text(
                            text = item.login,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Campo da senha
                    Text("Senha:", fontWeight = FontWeight.Bold)
                    StandardBoxPopUp {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            // Ocultação e exibição de senha
                            Text(

                                // Se passwordVisible == true, mostre a senha real; caso contrário, mostre tantos “•” quantos caracteres tiver a senha
                                text = if (passwordVisible)
                                    item.senha
                                else
                                    "•".repeat(item.senha.length),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 4.dp),
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Ícone de olho que alterna entre mostrar e ocultar a senha
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        Icons.Default.Visibility // Senha visível
                                    else
                                        Icons.Default.VisibilityOff, // Senha não é visível
                                    contentDescription = if (passwordVisible)
                                        "Ocultar senha"
                                    else
                                        "Mostrar senha",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Campo da descrição
                    Text("Descrição:", fontWeight = FontWeight.Bold)
                    StandardBoxPopUp {
                        Text(
                            text = item.descricao,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Campo da categoria
                    Text("Categoria:", fontWeight = FontWeight.Bold)
                    StandardBoxPopUp {
                        Text(
                            text = item.categoria,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        // Botão para alterar a senha
                        Button(
                            onClick = {
                                val intent = Intent(context, ChangePasswordActivity::class.java).apply {
                                    putExtra("PASSWORD_ID", item.id)
                                    putExtra("PASSWORD_TITLE", item.titulo)
                                    putExtra("PASSWORD_VALUE", item.senha)
                                    putExtra("PASSWORD_DESCRIPTION", item.descricao)
                                    putExtra("PASSWORD_CATEGORY", item.categoria)
                                    putExtra("PASSWORD_LOGIN", item.login)
                                }
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text("Alterar")
                        }

                        // Botão para remover a senha
                        Button(
                            onClick = { showRemovePopUp = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text("Remover")
                        }
                    }

                    // Diálogo de confirmação de remoção
                    if (showRemovePopUp) {
                        RemovePasswordDialog(
                            item = item,
                            onDismiss = {
                                showRemovePopUp = false
                            },
                            onSuccess = {
                                showRemovePopUp = false
                                onDismiss()
                                onPasswordRemoved()
                                Toast.makeText(context, "Senha excluída com sucesso!", Toast.LENGTH_SHORT).show()
                            },
                            onError = { e ->
                                Toast.makeText(context, "Erro ao excluir: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }
            }
        }
    }
}