package pe.edu.upeu.pharmamobil.domain.sesion

/** Resultado de una prueba: codigo HTTP, mensaje literal del servidor y si viajo la cabecera. */
data class ResultadoDiagnostico(
    val codigo: Int?,
    val mensaje: String,
    val conCabeceraAuthorization: Boolean? = null
)

/** Lo que se sabe del token guardado sin enviarlo al servidor. */
data class EstadoToken(
    val guardado: Boolean,
    /** Momento de expiracion (claim exp), en segundos desde 1970. */
    val expiraEn: Long? = null
)

/**
 * Herramienta de la actividad autonoma de la sesion 10: provoca a proposito
 * los escenarios de token que la app normal nunca produce (token alterado,
 * prefijo en minuscula, peticion sin sesion). Solo se ofrece en una
 * compilacion de depuracion.
 */
interface DiagnosticoSesion {
    fun estadoToken(): EstadoToken
    /** Cambia un caracter de la firma del token guardado. */
    fun alterarTokenGuardado(): Boolean
    /** Peticion con el cliente real (plugin Auth), tal como saldria tras cerrar sesion. */
    suspend fun peticionConElClienteActual(): ResultadoDiagnostico
    /** Peticion con el token guardado pero con el prefijo «bearer» en minuscula. */
    suspend fun peticionConPrefijoEnMinuscula(): ResultadoDiagnostico
}
