# Informe de Implementación: Proyecto NavCompose Login y Registro

Este informe detalla la implementación y solución completada para la aplicación Android desarrollada en Jetpack Compose con navegación (`NavHost`).

## 📋 Resumen de Requerimientos Cumplidos

1. **Tercera pantalla `HomeScreen(usuario)` con argumentos de navegación**:
   - Registrada en el `NavHost` bajo la ruta `"home/{usuario}"` usando `navArgument("usuario") { type = NavType.StringType }`.
   - Recibe el nombre del usuario logueado como argumento y muestra el mensaje formal `"Bienvenido [usuario]"`.
   - Incluye un botón para "Cerrar sesión" que regresa a la pantalla de login limpiando el historial de navegación (`popUpTo("login") { inclusive = true }`).

2. **Almacenamiento persistente en archivo (`cuentas.txt`) en `RegistroScreen`**:
   - Al presionar el botón *"Aceptar"* en `RegistroScreen`, se almacenan el usuario y la contraseña en un archivo de texto plano llamado `cuentas.txt` utilizando el almacenamiento interno con `openFileOutput(..., Context.MODE_APPEND)`.
   - Muestra un mensaje de confirmación (`Toast`) indicando que la cuenta fue registrada exitosamente y regresa a `LoginScreen`.

3. **Verificación de credenciales en `LoginScreen` contra `cuentas.txt`**:
   - Al presionar *"Ingresar"*, el sistema valida las credenciales leyendo y buscando coincidencia en el archivo `cuentas.txt` (incluyendo una cuenta por defecto `admin`/`1234` si el archivo no existe).
   - Si la cuenta existe, navega exitosamente a `HomeScreen`.
   - Si no existe (o las credenciales son incorrectas), muestra el mensaje de error: `"Cuenta no encontrada"`.

4. **Validación de campos vacíos (Reto opcional)**:
   - Se implementó validación en ambos formularios (`LoginScreen` y `RegistroScreen`) para verificar que ningún campo esté en blanco (`isBlank()`), mostrando un mensaje de error visual (`mensajeError`) si se intenta enviar el formulario incompleto.

---

## 🛠️ Descripción Técnica de la Implementación

### Estructura de Rutas y Navegación (`NavHost`)
```kotlin
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
```

### Funciones de Persistencia (`guardarCuenta` y `validarCuenta`)
- **Guardado**:
  ```kotlin
  fun guardarCuenta(context: Context, usuario: String, pass: String) {
      val linea = "$usuario,$pass\n"
      context.openFileOutput("cuentas.txt", Context.MODE_APPEND).use { output ->
          output.write(linea.toByteArray())
      }
  }
  ```
- **Validación**:
  ```kotlin
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
  ```

---

## 📱 Capturas de Pantalla / Vistas Principales

1. **LoginScreen**: Pantalla inicial con campos de Usuario, Contraseña, botón **Ingresar**, botón **Crear cuenta** y validación de campos vacíos.
2. **RegistroScreen**: Pantalla para registrar nuevo usuario y contraseña guardados en `cuentas.txt`.
3. **HomeScreen**: Pantalla de bienvenida que muestra *"Bienvenido [usuario]"* con opción de cerrar sesión.

> [!NOTE]
> El proyecto ha sido compilado exitosamente (`app:assembleDebug`) sin errores de sintaxis ni advertencias de compilación.
