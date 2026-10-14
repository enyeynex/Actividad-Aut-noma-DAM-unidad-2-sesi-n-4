package pe.edu.upeu.pharmamobil.data.local

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.interpretObjCPointer
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRefVar
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSNumber
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/** En iOS el almacen cifrado es el Keychain del sistema (paquete platform.Security). */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class AlmacenSeguroIos : AlmacenSeguro {

    override fun guardarToken(token: String) {
        borrarToken()
        val datos = token.encodeToByteArray().toNSData()
        val consulta = consultaBase().apply {
            setObject(datos, forKey = clave(kSecValueData))
        }
        conDiccionario(consulta) { SecItemAdd(it, null) }
    }

    override fun leerToken(): String? = memScoped {
        val consulta = consultaBase().apply {
            setObject(NSNumber(bool = true), forKey = clave(kSecReturnData))
            setObject(clave(kSecMatchLimitOne), forKey = clave(kSecMatchLimit))
        }
        val resultado = alloc<CFTypeRefVar>()
        val estado = conDiccionario(consulta) { SecItemCopyMatching(it, resultado.ptr) }
        if (estado != errSecSuccess) return@memScoped null

        val datos = CFBridgingRelease(resultado.value) as? NSData ?: return@memScoped null
        NSString.create(data = datos, encoding = NSUTF8StringEncoding)?.toString()
    }

    override fun borrarToken() {
        conDiccionario(consultaBase()) { SecItemDelete(it) }
    }

    /** Identifica la entrada del token: una contrasena generica de esta app. */
    private fun consultaBase(): NSMutableDictionary = NSMutableDictionary().apply {
        setObject(clave(kSecClassGenericPassword), forKey = clave(kSecClass))
        setObject(SERVICIO, forKey = clave(kSecAttrService))
        setObject(CUENTA, forKey = clave(kSecAttrAccount))
    }

    /** Las constantes de Security son CFStringRef: se leen como NSString sin copiarlas. */
    private fun clave(constante: CFStringRef?): NSString =
        interpretObjCPointer(constante!!.rawValue)

    /** Las funciones SecItem reciben un CFDictionaryRef: se puentea y se libera al terminar. */
    private fun <T> conDiccionario(diccionario: NSMutableDictionary, accion: (CFDictionaryRef?) -> T): T {
        val referencia = CFBridgingRetain(diccionario)
        try {
            @Suppress("UNCHECKED_CAST")
            return accion(referencia as CFDictionaryRef?)
        } finally {
            CFRelease(referencia)
        }
    }

    /** Copia los bytes del token a un NSData, que es lo que guarda el Keychain. */
    private fun ByteArray.toNSData(): NSData = usePinned { fijado ->
        NSData.create(bytes = fijado.addressOf(0), length = size.toULong())
    }

    private companion object {
        const val SERVICIO = "pe.edu.upeu.pharmamobil"
        const val CUENTA = "jwt"
    }
}
