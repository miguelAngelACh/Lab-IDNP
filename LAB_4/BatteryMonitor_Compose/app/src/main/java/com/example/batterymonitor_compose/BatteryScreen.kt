package com.example.batterymonitor_compose

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun BatteryScreen() {
    var porcentaje by remember { mutableStateOf(0) }
    val context = LocalContext.current

    val ACTION_ACTUALIZAR = "com.tuapp.ACTUALIZAR_BATERIA"

    DisposableEffect(Unit) {
        val autoReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val nivel = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val escala = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                if (nivel != -1 && escala != -1) {
                    porcentaje = (nivel * 100) / escala
                    Log.d("BatteryScreen", "AutoReceiver: batería actualizada a $porcentaje%")
                }
            }
        }

        context.registerReceiver(autoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        Log.d("BatteryScreen", "AutoReceiver registrado")

        onDispose {
            context.unregisterReceiver(autoReceiver)
            Log.d("BatteryScreen", "AutoReceiver desregistrado")
        }
    }

     DisposableEffect(Unit) {
        val manualReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == ACTION_ACTUALIZAR && context != null) {
                    val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                    val nivel = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                    val escala = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

                    if (nivel != -1 && escala != -1) {
                        porcentaje = (nivel * 100) / escala
                        Log.d("BatteryScreen", "ManualReceiver: batería actualizada manualmente a $porcentaje%")
                    }
                }
            }
        }
        Log.d("BatteryScreen", "ManualReceiver registrado")

        onDispose {
            context.unregisterReceiver(manualReceiver)
            Log.d("BatteryScreen", "ManualReceiver desregistrado")
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Batería: $porcentaje%")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val intent = Intent(ACTION_ACTUALIZAR)

            val pendingIntent = PendingIntent.getBroadcast(
                  context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            pendingIntent.send()
        }) {
            Text(text = "Actualizar manualmente")
        }
    }
}