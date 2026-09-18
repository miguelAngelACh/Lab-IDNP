package com.example.navcompose_loginregistro

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.navcompose_loginregistro.ui.theme.NavCompose_LoginRegistroTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NavCompose_LoginRegistroTheme {
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginScreen(
                                onLoginExitoso = { usuario ->
                                    navController.navigate("home/$usuario")
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

                        composable(
                            route = "home/{usuario}",
                            arguments = listOf(navArgument("usuario") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val usuario = backStackEntry.arguments?.getString("usuario") ?: ""
                            HomeScreen(
                                usuario = usuario,
                                onCerrarSesion = {
                                    navController.navigate("login") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    onLoginExitoso: (String) -> Unit,
    onIrARegistro: () -> Unit
) {
    val context = LocalContext.current
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
                if (usuario.isBlank() || password.isBlank()) {
                    mensajeError = "Por favor, completa todos los campos"
                } else {
                    val existe = validarCuenta(context, usuario, password)
                    if (existe) {
                        mensajeError = ""
                        onLoginExitoso(usuario)
                    } else {
                        mensajeError = "Cuenta no encontrada"
                    }
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
    val context = LocalContext.current
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }

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

        if (mensajeError.isNotEmpty()) {
            Text(mensajeError, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (usuario.isBlank() || password.isBlank()) {
                    mensajeError = "Por favor, completa todos los campos"
                } else {
                    guardarCuenta(context, usuario, password)
                    Toast.makeText(context, "Cuenta registrada exitosamente", Toast.LENGTH_SHORT).show()
                    onRegistroExitoso()
                }
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
        horizontalAlignment = Alignment.CenterHorizontally
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

fun guardarCuenta(context: Context, usuario: String, pass: String) {
    val linea = "$usuario,$pass\n"
    context.openFileOutput("cuentas.txt", Context.MODE_APPEND).use { output ->
        output.write(linea.toByteArray())
    }
}

fun validarCuenta(context: Context, usuario: String, pass: String): Boolean {
    val archivo = File(context.filesDir, "cuentas.txt")
    if (!archivo.exists()) {
        try {
            context.openFileOutput("cuentas.txt", Context.MODE_PRIVATE).use { output ->
                output.write("admin,1234\n".toByteArray())
            }
        } catch (_: Exception) {}
    }

    if (!archivo.exists()) return false

    context.openFileInput("cuentas.txt").bufferedReader().useLines { lineas ->
        for (linea in lineas) {
            val partes = linea.split(",")
            if (partes.size == 2) {
                val u = partes[0].trim()
                val p = partes[1].trim()
                if (u == usuario.trim() && p == pass.trim()) {
                    return true
                }
            }
        }
    }
    return false
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    NavCompose_LoginRegistroTheme {
        LoginScreen(onLoginExitoso = {}, onIrARegistro = {})
    }
}
