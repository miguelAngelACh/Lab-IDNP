package com.example.navcompose_loginregistro

import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.navcompose_loginregistro.ui.theme.NavCompose_LoginRegistroTheme
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.navArgument
import java.io.File
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "login") {
                composable("login") {
                    LoginScreen(
                        onLoginExitoso = { usuario ->
                            Toast.makeText(
                                this@MainActivity,
                                "Bienvenido $usuario",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onIrARegistro = {
                            navController.navigate("registro")
                        }
                    )
                }

                composable("registro") {
                    RegistroScreen(
                        onRegistroExitoso = {
                            navController.popBackStack()
                        },
                        onCancelar = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NavCompose_LoginRegistroTheme {
        Greeting("Android")
    }
}

val cuentasRegistradas = mutableStateListOf(
    "admin" to "1234" // cuenta de prueba inicial
)

@Composable
fun LoginScreen(
    onLoginExitoso: (String) -> Unit,
    onIrARegistro: () -> Unit
) {
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Iniciar sesión", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it },
            label = { Text("Usuario") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (mensajeError.isNotEmpty()) {
            Text(mensajeError, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                val coincide = cuentasRegistradas.any {
                    it.first == usuario && it.second == password
                }
                if (coincide) {
                    mensajeError = ""
                    onLoginExitoso(usuario)
                } else {
                    mensajeError = "Usuario o contraseña incorrectos"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ingresar")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onIrARegistro,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear cuenta")
        }
    }
}

@Composable
fun RegistroScreen(
    onRegistroExitoso: () -> Unit,
    onCancelar: () -> Unit
) {
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Crear cuenta", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it },
            label = { Text("Nuevo usuario") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Nueva contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                cuentasRegistradas.add(usuario to password)
                onRegistroExitoso()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Aceptar")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onCancelar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }
    }
}

@Composable
fun HomeScreen(
    usuario: String,
    onCerrarSesion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bienvenido $usuario",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onCerrarSesion,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}