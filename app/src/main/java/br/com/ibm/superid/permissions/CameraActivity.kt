/*
Esse arquivo implementa uma tela que:
- Pede permissão para usar a câmera.
- Exibe a câmera frontal ou traseira com pré-visualização.
- Permite alternar entre as câmeras.
- Usa CameraX, que é a forma moderna e segura de acessar a câmera no Android.
*/

package br.com.ibm.superid.permissions

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.view.PreviewView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LifecycleOwner
import br.com.ibm.superid.ui.theme.SuperIDTheme

import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperIDTheme{
                Scaffold(modifier = Modifier.fillMaxSize()) {
                        innerPadding ->
                    WithPermission(
                        modifier = Modifier.padding(innerPadding),
                        permission = Manifest.permission.CAMERA,
                        permissionActionLabel = "Permitir Camera..."
                    ) {
                        TakePhotoScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun CameraAppTirarFoto() {
    Text("Camera aberta")
}

// Essa função exibe a interface da câmera do celular com dois botões para trocar entre câmera frontal e câmera traseira
@Composable
fun TakePhotoScreen() {

    // Variável que controla qual câmera está ativa
    var lensFacing by remember {
        mutableIntStateOf(CameraSelector.LENS_FACING_FRONT)
    }

    // Variável que representa o nível de zoom da câmera (0.0 = sem zoom)
    var zoomLevel by remember {
        mutableFloatStateOf(0.0f)
    }

    // Variável que prepara a câmera para tirar fotos
    var imageCaptureUseCase by remember {
        mutableStateOf(ImageCapture.Builder().build())
    }

    val localContext = LocalContext.current

    Box {

        // Mostra a imagem da câmera ao vivo na tela.
        CameraPreview(
            lensFacing = lensFacing,
            zoomLevel = zoomLevel,
            ImageCaptureUseCase = imageCaptureUseCase
        )

        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            Row {

                // Botão da câmera frontal
                Button(
                    onClick = { lensFacing = CameraSelector.LENS_FACING_FRONT },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(text = "Câmera frontal")
                }

                // Botão da câmera traseira
                Button(
                    onClick = { lensFacing = CameraSelector.LENS_FACING_BACK },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(text = "Câmera Traseira")
                }
            }
        }
    }
}

// Função responsável por mostrar a imagem da câmera ao vivo na tela do aplicativo
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    lensFacing: Int,
    zoomLevel: Float,
    ImageCaptureUseCase: ImageCapture
) {

    // Mostra a imagem da câmera ao vivo
    val previewUseCase = remember {
        androidx.camera.core.Preview.Builder()
            .build()
    }

    // Diz ao Android que queremos usar a camera agora
    var cameraProvider by remember {
        mutableStateOf<ProcessCameraProvider?>(null)
    }

    // Guarda o controle da câmera em tempo real (zoom, mudar foco, flash)
    var cameraControl by remember {
        mutableStateOf<CameraControl?>(null)
    }

    val localContext = LocalContext.current

    fun rebindCameraProvider() {
        cameraProvider?.let { cameraProvider ->
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                localContext as LifecycleOwner,
                cameraSelector,
                previewUseCase, ImageCaptureUseCase
            )
            cameraControl = cameraControl
        }
    }

    LaunchedEffect(Unit) {
        cameraProvider = ProcessCameraProvider
            .awaitInstance(localContext)
        rebindCameraProvider()
    }

    LaunchedEffect(lensFacing) {
        rebindCameraProvider()
    }

    LaunchedEffect(zoomLevel) {
        cameraControl?.setLinearZoom(zoomLevel)
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            PreviewView(context).also {
                previewUseCase.surfaceProvider = it.surfaceProvider
                rebindCameraProvider()
            }
        }
    )
}
