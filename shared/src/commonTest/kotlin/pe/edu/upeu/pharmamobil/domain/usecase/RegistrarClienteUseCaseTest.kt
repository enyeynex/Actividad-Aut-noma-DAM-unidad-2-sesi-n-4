package pe.edu.upeu.pharmamobil.domain.usecase

import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.repository.FakeClienteRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * Las reglas que antes vivian en ClienteValidator, dentro de la capa de
 * presentacion, se verifican ahora en el caso de uso.
 */
class RegistrarClienteUseCaseTest {

    private fun nuevoCasoDeUso(
        repositorio: FakeClienteRepository = FakeClienteRepository()
    ) = RegistrarClienteUseCase(repositorio)

    private suspend fun erroresAlRegistrar(
        dni: String = "70123456",
        nombre: String = "Farmacia Nueva Vida",
        correo: String = "ventas@central.pe",
        telefono: String = "989789123"
    ): ErroresDeCliente {

        val resultado = nuevoCasoDeUso().invoke(dni, nombre, correo, telefono)
        val fallo = assertIs<ClienteInvalidoException>(resultado.exceptionOrNull())
        return fallo.errores
    }

    @Test
    fun aceptaUnClienteValido() = runTest {

        val resultado = nuevoCasoDeUso()
            .invoke("70123456", "Farmacia Nueva Vida", "ventas@central.pe", "989789123")

        assertNull(resultado.exceptionOrNull())
        assertEquals("989789123", resultado.getOrThrow().telefono)
    }

    @Test
    fun rechazaNombreVacio() = runTest {
        assertEquals(
            "El nombre es obligatorio",
            erroresAlRegistrar(nombre = "   ").nombre
        )
    }

    @Test
    fun rechazaCorreoConFormatoInvalido() = runTest {
        assertEquals(
            "El correo no tiene un formato válido",
            erroresAlRegistrar(correo = "ventas.central.pe").correo
        )
    }

    @Test
    fun rechazaTelefonoQueNoTieneNueveDigitos() = runTest {
        assertEquals(
            "El teléfono debe tener 9 dígitos",
            erroresAlRegistrar(telefono = "12345").telefono
        )
    }

    @Test
    fun rechazaDniQueNoTieneOchoDigitos() = runTest {
        assertEquals(
            "El DNI debe tener 8 dígitos",
            erroresAlRegistrar(dni = "1234").dni
        )
    }

    @Test
    fun pideNombreYApellido() = runTest {
        assertEquals(
            "Escribe nombre y apellido",
            erroresAlRegistrar(nombre = "Lucia").nombre
        )
    }

    @Test
    fun elTelefonoEsOpcionalYSeGuardaComoNull() = runTest {

        val resultado = nuevoCasoDeUso()
            .invoke("70123456", "Farmacia Nueva Vida", "ventas@central.pe", "   ")

        assertNull(resultado.exceptionOrNull())
        assertNull(resultado.getOrThrow().telefono)
    }

    @Test
    fun reportaLosTresErroresALaVez() = runTest {

        val errores = erroresAlRegistrar(nombre = "", correo = "", telefono = "abc")

        assertEquals("El nombre es obligatorio", errores.nombre)
        assertEquals("El correo es obligatorio", errores.correo)
        assertEquals("El teléfono debe tener 9 dígitos", errores.telefono)
    }

    @Test
    fun elIdLoAsignaElRepositorio() = runTest {

        val casoDeUso = nuevoCasoDeUso()

        val primero = casoDeUso.invoke("70123456", "Botica A", "a@central.pe", "").getOrThrow()
        val segundo = casoDeUso.invoke("70123457", "Botica B", "b@central.pe", "").getOrThrow()

        assertEquals(1L, primero.id)
        assertEquals(2L, segundo.id)
    }

    @Test
    fun devuelveFailureCuandoElRepositorioFalla() = runTest {

        val repositorio = FakeClienteRepository().apply {
            fallaAlRegistrar = IllegalStateException("Sin conexión")
        }

        val resultado = nuevoCasoDeUso(repositorio)
            .invoke("70123456", "Farmacia Nueva Vida", "ventas@central.pe", "989789123")

        assertEquals("Sin conexión", resultado.exceptionOrNull()?.message)
    }
}
