package com.example.audioguiaforeground_compose

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.audioguiaforeground_compose.ui.theme.AudioGuiaForeground_ComposeTheme
import kotlinx.coroutines.*


class AudioGuiaService : Service() {
    private var job: Job? = null
    private var tituloActual: String = "Biblioteca Central"

    companion object {
        const val ACCION_REPRODUCIR = "com.example.audioguiaforeground_compose.REPRODUCIR"
        const val ACCION_PAUSAR = "com.example.audioguiaforeground_compose.PAUSAR"
        const val ACCION_DETENER = "com.example.audioguiaforeground_compose.DETENER"
        const val EXTRA_TITULO = "EXTRA_TITULO"
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

    private fun construirNotificacion(titulo: String, estadoTexto: String = "Reproduciendo audio-guía"): Notification {
        // PendingIntent para Pausar desde la notificación
        val pauseIntent = Intent(this, AudioGuiaService::class.java).apply {
            action = ACCION_PAUSAR
        }
        val pausePendingIntent = PendingIntent.getService(
            this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // PendingIntent para Detener desde la notificación
        val stopIntent = Intent(this, AudioGuiaService::class.java).apply {
            action = ACCION_DETENER
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 2, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, ID_CANAL)
            .setContentTitle(titulo)
            .setContentText(estadoTexto)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            // Botones incrustados en la notificación (Punto 2)
            .addAction(android.R.drawable.ic_media_pause, "Pausar", pausePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Detener", stopPendingIntent)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra(EXTRA_TITULO)?.let { nuevoTitulo ->
            tituloActual = nuevoTitulo
        }

        when (intent?.action) {
            ACCION_REPRODUCIR -> {
                startForeground(ID_NOTIFICACION, construirNotificacion(tituloActual, "Reproduciendo audio-guía"))
                iniciarAvance()
            }
            ACCION_PAUSAR -> {
                job?.cancel()
                // Actualiza el texto de la notificación para reflejar la pausa
                val manager = getSystemService(NotificationManager::class.java)
                manager.notify(ID_NOTIFICACION, construirNotificacion(tituloActual, "Audio-guía en pausa"))
            }
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

    val edificios = listOf(
        "Biblioteca Central",
        "Pabellón Nicholson",
        "INFOUNSA",
        "Facultad de Arquitectura",
        "Estadio Ho Chi Minh"
    )
    var indiceActual by remember { mutableStateOf(0) }
    val edificioActual = edificios[indiceActual]

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            text = "Edificio: $edificioActual",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(if (reproduciendo) "Reproduciendo audio-guía..." else "Audio-guía detenida o en pausa")

        if (enSegundoPlano && reproduciendo) {
            Text("La app está en segundo plano: revisa la notificación")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = {
                reproduciendo = true
                val intent = Intent(context, AudioGuiaService::class.java)
                    .setAction(AudioGuiaService.ACCION_REPRODUCIR)
                    .putExtra(AudioGuiaService.EXTRA_TITULO, edificioActual)
                context.startService(intent)
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

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(onClick = {
            indiceActual = (indiceActual + 1) % edificios.size
            val siguienteEdificio = edificios[indiceActual]

            if (reproduciendo) {
                val intent = Intent(context, AudioGuiaService::class.java)
                    .setAction(AudioGuiaService.ACCION_REPRODUCIR)
                    .putExtra(AudioGuiaService.EXTRA_TITULO, siguienteEdificio)
                context.startService(intent)
            }
        }) {
            Text("Siguiente edificio")
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