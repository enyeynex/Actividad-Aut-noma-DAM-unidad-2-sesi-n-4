package pe.edu.upeu.pharmamobil.data.local

/** Almacen en memoria para las pruebas: no hay Keystore ni Keychain en un test. */
class FakeAlmacenSeguro(var token: String? = null) : AlmacenSeguro {
    override fun guardarToken(token: String) { this.token = token }
    override fun leerToken(): String? = token
    override fun borrarToken() { token = null }
}
