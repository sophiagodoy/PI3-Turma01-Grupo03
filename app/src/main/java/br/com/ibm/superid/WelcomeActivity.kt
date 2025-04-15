// TELA DE EXPLICAÇÃO DO SUPERID + TERMOS DE USO
package br.com.ibm.superid

import android.os.Bundle
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PreviewWelcome()
        }
    }
}

@Composable
fun Welcome() {
    var termosAceitos by remember { mutableStateOf(false) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.LightGray)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Checkbox(
                checked = termosAceitos,
                onCheckedChange = { termosAceitos = it }
            )
            Text(text = "Aceito os termos de uso")
        }
        
        Text(
            text = "Ler mais",
            color = Color.Blue,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .clickable { mostrarDialogo = true }
        )

        if (mostrarDialogo) {
            AlertDialog(
                onDismissRequest = { mostrarDialogo = false },
                confirmButton = {
                    TextButton(onClick = { mostrarDialogo = false }) {
                        Text("Fechar")
                    }
                },
                title = {
                    Text(text = "Termos de Uso")
                },
                text = {
                    Column(modifier = Modifier
                        .height(400.dp)
                        .verticalScroll(rememberScrollState())
                    ) {  Text(text = "Termos e Condições de Uso – SuperID\n" +
                            " Versão 1.0 – Abril de 2025\n" +
                            " Bem-vindo ao SuperID, um aplicativo desenvolvido com fins educacionais para gerenciamento e\n" +
                            " autenticação de credenciais. Ao utilizar nosso aplicativo, você concorda com os seguintes\n" +
                            " Termos e Condições de Uso. Leia atentamente antes de prosseguir.\n" +
                            " 1. Aceitação dos Termos\n" +
                            " Ao utilizar o SuperID, você concorda com todos os termos descritos neste documento. Caso não\n" +
                            " concorde, não utilize o aplicativo.\n" +
                            " 2. Finalidade do Aplicativo\n" +
                            " O SuperID tem como objetivo:- Criar e gerenciar credenciais seguras do usuário.- Armazenar senhas pessoais de forma organizada e criptografada.- Permitir autenticação em sites parceiros sem uso de senhas via QR Code.- Explorar conceitos de segurança da informação com foco educacional.\n" +
                            " Importante: Este aplicativo é um projeto acadêmico e não é destinado ao uso comercial ou\n" +
                            " profissional. Embora utilizemos boas práticas de segurança, o SuperID não garante proteção\n" +
                            " completa de dados sensíveis.\n" +
                            " 3. Cadastro e Conta do Usuário\n" +
                            " Para utilizar o aplicativo, você deverá:- Informar nome, e-mail e uma senha mestre.- Validar seu e-mail por meio do Firebase Authentication.- Autorizar o armazenamento de dados como UID e IMEI do dispositivo no Firebase Firestore.\n" +
                            " A validação do e-mail é essencial para recuperação de senha e uso de certas funcionalidades.\n" +
                            " 4. Armazenamento de Senhas\n" +
                            " Você poderá cadastrar, alterar e excluir senhas pessoais, organizadas em categorias. As senhas\n" +
                            " são criptografadas antes de serem armazenadas, sendo essa criptografia escolhida pela equipe de\n" +
                            " desenvolvimento.\n" +
                            " Cada senha armazenada recebe um accessToken exclusivo, gerado aleatoriamente, que é atualizado\n" +
                            " a cada uso.\n" +
                            "5. Autenticação em Sites Parceiros\n" +
                            " Você poderá autenticar-se em sites parceiros via leitura de QR Codes. O processo ocorre da\n" +
                            " seguinte forma:- O site gera um QR Code com um token.- Você escaneia o QR Code com o app SuperID e confirma a autenticação.- O site verifica se o login foi validado através do Firebase.\n" +
                            " Este processo ocorre apenas com sites que possuem integração ativa com o SuperID.\n" +
                            " 6. Recuperação de Senha Mestre\n" +
                            " Caso você esqueça sua senha mestre, será possível redefini-la via e-mail, desde que o e-mail\n" +
                            " tenha sido previamente validado.\n" +
                            " 7. Responsabilidades do Usuário- Manter suas credenciais e senha mestre em sigilo.- Utilizar o aplicativo de forma ética e dentro dos limites educacionais propostos.- Não utilizar o SuperID para fins ilegais ou que violem direitos de terceiros.\n" +
                            " 8. Limitações de Responsabilidade\n" +
                            " O SuperID não se responsabiliza por:- Perda ou vazamento de dados decorrente de uso inadequado.- Problemas de segurança não previstos devido à natureza experimental do projeto.- Falhas na integração com sites parceiros.\n" +
                            " 9. Privacidade e Dados\n" +
                            " Os dados armazenados no SuperID são mantidos no Firebase, seguindo as políticas de privacidade\n" +
                            " da plataforma. Informações como UID, IMEI, senhas criptografadas e accessTokens são utilizadas\n" +
                            " apenas para funcionamento do app.\n" +
                            "10. Modificações nos Termos\n" +
                            " Estes termos podem ser atualizados a qualquer momento pela equipe desenvolvedora, especialmente\n" +
                            " em caso de atualizações ou correções no projeto") }
                }
            )
        }

        Button(
            onClick = {
                if (termosAceitos) {
                    // continuar
                }
            },
            enabled = termosAceitos
        ) {
            Text(text = "Continuar")
        }
    }
}

@Preview
@Composable
fun PreviewWelcome() {
    Welcome()
}
}
