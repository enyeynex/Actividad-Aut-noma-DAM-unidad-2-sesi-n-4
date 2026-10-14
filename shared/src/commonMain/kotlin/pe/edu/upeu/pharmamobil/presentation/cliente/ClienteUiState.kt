package pe.edu.upeu.pharmamobil.presentation.cliente


data class ClienteUiState(
    val fase: Fase = Fase.Cargando,
    val formulario: FormularioCliente = FormularioCliente(),
    val registrando: Boolean = false,
    val mensajeExito: String? = null
) {

    /** Fases excluyentes de la cartera: solo una puede estar activa. */
    sealed interface Fase {

        data object Cargando : Fase

        data object SinClientes : Fase

        data class ConClientes(val clientes: List<ClienteUi>) : Fase

        data class Error(val mensaje: String) : Fase
    }
}

data class FormularioCliente(
    val dni: String = "",
    val nombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val dniError: String? = null,
    val nombreError: String? = null,
    val correoError: String? = null,
    val telefonoError: String? = null
)
