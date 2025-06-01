// TELA DE EXPLICAÇÃO DO SUPERID + TERMOS DE USO

package br.com.ibm.superid

import android.os.Bundle
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ibm.superid.ui.theme.SuperIDTheme
import br.com.ibm.superid.ui.theme.core.util.SuperIDHeaderImage
import androidx.core.content.edit

// Declaração da Activity responsável pela tela de boas-vindas e termos de uso
class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtem SharedPreferences para verificar se o usuário já viu a tela de boas-vindas
        // Baseado em: https://developer.android.com/training/data-storage/shared-preferences
        val sharedPref = getSharedPreferences("superid_prefs", MODE_PRIVATE)
        val jaViuWelcome = sharedPref.getBoolean("welcome_exibido", false)

        // Se já viu a tela, abre a próxima Activity diretamente e finaliza esta
        if (jaViuWelcome) {
            startActivity(Intent(this, AccessOptionActivity::class.java))
            finish()
            return
        }

        // Ativa a interface Edge-to-Edge para melhor uso da tela
        enableEdgeToEdge()

        // Define o conteúdo Compose da tela
        setContent {
            SuperIDTheme {
                WelcomeScreen(
                    onContinue = {
                        // Marca no SharedPreferences que o usuário já viu a tela
                        sharedPref.edit {
                            putBoolean("welcome_exibido", true)
                        }.apply{}

                        // Navega para a tela AccessOptionActivity e finaliza a atual
                        startActivity(Intent(this@WelcomeActivity, AccessOptionActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

// Função Composable que monta a interface da tela de boas-vindas
@Composable
fun WelcomeScreen(
    onContinue: () -> Unit,  // Callback acionado ao continuar
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var termosAceitos by remember { mutableStateOf(false) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var imagemSelecionada: Int? by remember { mutableStateOf(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // Cabeçalho visual personalizado
        SuperIDHeaderImage()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 50.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título "Descrição" alinhado à esquerda
            Text(
                text = "Descrição",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
                    .align(Alignment.Start)
            )

            // Caixa que contém o texto explicativo e as imagens do tour
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())  // Permite scroll vertical do conteúdo
                    .clip(RoundedCornerShape(16.dp))  // Bordas arredondadas
                    .background(MaterialTheme.colorScheme.surfaceVariant)  // Fundo de cor variante da superfície
                    .padding(16.dp)
            ) {
                Column {
                    // Texto explicativo sobre o SuperID e suas funcionalidades
                    Text(
                        text = "Bem-vindo(a) ao SuperID!\n" +
                                "Organize suas senhas com segurança e praticidade. Aqui você pode:\n" +
                                "•\u2060  \u2060Criar categorias personalizadas para agrupar suas senhas do jeito que preferir.\n" +
                                "•\u2060  \u2060Adicionar senhas com títulos fáceis de lembrar, mantendo tudo acessível e protegido.\n" +
                                "•\u2060  \u2060Navegar por categorias para encontrar rapidamente o que precisa.\n" +
                                "•\u2060  \u2060Usar o Login Sem Senha: acesse sites parceiros com um simples escaneamento de QR Code — sem precisar digitar nada!\n" +
                                "\n" +
                                "Faça o breve tour abaixo, por meio de imagens, para conhecer um pouco mais do SuperID!",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Justify
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Título para a seção de imagens
                    Text(
                        text = "Veja como funciona:",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Linha horizontal com imagens do tour, que podem ser clicadas para ampliar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())  // Scroll horizontal para as imagens
                    ) {
                        // Lista de IDs das imagens para o tour
                        val imagensTour = listOf(
                            R.drawable.accessoption_description,
                            R.drawable.signup_description,
                            R.drawable.signin_description,
                            R.drawable.main_description
                        )

                        // Itera pelas imagens para criar cada componente Image clicável
                        imagensTour.forEach { imagemRes ->
                            Image(
                                painter = painterResource(id = imagemRes),
                                contentDescription = "Imagem do tour",
                                modifier = Modifier
                                    .size(100.dp)  // Tamanho quadrado para as miniaturas
                                    .clickable { imagemSelecionada = imagemRes }  // Define a imagem selecionada ao clicar
                            )
                        }
                    }
                }
            }

            // Caso uma imagem do tour esteja selecionada, exibe um AlertDialog com a imagem em tamanho maior
            if (imagemSelecionada != null) {
                AlertDialog(
                    onDismissRequest = { imagemSelecionada = null }, // Fecha ao tocar fora ou voltar
                    confirmButton = {
                        TextButton(onClick = { imagemSelecionada = null }) {  // Botão para fechar o diálogo
                            Text("Fechar")
                        }
                    },
                    text = {
                        Image(
                            painter = painterResource(id = imagemSelecionada!!),
                            contentDescription = "Imagem em tela cheia",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 500.dp)  // Altura máxima para não ocupar toda a tela
                        )
                    }
                )
            }

            // Linha contendo o Checkbox para aceitar os termos e o texto explicativo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Checkbox(
                    checked = termosAceitos,  // Estado de aceitação
                    onCheckedChange = { termosAceitos = it }  // Atualiza estado ao clicar
                )
                Text(
                    text = "Aceito os termos de uso",
                    fontSize = 20.sp
                )
            }

            Text(
                text = "Ler mais",
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 18.sp,
                style = TextStyle(textDecoration = TextDecoration.Underline),
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .clickable { mostrarDialogo = true }  // Abre o diálogo dos termos
            )

            // Dialogo com os termos de uso completo, exibido quando mostrarDialogo == true
            if (mostrarDialogo) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogo = false },  // Fecha ao clicar fora ou voltar
                    title = {
                        Text(text = "Termos de Uso")
                    },
                    text = {

                        // Conteúdo longo dos termos com scroll vertical
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)  // Altura máxima para scroll
                                .verticalScroll(rememberScrollState())  // Scroll vertical para texto longo
                                .padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {

                            // Título principal dos termos
                            Text(
                                text = "Termos e Condições de Uso – SuperID",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(4.dp))

                            // Versão e data dos termos
                            Text(
                                text = "Versão 1.0 – Abril de 2025",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(12.dp))

                            // Parágrafo introdutório explicando a natureza educacional do app
                            Text(
                                text = "Bem-vindo ao SuperID, um aplicativo criado para fins educacionais. " +
                                        "Ao usar, você concorda com estes termos.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Justify
                            )

                            Spacer(Modifier.height(12.dp))

                            // Seção 1: Aceitação dos Termos
                            Text(
                                text = "1. Aceitação dos Termos",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Se você não concordar, não use o aplicativo.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Justify
                            )

                            Spacer(Modifier.height(12.dp))

                            // Seção 2: Finalidade do Aplicativo
                            Text(
                                text = "2. Finalidade do Aplicativo",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "- Criar e gerenciar credenciais seguras.\n" +
                                        "- Armazenar senhas criptografadas.\n" +
                                        "- Autenticar via QR Code.\n" +
                                        "- Ensinar segurança da informação.",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(Modifier.height(12.dp))

                            // Seção 3: Cadastro e Conta do Usuário
                            Text(
                                text = "3. Cadastro e Conta do Usuário",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Para utilizar o aplicativo, você deverá:\n" +
                                        "- Informar nome, e-mail e uma senha mestre.\n" +
                                        "- Validar seu e-mail por meio do Firebase Authentication.\n" +
                                        "- Autorizar o armazenamento de dados como UID e IMEI do dispositivo no Firebase Firestore.\n" +
                                        "A validação do e-mail é essencial para recuperação de senha e uso de certas funcionalidades.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Justify
                            )

                            Spacer(Modifier.height(12.dp))

                            // Seção 4: Armazenamento de Senhas
                            Text(
                                text = "4. Armazenamento de Senhas",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Você poderá cadastrar, alterar e excluir senhas pessoais, organizadas em categorias. " +
                                        "As senhas são criptografadas antes de serem armazenadas, sendo essa criptografia escolhida pela equipe de desenvolvimento.\n" +
                                        "Cada senha armazenada recebe um accessToken exclusivo, gerado aleatoriamente, que é atualizado a cada uso.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Justify
                            )

                            Spacer(Modifier.height(12.dp))

                            // Seção 5: Autenticação em Sites Parceiros
                            Text(
                                text = "5. Autenticação em Sites Parceiros",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Você poderá autenticar-se em sites parceiros via leitura de QR Codes. " +
                                        "O processo ocorre da seguinte forma:\n" +
                                        "- O site gera um QR Code com um token.\n" +
                                        "- Você escaneia o QR Code com o app SuperID e confirma a autenticação.\n" +
                                        "- O site verifica se o login foi validado através do Firebase.\n" +
                                        "Este processo ocorre apenas com sites que possuem integração ativa com o SuperID.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Justify
                            )

                            Spacer(Modifier.height(12.dp))

                            // Seção 6: Recuperação de Senha Mestre
                            Text(
                                text = "6. Recuperação de Senha Mestre",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Caso você esqueça sua senha mestre, será possível redefini-la via e-mail, " +
                                        "desde que o e-mail tenha sido previamente validado.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Justify
                            )

                            Spacer(Modifier.height(12.dp))

                            // Seção 7: Responsabilidades do Usuário
                            Text(
                                text = "7. Responsabilidades do Usuário",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "- Manter suas credenciais e senha mestre em sigilo.\n" +
                                        "- Utilizar o aplicativo de forma ética e dentro dos limites educacionais propostos.\n" +
                                        "- Não utilizar o SuperID para fins ilegais ou que violem direitos de terceiros.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Justify
                            )

                            Spacer(Modifier.height(12.dp))

                            // Seção 8: Limitações de Responsabilidade
                            Text(
                                text = "8. Limitações de Responsabilidade",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "O SuperID não se responsabiliza por:\n" +
                                        "- Perda ou vazamento de dados decorrente de uso inadequado.\n" +
                                        "- Problemas de segurança não previstos devido à natureza experimental do projeto.\n" +
                                        "- Falhas na integração com sites parceiros.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Justify
                            )

                            Spacer(Modifier.height(12.dp))

                            // Seção 9: Privacidade e Dados
                            Text(
                                text = "9. Privacidade e Dados",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Os dados armazenados no SuperID são mantidos no Firebase, seguindo as políticas de privacidade da plataforma. " +
                                        "Informações como UID, IMEI, senhas criptografadas e accessTokens são utilizadas apenas para funcionamento do app.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Justify
                            )

                            Spacer(Modifier.height(12.dp))

                            // Seção 10: Alterações nos Termos
                            Text(
                                text = "10. Alterações nos Termos",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Reservamo-nos o direito de alterar estes termos a qualquer momento, notificando os usuários em atualizações do aplicativo.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Justify
                            )

                            Spacer(Modifier.height(16.dp))

                            // Assinatura final
                            Text(
                                text = "Equipe SuperID",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { mostrarDialogo = false }) {
                            Text("Fechar")
                        }
                    }
                )
            }

            // Botão que só é habilitado se os termos de uso forem aceitos
            Button(
                onClick = {
                    if (!termosAceitos) {
                        Toast.makeText(context, "Por favor, aceite os termos de uso para continuar.", Toast.LENGTH_SHORT).show()
                    } else {
                        onContinue()  // Se os termos já estiverem aceitos, chama o callback para avançar
                    }
                },
                modifier = Modifier
                    .height(60.dp)
                    .width(150.dp),
                enabled = termosAceitos  // Habilita o botão apenas se o usuário aceitou os termos
            ) {
                Text("Continuar")
            }
        }
    }
}