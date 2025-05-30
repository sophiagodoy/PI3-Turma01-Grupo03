# SuperID

**Projeto Integrador 3**  
_PUC Campinas – Engenharia de Software – 3° Semestre_

## Integrantes:
- [Arthur Azevedo Locce Baptista](https://github.com/arthurlocce)  
- [Eduarda Picolo Barboza](https://github.com/eduardapicolo)  
- [Felipe Nonato Leoneli](https://github.com/lipeleoneli)  
- [Henrique Martins](https://github.com/HenriqueMartins2502)  
- [Sophia Franco de Godoy](https://github.com/sophiagodoy)

## Sobre o Projeto

O **SuperID** é um aplicativo Android nativo, desenvolvido em Kotlin, cujo objetivo principal é proporcionar um gerenciamento seguro e centralizado de credenciais de acesso. Nele, o usuário pode:

- Cadastrar e armazenar senhas criptografadas, organizadas por categorias (Sites Web, Aplicativos, Dispositivos etc.).  
- Recuperar sua “senha mestre” em caso de esquecimento, via e-mail.  
- Realizar “login sem senha” em sites parceiros por meio da leitura de um QR Code.  

## Tecnologias e Ferramentas

## SuperID - Android Studio 
- **Kotlin**  
- **Android Studio**  
- **Firebase Firestore**  
- **Firebase Authentication**  
- **Firebase Functions**
- **VsCode**
- **Git & GitHub**

## Site Parceiro - VSCode 
- **TypeScript**
- **JavaScript**
- **HTML**
- **CSS**

## Como Abrir e Rodar no Android Studio

1. **Clone o Repositório**
      ```bash
   git@github.com:sophiagodoy/PI3-Turma01-Grupo03.git

3. **Abra o Android Studio**  
   - Inicie o Android Studio e abra a pasta que foi clonada

4. **Configurar o Firebase (Firestore + Authentication)**  
   - No Console [Firebase](https://console.firebase.google.com/), crie um novo projeto. 
   - Ative o **Firestore** e o **Authentication**.  
   - Baixe o arquivo de configuração `google-services.json` e copie para a pasta `app/`.

5. **Sincronize o Gradle**  
   - No Android Studio, clique em **Sync Now** quando for solicitado.  
   - Certifique-se de que não há erros de dependências.

6. **Execute no emulador**  
   - Dentro do Android Studio, selecione um emulador existente ou crie um novo.  
   - Clique em **Run** para instalar e iniciar o app no emulador.  

> **Observação:** ao criar ou editar dados no app, você poderá visualizar e monitorar tudo em tempo real no banco de dados Firestore e no Authentication.

## Como Rodar no Celular Android

1. Conecte um dispositivo Android via cabo USB e ative a **Depuração USB** em **Opções de Desenvolvedor**; 
2. No Android Studio, selecione seu dispositivo;
3. Clique em **Run**; o app será instalado e executado diretamente no celular;

## Site Parceiro para demonstração 

1. Clone o repositório
      ```bash
   git clone https://github.com/sophiagodoy/Intent-In-Jetpack-Compose.git
      
3. Abra no VSCode
