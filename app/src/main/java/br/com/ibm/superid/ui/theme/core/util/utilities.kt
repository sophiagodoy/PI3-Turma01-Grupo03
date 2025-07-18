// ARQUIVO QUE REÚNE COMPOSABLES USADOS EM TODO O APP

package br.com.ibm.superid.ui.theme.core.util

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ibm.superid.AddPasswordActivity
import br.com.ibm.superid.MainActivity
import br.com.ibm.superid.R
import br.com.ibm.superid.SignInActivity


@Preview
@Composable
// Função que desenha o cabeçalho estilizado Super ID
fun SuperIDHeader() {
    Box(modifier = Modifier.fillMaxWidth()) {

        // Parte verde no topo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(MaterialTheme.colorScheme.primary)
        )

        // Retângulo curvado sobreposto
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(top = 60.dp)
                .fillMaxWidth()
        ) {

            // Cartão branco com cantos arredondados
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(16.dp))
                    .padding(horizontal = 32.dp, vertical = 8.dp)
            ) {

                // Texto “SuperID” em duas cores
                Row {
                    Text(
                        text = "Super",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Círculo com imagem centralizada
            /*
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFFEDEDE5), shape = CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_laucher),
                    contentDescription = "Ícone do App",
                    modifier = Modifier.size(100.dp)
                )
            }*/
        }
    }
}

// Função que insere o ícone do app dentro do círculo
@Composable
fun SuperIDHeaderImage() {
    Box(modifier = Modifier.fillMaxWidth()) {

        // Parte verde superior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(MaterialTheme.colorScheme.primary)
        )

        // Retângulo curvado sobreposto
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(top = 60.dp)
                .fillMaxWidth()
        ) {

            // Cartão branco arredondado com o texto “SuperID”
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(16.dp))
                    .padding(horizontal = 32.dp, vertical = 8.dp)
            ) {

                // Texto “SuperID” em duas cores
                Row {
                    Text(
                        text = "Super",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Círculo com ícone do app centralizada
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(90.dp)
                    .background(color = MaterialTheme.colorScheme.background,shape = CircleShape)
            ) {

                // Imagem centralizada dentro do círculo
                Image(
                    painter = painterResource(id = R.drawable.icon),
                    contentDescription = "Ícone do App",
                    modifier = Modifier.size(200.dp)
                )
            }
        }
    }
}

// Função quie exibe a seta de voltar
@Composable
fun BackButtonBar(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.CenterStart
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar para a tela anterior"
            )
        }
    }
}

// Função para que os pop-up tenham os mesmos estilos visuais
@Composable
fun StandardBoxPopUp(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant) // Cor de fundo escura
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp)) // 16.dp para o arredondamento
    ) {
        content()
    }
}

// Função que cria todos os compos de texto com os mesmos estilos
@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        modifier = modifier.padding(10.dp),
        readOnly = readOnly,
        enabled = enabled,
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        visualTransformation = visualTransformation, // Para transformar a visualização, como senhas
        keyboardOptions = keyboardOptions, // Para configurar o tipo de teclado (como senha)
        trailingIcon = trailingIcon,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface, // Cor do texto quando focado
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface, // Cor do texto quando não focado
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Cor de fundo quando focado
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, // Cor de fundo quando não focado
            focusedBorderColor = MaterialTheme.colorScheme.primary, // Cor da borda quando focado
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface // Cor da borda quando não focado
        )
    )
}
