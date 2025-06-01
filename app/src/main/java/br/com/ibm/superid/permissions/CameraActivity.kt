/*
Esse arquivo implementa uma tela que:
- Pede permissão para usar a câmera.
- Exibe a câmera frontal ou traseira com pré-visualização.
- Permite alternar entre as câmeras.
- Usa CameraX, que é a forma moderna e segura de acessar a câmera no Android.
*/

package br.com.ibm.superid.permissions


import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import br.com.ibm.superid.MainActivity
import br.com.ibm.superid.ui.theme.SuperIDTheme
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONObject
import androidx.camera.core.ImageAnalysis


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WithPermission(
                        modifier = Modifier.padding(innerPadding),
                        permission = Manifest.permission.CAMERA,
                        permissionActionLabel = "Permitir Câmera..."
                    ) {
                        QrScannerScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun QrScannerScreen() {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize()) {
        BackCameraQrPreview(
            modifier = Modifier.fillMaxSize(),
            onQrCodeScanned = { qrText ->
                updateLoginInFirestore(context, qrText)

            }
        )
    }
}

@Composable
fun BackCameraQrPreview(
    modifier: Modifier = Modifier,
    onQrCodeScanned: (String) -> Unit
) {
    // 1) previewUseCase será o “espelho” que mostra o que a câmera vê.
    // Ele não lê QR, apenas reflete o vídeo para a tela.

    val previewUseCase = remember {
        androidx.camera.core.Preview.Builder().build()
    }

    // 2) Análise de imagem (para QR Code)
    // “verifica quadro a quadro” se há um código escondido naquele pedaço do vídeo
    // procurando um QR Code. Se demorar, ela ignora frames antigos
    // (STRATEGY_KEEP_ONLY_LATEST) para acompanhar rápido.

    val imageAnalyzerUseCase = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }

    // 3) Provider para ligar as UseCases
    // Ele é quem abre a câmera e liga os 2 itens de cima.

    val cameraProvider = remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val localContext = LocalContext.current

    // Função para “bindar” Preview + Analyzer na lente traseira
    //Toda vez que chamamos bindCamera(),dizemos:
    //a. “desliga tudo o que estiver ligado agora (unbindAll).”
    //b. “agora ligue a câmera traseira (selector), junte o espelho (previewUseCase) e o verificador (imageAnalyzerUseCase).”

    fun bindCamera() {
        cameraProvider.value?.let { provider ->
            val selector = androidx.camera.core.CameraSelector.Builder()
                .requireLensFacing(androidx.camera.core.CameraSelector.LENS_FACING_BACK)
                .build()

            provider.unbindAll()
            provider.bindToLifecycle(
                localContext as LifecycleOwner,
                selector, // escolhe a câmera traseira
                previewUseCase, // “espelho” que mostra o vídeo
                imageAnalyzerUseCase // “lupa” que procura QR em cada frame
            )
        }
    }

    // 4) Quando o CameraProvider ficar pronto, liga as UseCases
    // Quando o composable aparece na tela, o LaunchedEffect(Unit) chama
    // ProcessCameraProvider.awaitInstance(localContext) até a cam estar disponivel para uso

    LaunchedEffect(Unit) {
        cameraProvider.value = ProcessCameraProvider.awaitInstance(localContext)
        // Quando a câmera está pronta, guarda o “controle” em cameraProvider.value
        // e chama bindCamera() para conectar tudo.
        bindCamera()
    }

    // 5) Para analisar quadros
    //Assim que o analisador (imageAnalyzerUseCase) existir, chamamos imageAnalyzerUseCase.setAnalyzer(...).
    // Isso conecta o analisador a uma função chamada scanImageForQrCode. Sempre que um novo “frame” chegar,
    // o analisador chama scanImageForQrCode(imageProxy, onQrCodeScanned).

    LaunchedEffect(imageAnalyzerUseCase) {
        val executor = ContextCompat.getMainExecutor(localContext)
        imageAnalyzerUseCase.setAnalyzer(executor) { imageProxy ->
            scanImageForQrCode(imageProxy, onQrCodeScanned)
        }
    }

    // 6) Exibe o PreviewView que mostra o feed da câmera
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            // Quando criamos PreviewView(ctx) e chamamos previewUseCase.setSurfaceProvider(previewView.surfaceProvider),
            // conectamos previewUseCase ao PreviewView. A câmera passa a “espelhar” tudo nessa janela, e você vê na
            // tela o que a câmera vê, em tempo real.
            PreviewView(ctx).also { previewView ->
                previewUseCase.setSurfaceProvider(previewView.surfaceProvider)
            }
        }
    )
}

 //Cada frame novo chega aqui como um imageProxy.

@OptIn(ExperimentalGetImage::class)
private fun scanImageForQrCode(
    imageProxy: ImageProxy,
    onQrCodeScanned: (String) -> Unit
) {
    //é a foto no estado bruto
    val mediaImage = imageProxy.image
    // está girada a X graus
    val rotation = imageProxy.imageInfo.rotationDegrees

    if (mediaImage != null) {
        val inputImage = InputImage.fromMediaImage(mediaImage, rotation)
        //consegue encontrar um código de barra na foto
        val scanner = BarcodeScanning.getClient()

        //manda analisar
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    if (barcode.format == Barcode.FORMAT_QR_CODE) {
                        barcode.rawValue?.let { text ->
                            onQrCodeScanned(text)

                        }
                    }
                }
            }
            .addOnFailureListener {
                Log.e("QrScanner", "Erro ao escanear: ${it.message}")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

private fun updateLoginInFirestore(context: Context, qrText: String) {
    try {
        val json = JSONObject(qrText)
        val loginTokenId = json.getString("loginToken")    // extrai a chave loginToken

        val currentUser = FirebaseAuth.getInstance().currentUser //Descobre quem está usando o app (FirebaseAuth)
        if (currentUser == null) {
            Log.e("SuperID", "Usuário não autenticado.")
            return
        }
        val uid = currentUser.uid

        val db = Firebase.firestore
        val docRef = db.collection("login").document(loginTokenId) //Apontamos para tal doc da coleção

        val updates = mapOf(
            "user" to uid, // Quem fez o login, ou seja, o uid do usuário logado
            "loginTime" to FieldValue.serverTimestamp() // Quando foi feito
        )

        docRef.update(updates)
            .addOnSuccessListener {
                Log.i("SuperID", "Login atualizado em login/$loginTokenId.")
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            }
            .addOnFailureListener { e ->
                Log.e("SuperID", "Falha ao atualizar login: ${e.message}")
            }

    } catch (e: Exception) {
        Log.e("SuperID", "JSON inválido ou outro erro: ${e.message}")
    }
}