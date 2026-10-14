package pe.edu.upeu.pharmamobil.domain.sesion

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesion

/**
 * Canal por el que cualquier repositorio avisa que la sesion dejo de valer
 * (un 401 fuera del login). Quien escucha es SesionViewModel, que cierra la
 * sesion y devuelve al login: asi ninguna pantalla tiene que hacerlo por su
 * cuenta.
 */
class AvisosDeSesion {

    private val _sesionTerminada = MutableSharedFlow<ErrorSesion>(extraBufferCapacity = 1)
    val sesionTerminada: SharedFlow<ErrorSesion> = _sesionTerminada.asSharedFlow()

    fun notificar(error: ErrorSesion) {
        _sesionTerminada.tryEmit(error)
    }
}
