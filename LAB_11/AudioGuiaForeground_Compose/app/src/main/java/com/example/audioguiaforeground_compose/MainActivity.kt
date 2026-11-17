package com.example.audioguiaforeground_compose

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.audioguiaforeground_compose.ui.theme.AudioGuiaForeground_ComposeTheme
import kotlinx.coroutines.*

class AudioGuiaService : Service() {
    private var job: Job? = null

    companion object {
        const val ACCION_REPRODUCIR = "com.example.audioguiaforeground_compose.REPRODUCIR"
        const val ACCION_PAUSAR = "com.example.audioguiaforeground_compose.PAUSAR"
        const val ACCION_DETENER = "com.example.audioguiaforeground_compose.DETENER"
        private const val ID_CANAL = "canal_audioguia"
        private const val ID_NOTIFICACION = 1
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                ID_CANAL, "Audio-guía", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }

    private fun construirNotificacion(titulo: String): Notification {
        return NotificationCompat.Builder(this, ID_CANAL)
            .setContentTitle(titulo)
            .setContentText("Reproduciendo audio-guía")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACCION_REPRODUCIR -> {
                startForeground(ID_NOTIFICACION, construirNotificacion("Biblioteca Central"))
                iniciarAvance()
            }
            ACCION_PAUSAR -> job?.cancel()
            ACCION_DETENER -> {
                job?.cancel()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun iniciarAvance() {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO).launch {
            var segundos = 0
            while (true) {
                delay(1000)
                segundos++
            }
        }
    }
}

@Composable
fun PantallaAudioGuia() {
    val context = LocalContext.current
    var reproduciendo by remember { mutableStateOf(false) }
    var enSegundoPlano by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> enSegundoPlano = true
                Lifecycle.Event.ON_START -> enSegundoPlano = false
                else -> {}
            }
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
        onDispose {
            ProcessLifecycleOwner.get().lifecycle.removeObserver(observer)
        }
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
        Text(if (reproduciendo) "Reproduciendo audio-guía..." else "Audio-guía detenida")
        if (enSegundoPlano && reproduciendo) {
            Text("La app está en segundo plano: revisa la notificación")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = {
                reproduciendo = true
                context.startService(
                    Intent(context, AudioGuiaService::class.java)
                        .setAction(AudioGuiaService.ACCION_REPRODUCIR)
                )
            }) { Text("Reproducir") }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                reproduciendo = false
                context.startService(
                    Intent(context, AudioGuiaService::class.java)
                        .setAction(AudioGuiaService.ACCION_PAUSAR)
                )
            }) { Text("Pausar") }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                reproduciendo = false
                context.startService(
                    Intent(context, AudioGuiaService::class.java)
                        .setAction(AudioGuiaService.ACCION_DETENER)
                )
            }) { Text("Detener") }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AudioGuiaForeground_ComposeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PantallaAudioGuia()
                }
            }
        }
    }
}