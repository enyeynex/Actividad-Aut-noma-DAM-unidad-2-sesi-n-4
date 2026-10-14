package pe.edu.upeu.pharmamobil.presentation.sesion

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobil.domain.model.Credenciales
import pe.edu.upeu.pharmamobil.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobil.domain.model.FalloSesionException
import pe.edu.upeu.pharmamobil.domain.model.Sesion
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobil.domain.sesion.AvisosDeSesion
import pe.edu.upeu.pharmamobil.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.SesionGuardadaUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SesionViewModelTest {

    private val admin = Sesion("tok", "admin", "Carlos Ramirez Torres", setOf("CATALOGO_ESCRIBIR", "CLIENTES_LEER"))

    private class FakeAuthRepository(
        var guardada: Sesion? = null,
        var falloLogin: Throwable? = null,
        val sesionLogin: Sesion? = null
    ) : AuthRepository {
        var cerrada = false
        override suspend fun iniciarSesion(credenciales: Credenciales): Sesion {
            falloLogin?.let { throw it }
            return sesionLogin!!
        }
        override suspend fun sesionGuardada(): Sesion? = guardada
        override suspend fun cerrarSesion() { cerrada = true; guardada = null }
    }

    @BeforeTest
    fun instalarMain() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun restaurarMain() {
        Dispatchers.resetMain()
    }

    private fun nuevoViewModel(repositorio: FakeAuthRepository, avisos: AvisosDeSesion = AvisosDeSesion()) =
        SesionViewModel(
            iniciarSesion = IniciarSesionUseCase(repositorio),
            sesionGuardada = SesionGuardadaUseCase(repositorio),
            cerrarSesion = CerrarSesionUseCase(repositorio),
            avisos = avisos
        )

    @Test
    fun sinTokenGuardadoMuestraElLogin() = runTest {
        val estado = nuevoViewModel(FakeAuthRepository()).uiState.value
        assertFalse(estado.verificando)
        assertNull(estado.sesion)
    }

    @Test
    fun conTokenGuardadoEntraDirectoSinPedirCredenciales() = runTest {
        val estado = nuevoViewModel(FakeAuthRepository(guardada = admin)).uiState.value
        assertEquals(admin, estado.sesion)
    }

    @Test
    fun unLoginCorrectoAbreLaSesionYLimpiaLaClave() = runTest {
        val viewModel = nuevoViewModel(FakeAuthRepository(sesionLogin = admin))

        viewModel.onUsuarioChange("admin")
        viewModel.onClaveChange("Admin123")
        viewModel.iniciarSesion()

        val estado = viewModel.uiState.value
        assertEquals(admin, estado.sesion)
        assertEquals("", estado.clave)
        assertFalse(estado.cargando)
    }

    @Test
    fun loginConCamposVaciosNoLlegaAlServidor() = runTest {
        val viewModel = nuevoViewModel(FakeAuthRepository(falloLogin = IllegalStateException("no deberia llamarse")))

        viewModel.iniciarSesion()

        assertEquals("El usuario es obligatorio", viewModel.uiState.value.error)
    }

    @Test
    fun credencialesIncorrectasMuestranUnError() = runTest {
        val viewModel = nuevoViewModel(
            FakeAuthRepository(falloLogin = FalloSesionException(ErrorSesion.CredencialesInvalidas))
        )

        viewModel.onUsuarioChange("admin")
        viewModel.onClaveChange("mala")
        viewModel.iniciarSesion()

        assertEquals("Usuario o contraseña incorrectos", viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.sesion)
    }

    @Test
    fun unAvisoDe401CierraLaSesionYVuelveAlLoginConMensaje() = runTest {
        val repositorio = FakeAuthRepository(guardada = admin)
        val avisos = AvisosDeSesion()
        val viewModel = nuevoViewModel(repositorio, avisos)

        avisos.notificar(ErrorSesion.SesionExpirada)

        val estado = viewModel.uiState.value
        assertNull(estado.sesion)
        assertTrue(repositorio.cerrada)
        assertEquals("Tu sesión expiró. Vuelve a iniciar sesión", estado.aviso)
        // La interfaz queda limpia: no se muestra el usuario anterior.
        assertEquals("", estado.usuario)
    }

    @Test
    fun unTokenAlteradoExplicaElMotivoSinCulparALasCredenciales() = runTest {
        val avisos = AvisosDeSesion()
        val viewModel = nuevoViewModel(FakeAuthRepository(guardada = admin), avisos)

        avisos.notificar(ErrorSesion.CredencialesInvalidas, "El token no es válido")

        assertEquals(
            "Tu sesión terminó (HTTP 401). PharmaSoft respondió: «El token no es válido»",
            viewModel.uiState.value.aviso
        )
        assertNull(viewModel.uiState.value.sesion)
    }

    @Test
    fun cerrarSesionNoDejaElUsuarioAnteriorEnElLogin() = runTest {
        val viewModel = nuevoViewModel(FakeAuthRepository(sesionLogin = admin))
        viewModel.onUsuarioChange("admin")
        viewModel.onClaveChange("Admin123")
        viewModel.iniciarSesion()

        viewModel.cerrarSesion()

        assertEquals("", viewModel.uiState.value.usuario)
        assertEquals("", viewModel.uiState.value.clave)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun cerrarSesionVuelveAlLoginSinAviso() = runTest {
        val repositorio = FakeAuthRepository(guardada = admin)
        val viewModel = nuevoViewModel(repositorio)

        viewModel.cerrarSesion()

        assertNull(viewModel.uiState.value.sesion)
        assertNull(viewModel.uiState.value.aviso)
        assertTrue(repositorio.cerrada)
    }

    @Test
    fun cadaSesionNuevaCambiaElIdentificador() = runTest {
        val viewModel = nuevoViewModel(FakeAuthRepository(guardada = admin, sesionLogin = admin))
        val primera = viewModel.uiState.value.idSesion

        viewModel.cerrarSesion()
        viewModel.onUsuarioChange("admin")
        viewModel.onClaveChange("Admin123")
        viewModel.iniciarSesion()

        assertNotNull(viewModel.uiState.value.sesion)
        assertTrue(viewModel.uiState.value.idSesion > primera)
    }
}
