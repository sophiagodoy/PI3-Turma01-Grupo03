// TELA DE EXPLICAÇÃO DO SUPERID + TERMOS DE USO

package br.com.ibm.superid

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ibm.superid.ui.theme.SuperIDTheme
import br.com.ibm.superid.ui.theme.core.util.SuperIDHeaderImage

// Declaração da Activity que exibe a tela de boas-vindas e termos de uso
class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Welcome(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// Função Composable que monta toda a interface da tela de boas-vindas
@Composable
fun Welcome(modifier: Modifier = Modifier) {

    // Defino o contexto atual da Activity para usar Intents
    val context = LocalContext.current

    // Declarando variáveis que controlam o comportamento da tela
    var termosAceitos by remember { mutableStateOf(false) }  // Armazena se o usuário aceitou os termos
    var mostrarDialogo by remember { mutableStateOf(false) } // Controla visibilidade do pop-up

    // Layout principal: uma coluna que ocupa toda a tela e pinta o fundo
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // Chamo a função SuperIDHeaderImage() que está em utilities.kt
        SuperIDHeaderImage()

        // Coluna interna para alinhar o conteúdo abaixo do cabeçalho
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 150.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Linha que agrupa checkbox + texto
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {

                // Definindo o Checkbox
                Checkbox(
                    checked = termosAceitos,
                    onCheckedChange = { termosAceitos = it } // controla o estado do chechbox
                )
                Text(
                    text = "Aceito os termos de uso",
                    fontSize = 20.sp
                )
            }

            // Texto "Ler mais" comportando-se como link
            Text(
                text = "Ler mais",
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 18.sp,
                style = TextStyle(textDecoration = TextDecoration.Underline),
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .clickable { mostrarDialogo = true } // define mostrarDialogo como ativo
            )

            // Se mostrarDialogo == true, exibe o diálogo com termos
            if (mostrarDialogo) {

                // Exibe uma caixa de diálogo (pop-up)
                AlertDialog(
                    onDismissRequest = { mostrarDialogo = false }, // Fecha o pop-up ao tocar fora da tela

                    // Definindo o título do diálogo
                    title = {
                        Text(text = "Termos de Uso")
                    },

                    // Corpo do diálogo com todas as seções e rolagem interna
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                                .verticalScroll(rememberScrollState()) // Permite rolagem
                                .padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {

                            // Subtítulo centralizado
                            Text(
                                text = "Termos e Condições de Uso – SuperID",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(4.dp))

                            // Definindo versão/data centralizado
                            Text(
                                text = "Versão 1.0 – Abril de 2025",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(12.dp))

                            // Definindo parágrafo introdutório
                            Text(
                                text = "Bem-vindo ao SuperID, um aplicativo criado para fins educacionais. " +
                                        "Ao usar, você concorda com estes termos.",
                                style = MaterialTheme.typography.bodyMedium,
                                // Baseado em: https://developer.android.com/reference/kotlin/androidx/compose/ui/text/style/TextAlign
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

                            // Seção 10: Modificações nos Termos
                            Text(
                                text = "10. Modificações nos Termos",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Estes termos podem ser atualizados a qualquer momento pela equipe desenvolvedora, especialmente em caso de atualizações ou correções no projeto.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Justify
                            )
                        }
                    },

                    // Botão de confirmação (fecha o pop-up quando clicado)
                    confirmButton = {
                        TextButton(
                            onClick = {
                                mostrarDialogo = false // define mostrarDialogo como inativo
                            }
                        ) {
                            Text(
                                text = "Fechar",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            }


            // Define um botão que só é possível clicar se termosAceitos = true
            Button(
                onClick = {
                    if (termosAceitos) {
                        val intent = Intent(context, AccessOptionActivity::class.java)
                        context.startActivity(intent)
                    }
                },
                // Ajustando o botão
                modifier = Modifier
                    .height(60.dp)
                    .width(150.dp),
                enabled = termosAceitos // controla se o botão está ativo ou não
            ) {
                Text(
                    text = "Continuar",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewWelcome() {
    Welcome()
}