package com.example.sincronizacionservicio_compose

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.room.*
import com.example.sincronizacionservicio_compose.ui.theme.SincronizacionServicio_ComposeTheme
import kotlinx.coroutines.*


@Entity
data class EdificioSync(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val requiereSync: Boolean
)

@Dao
interface EdificioSyncDao {
    @Query("SELECT * FROM EdificioSync WHERE requiereSync = 1")
    fun obtenerPendientesDeSync(): List<EdificioSync>

    @Insert
    fun insertar(edificio: EdificioSync)
}

@Database(entities = [EdificioSync::class], version = 1)
abstract class SyncDatabase : RoomDatabase() {
    abstract fun dao(): EdificioSyncDao
}


class SincronizacionService : Service() {
    private var job: Job? = null
    private lateinit var db: SyncDatabase

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(applicationContext, SyncDatabase::class.java, "sync.db").build()

        CoroutineScope(Dispatchers.IO).launch {
            if (db.dao().obtenerPendientesDeSync().isEmpty()) {
                db.dao().insertar(EdificioSync(nombre = "Pabellón Nicholson", requiereSync = true))
                db.dao().insertar(EdificioSync(nombre = "INFOUNSA", requiereSync = true))
                db.dao().insertar(EdificioSync(nombre = "Facultad de Arquitectura", requiereSync = true))
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("SYNC", "1. Servicio iniciando sincronización...")
                delay(4000)

                val pendientes = db.dao().obtenerPendientesDeSync()
                val cantidadReal = pendientes.size

                val resultado = Intent("com.example.sincronizacionservicio_compose.ACCION_SYNC_LISTA")
                resultado.setPackage(packageName)
                resultado.putExtra("edificios_actualizados", cantidadReal) // Enviamos el dato real

                sendBroadcast(resultado)
                Log.d("SYNC", "2. Éxito: Sincronizados $cantidadReal registros de Room.")

            } catch (e: CancellationException) {
                Log.d("SYNC", "Operación cancelada por el usuario.")
            } finally {
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
        Log.d("SYNC", "Servicio destruido y corrutina cancelada.")
    }
}
@Composable
fun PantallaSincronizacion() {
    val context = LocalContext.current
    var resultado by remember { mutableStateOf<String?>(null) }
    var estaSincronizando by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                Log.d("SYNC", "3. Aviso recibido en la pantalla")
                val cantidad = intent?.getIntExtra("edificios_actualizados", 0) ?: 0
                resultado = "Sincronización completa: $cantidad edificios desde Room"
                estaSincronizando = false
            }
        }

        val filtro = IntentFilter("com.example.sincronizacionservicio_compose.ACCION_SYNC_LISTA")
        ContextCompat.registerReceiver(context, receiver, filtro, ContextCompat.RECEIVER_NOT_EXPORTED)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {
                    resultado = null
                    estaSincronizando = true
                    context.startService(Intent(context, SincronizacionService::class.java))
                },
                enabled = !estaSincronizando
            ) {
                Text("Sincronizar")
            }

            Button(
                onClick = {
                    estaSincronizando = false
                    resultado = "Sincronización cancelada manualmente."
                    context.stopService(Intent(context, SincronizacionService::class.java))
                },
                enabled = estaSincronizando,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Detener")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (estaSincronizando) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text("Procesando datos locales...")
        } else {
            Text(resultado ?: "Presiona 'Sincronizar' para iniciar")
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SincronizacionServicio_ComposeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PantallaSincronizacion()
                }
            }
        }
    }
}