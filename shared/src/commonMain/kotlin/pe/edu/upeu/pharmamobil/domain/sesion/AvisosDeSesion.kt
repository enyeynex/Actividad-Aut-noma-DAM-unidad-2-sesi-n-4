package pe.edu.upeu.pharmamobil.domain.sesion

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesion

/** Por que termino la sesion: el tipo de error y el mensaje literal del servidor. */
data class AvisoSesion(
    val error: ErrorSesion,
    val detalle: String? = null
)

/**
 * Canal por el que cualquier repositorio avisa que la sesion dejo de valer
 * (un 401 fuera del login). Quien escucha es SesionViewModel, que cierra la
 * sesion y devuelve al login: asi ninguna pantalla tiene que hacerlo por su
 * cuenta.
 */
class AvisosDeSesion {

    private val _sesionTerminada = MutableSharedFlow<AvisoSesion>(extraBufferCapacity = 1)
    val sesionTerminada: SharedFlow<AvisoSesion> = _sesionTerminada.asSharedFlow()

    fun notificar(error: ErrorSesion, detalle: String? = null) {
        _sesionTerminada.tryEmit(AvisoSesion(error, detalle))
    }
}
