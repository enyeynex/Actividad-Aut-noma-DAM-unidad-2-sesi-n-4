package pe.edu.upeu.pharmamobil.presentation.sesion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.model.mensajeDe
import pe.edu.upeu.pharmamobil.domain.sesion.AvisosDeSesion
import pe.edu.upeu.pharmamobil.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.LoginInvalidoException
import pe.edu.upeu.pharmamobil.domain.usecase.SesionGuardadaUseCase

/**
 * Dueño de la sesion en la interfaz. Decide si se ve el login o la app, y es
 * el unico que reacciona a un 401: cierra la sesion y vuelve al login con un
 * aviso. Un 403 no llega aqui; lo muestra la pantalla que lo provoco.
 */
class SesionViewModel(
    private val iniciarSesion: IniciarSesionUseCase,
    private val sesionGuardada: SesionGuardadaUseCase,
    private val cerrarSesion: CerrarSesionUseCase,
    private val avisos: AvisosDeSesion
) : ViewModel() {

    private val _uiState = MutableStateFlow(SesionUiState())
    val uiState: StateFlow<SesionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val guardada = sesionGuardada()
            _uiState.update {
                it.copy(
                    sesion = guardada,
                    verificando = false,
                    idSesion = if (guardada != null) it.idSesion + 1 else it.idSesion
                )
            }
        }

        viewModelScope.launch {
            avisos.sesionTerminada.collect { error ->
                if (_uiState.value.sesion != null) {
                    terminarSesion(aviso = mensajeDe(error))
                }
            }
        }
    }

    fun onUsuarioChange(usuario: String) {
        _uiState.update { it.copy(usuario = usuario, error = null) }
    }

    fun onClaveChange(clave: String) {
        _uiState.update { it.copy(clave = clave, error = null) }
    }

    fun iniciarSesion() {

        if (_uiState.value.cargando) return

        viewModelScope.launch {

            _uiState.update { it.copy(cargando = true, error = null, aviso = null) }

            val estado = _uiState.value

            iniciarSesion(estado.usuario, estado.clave).fold(
                onSuccess = { sesion ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            clave = "",
                            sesion = sesion,
                            idSesion = it.idSesion + 1
                        )
                    }
                },
                onFailure = { fallo ->
                    val mensaje = when (fallo) {
                        is LoginInvalidoException -> fallo.errores.usuario ?: fallo.errores.clave
                        else -> fallo.message
                    }
                    _uiState.update {
                        it.copy(cargando = false, error = mensaje ?: "No se pudo iniciar sesión")
                    }
                }
            )
        }
    }

    /** Accion «Cerrar sesion»: borra el token y devuelve al login. */
    fun cerrarSesion() {
        viewModelScope.launch { terminarSesion(aviso = null) }
    }

    private suspend fun terminarSesion(aviso: String?) {
        cerrarSesion.invoke()
        _uiState.update {
            SesionUiState(
                usuario = it.sesion?.usuario ?: it.usuario,
                verificando = false,
                aviso = aviso,
                idSesion = it.idSesion
            )
        }
    }
}
