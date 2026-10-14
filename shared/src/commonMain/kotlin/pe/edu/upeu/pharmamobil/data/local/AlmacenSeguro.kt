package pe.edu.upeu.pharmamobil.data.local

/**
 * Almacen cifrado del token. Es una interfaz y no un expect porque Android
 * necesita un Context en el constructor y iOS no recibe nada: cada
 * platformModule registra su implementacion.
 */
interface AlmacenSeguro {
    fun guardarToken(token: String)
    fun leerToken(): String?
    fun borrarToken()
}
