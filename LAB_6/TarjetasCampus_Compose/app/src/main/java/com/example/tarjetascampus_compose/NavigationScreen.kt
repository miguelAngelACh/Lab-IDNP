package com.example.tarjetascampus_compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

data class Edificio(
    val nombre: String,
    val descripcion: String,
    val imagenRes: Int,
    val colorFondo: Color
)

val listaEdificios = listOf(
    Edificio(
        nombre = "Edificio Principal UNSA",
        descripcion = "Sede central de la Universidad Nacional de San Agustín de Arequipa",
        imagenRes = R.drawable.unsa_principal,
        colorFondo = Color(0xFFE8EAF6)
    ),
    Edificio(
        nombre = "Biblioteca Central",
        descripcion = "Zona de estudio, cubículos de lectura y préstamo bibliográfico",
        imagenRes = R.drawable.biblioteca,
        colorFondo = Color(0xFFE3F2FD)
    ),
    Edificio(
        nombre = "Comedor Universitario",
        descripcion = "Servicio integral de alimentación balanceada para estudiantes",
        imagenRes = R.drawable.comedor,
        colorFondo = Color(0xFFFFF3E0)
    ),
    Edificio(
        nombre = "Pabellón de Ingenierías",
        descripcion = "Facultad de Ingeniería de Producción y Servicios y laboratorios",
        imagenRes = R.drawable.pabellon_ingenierias,
        colorFondo = Color(0xFFE8F5E9)
    )
)

sealed class Screen(val route: String, val label: String) {
    object Home : Screen("home", "Home")
    object Edificios : Screen("edificios", "Edificios")
    object Mapa : Screen("mapa", "Mapa")
    object Perfil : Screen("perfil", "Perfil")
}

@Composable
fun MainScreen(viewModel: SeleccionViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(Screen.Home, Screen.Edificios, Screen.Mapa, Screen.Perfil)
    val mostrarBottomBar = currentRoute?.startsWith("detalle") != true

    Scaffold(
        bottomBar = {
            if (mostrarBottomBar) {
                NavigationBar {
                    items.forEach { screen ->
                        val icon = when (screen) {
                            Screen.Home -> Icons.Default.Home
                            Screen.Edificios -> Icons.Default.List
                            Screen.Mapa -> Icons.Default.Place
                            Screen.Perfil -> Icons.Default.Person
                        }
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, contentDescription = screen.label) },
                            label = { Text(screen.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Edificios.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(edificio = viewModel.edificioSeleccionado)
            }
            composable(Screen.Edificios.route) {
                EdificiosScreen(viewModel = viewModel, navController = navController)
            }
            composable(Screen.Mapa.route) {
                MapaScreen()
            }
            composable(Screen.Perfil.route) {
                PerfilScreen()
            }
            // Navegación hacia Detalle (Ejercicio 2)[cite: 31]
            composable("detalle/{indice}") { backStackEntry ->
                val indice = backStackEntry.arguments?.getString("indice")?.toIntOrNull() ?: 0
                val edificio = listaEdificios[indice]
                DetalleLugarScreen(
                    nombre = edificio.nombre,
                    descripcion = edificio.descripcion,
                    imagen = painterResource(id = edificio.imagenRes),
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun EdificiosScreen(viewModel: SeleccionViewModel, navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Estilo 1 — Tarjeta Lugar (Row)",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        itemsIndexed(listaEdificios) { indice, edificio ->
            TarjetaLugar(
                nombre = edificio.nombre,
                descripcion = edificio.descripcion,
                imagen = painterResource(id = edificio.imagenRes),
                colorFondo = edificio.colorFondo,
                modifier = Modifier.clickable {
                    viewModel.seleccionarEdificio(edificio.nombre)
                    navController.navigate("detalle/$indice")
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Estilo 2 — Tarjeta Compacta (Box)",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        itemsIndexed(listaEdificios) { indice, edificio ->
            TarjetaCompacta(
                nombre = edificio.nombre,
                imagen = painterResource(id = edificio.imagenRes),
                modifier = Modifier.clickable {
                    viewModel.seleccionarEdificio(edificio.nombre)
                    navController.navigate("detalle/$indice")


                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Estilo 3 — Tarjeta con Insignia (Reto Opcional)",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        itemsIndexed(listaEdificios) { indice, edificio ->
            val etiqueta = when (indice) {
                0 -> "Central"
                1 -> "Estudio"
                2 -> "Comedor"
                else -> "Aulas"
            }
            val colorTag = when (indice) {
                0 -> Color(0xFF1565C0)
                1 -> Color(0xFF2E7D32)
                2 -> Color(0xFFE65100)
                else -> Color(0xFF6A1B9A)
            }

            TarjetaEtiquetada(
                nombre = edificio.nombre,
                descripcion = edificio.descripcion,
                imagen = painterResource(id = edificio.imagenRes),
                etiqueta = etiqueta,
                colorEtiqueta = colorTag,
                modifier = Modifier.clickable {
                    viewModel.seleccionarEdificio(edificio.nombre)
                    navController.navigate("detalle/$indice")
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleLugarScreen(
    nombre: String,
    descripcion: String,
    imagen: Painter,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = nombre) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = imagen,
                contentDescription = nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = nombre,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = descripcion,
                fontSize = 16.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            TarjetaLugar(
                nombre = nombre,
                descripcion = descripcion,
                imagen = imagen,
                colorFondo = Color(0xFFF5F5F5),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun TarjetaLugar(
    nombre: String,
    descripcion: String,
    imagen: Painter,
    colorFondo: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = colorFondo,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Image(
                painter = imagen,
                contentDescription = nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = nombre, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(text = descripcion, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun TarjetaCompacta(
    nombre: String,
    imagen: Painter,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Image(
            painter = imagen,
            contentDescription = nombre,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.35f))
        )
        Text(
            text = nombre,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        )
    }
}

@Composable
fun HomeScreen(edificio: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Bienvenido al Campus Navigator", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Último edificio consultado: $edificio", color = Color.Gray)
    }
}

@Composable
fun MapaScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Mapa de ubicaciones (pendiente de integrar)")
    }
}

@Composable
fun PerfilScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Perfil de Usuario")
    }
}

@Composable
fun TarjetaEtiquetada(
    nombre: String,
    descripcion: String,
    imagen: Painter,
    etiqueta: String,
    colorEtiqueta: Color = Color(0xFF2E7D32),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFAFAFA))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Image(
                painter = imagen,
                contentDescription = nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Text(
                    text = nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF1E1E1E)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = descripcion,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 2
                )
            }
        }

        Surface(
            color = colorEtiqueta,
            shape = RoundedCornerShape(bottomStart = 10.dp),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Text(
                text = etiqueta.uppercase(),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}