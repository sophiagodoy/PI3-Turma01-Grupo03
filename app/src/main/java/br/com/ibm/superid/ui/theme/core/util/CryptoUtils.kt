// ARQUIVO COM AS FUNÇÕES DE CRIPTOGRAFIA

package br.com.ibm.superid.ui.theme.core.util

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

// Baseado na documentação: https://www.baeldung.com/kotlin/advanced-encryption-standard
// Função que gera um token de acesso aleatório para a senha
@OptIn(ExperimentalEncodingApi::class)
fun createacesstoken(): String {

    // Cria um gerador de números aleatórios
    val random = SecureRandom()

    // Cria um array para que tenha um tamanho de 256 bytes (32 bytes = 256 bits)
    val bytes = ByteArray(32)

    // Preenche o array bytes com valores aleatórios
    random.nextBytes(bytes)

    // Converte os bytes gerados em uma string Base64
    return Base64.encode(bytes)
}

// Baseado na documentação: https://www.baeldung.com/kotlin/advanced-encryption-standard
// Essa função criptografa uma senha usando AES, tendo como a chave "ProjetoIntegrador3Semestre062025"
@OptIn(ExperimentalEncodingApi::class)
fun encryptpassword(
    password: String, // texto (senha) que quero cifrar
    encryptionKey: String = "ProjetoIntegrador3Semestre062025" // chave secreta usada para cifrar
): Pair<String, String> {

    // Converte a chave para bytes UTF-8 e ajusta para 32 bytes (256 bits)
    val keyBytes = encryptionKey.toByteArray(Charsets.UTF_8).copyOf(32)

    // Cria a especificação da chave secreta para o algoritmo AES
    val secretKey = SecretKeySpec(keyBytes, "AES")

    // Gera 16 bytes aleatórios (tamanho pedido pela AES)
    val iv = ByteArray(16)

    // Preenche com bytes criptografados
    SecureRandom().nextBytes(iv)

    // Cria o objeto IV
    val ivSpec = IvParameterSpec(iv)

    // Configuração da criptografia, aonde obtem a instancia do cipher
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

    // Inicializa para a criptografia com a chave e IV
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)

    // Converte a senha para bytes UTF-8 e criptografa
    val encryptedBytes = cipher.doFinal(password.toByteArray(Charsets.UTF_8))

    // Utilizado "Return Pair", para que consiga dar return em dois elementos ncessarios para descriptografar
    // Retorna Pair com a senha criptografada e IV, ambos em Base64
    return Pair(
        Base64.encode(encryptedBytes),
        Base64.encode(iv)
    )
}

// Função que descriptografa a senha
@OptIn(ExperimentalEncodingApi::class)
fun decryptPassword(encrypted: String, ivBase64: String, key: String = "ProjetoIntegrador3Semestre062025"): String {
    val keyBytes = key.toByteArray(Charsets.UTF_8).copyOf(32)
    val secretKey = SecretKeySpec(keyBytes, "AES")
    val iv = Base64.decode(ivBase64)
    val ivSpec = IvParameterSpec(iv)
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
    val decryptedBytes = cipher.doFinal(Base64.decode(encrypted))
    return decryptedBytes.toString(Charsets.UTF_8)
}

// Baseado na documentação: https://www.baeldung.com/kotlin/advanced-encryption-standard
// Criptografa uma senha usando AES, tendo como a chave "ProjetoIntegrador3Semestre062025"
@OptIn(ExperimentalEncodingApi::class)
fun encryptPassword(password: String, encryptionKey: String = "ProjetoIntegrador3Semestre062025"): Pair<String, String> {
    // Converte a chave para bytes UTF-8 e ajusta para 32 bytes (256 bits)
    val keyBytes = encryptionKey.toByteArray(Charsets.UTF_8).copyOf(32)
    // Cria a especificação da chave secreta para o algoritmo AES
    val secretKey = SecretKeySpec(keyBytes, "AES")
    // Gera 16 bytes aleatórios (tamanho pedido pela AES)
    val iv = ByteArray(16)
    // Preenche com bytes criptografados
    SecureRandom().nextBytes(iv)
    // Cria o objeto IV
    val ivSpec = IvParameterSpec(iv)
    // Configuração da criptografia, aonde obtem a instancia do cipher
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    // Inicializa para a criptografia com a chave e IV
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
    // Converte a senha para bytes UTF-8 e criptografa
    val encryptedBytes = cipher.doFinal(password.toByteArray(Charsets.UTF_8))

    // Utilizado "Return Pair", para que consiga dar return em dois elementos ncessarios para descriptografar
    // Retorna Pair com a senha criptografada e IV, ambos em Base64
    return Pair(
        Base64.encode(encryptedBytes),
        Base64.encode(iv)
    )
}
