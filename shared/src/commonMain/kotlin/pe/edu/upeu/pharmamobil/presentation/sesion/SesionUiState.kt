package pe.edu.upeu.pharmamobil.presentation.sesion

import pe.edu.upeu.pharmamobil.domain.model.Sesion

data class SesionUiState(
    val usuario: String = "",
    val clave: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val sesion: Sesion? = null,
    /** Mientras se consulta el almacen al arrancar todavia no se sabe si hay sesion. */
    val verificando: Boolean = true,
    /** Motivo por el que se volvio al login sin que el usuario lo pidiera (401). */
    val aviso: String? = null,
    /** Cambia con cada sesion nueva para que las pantallas no muestren datos del usuario anterior. */
    val idSesion: Int = 0
)
