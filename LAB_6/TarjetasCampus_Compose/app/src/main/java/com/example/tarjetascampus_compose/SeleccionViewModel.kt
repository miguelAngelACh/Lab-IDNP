package com.example.tarjetascampus_compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SeleccionViewModel : ViewModel() {
    var edificioSeleccionado by mutableStateOf("Ninguno")
        private set

    fun seleccionarEdificio(nuevoEdificio: String) {
        edificioSeleccionado = nuevoEdificio
    }
}