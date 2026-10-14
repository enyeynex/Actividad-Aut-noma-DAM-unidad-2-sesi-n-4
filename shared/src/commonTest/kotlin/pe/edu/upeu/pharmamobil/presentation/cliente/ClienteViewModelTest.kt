package pe.edu.upeu.pharmamobil.presentation.cliente

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobil.data.repository.FakeClienteRepository
import pe.edu.upeu.pharmamobil.domain.usecase.ListarClientesUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarClienteUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class ClienteViewModelTest {

    @BeforeTest
    fun instalarMain() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun restaurarMain() {
        Dispatchers.resetMain()
    }

    private fun nuevoViewModel(
        repositorio: FakeClienteRepository = FakeClienteRepository()
    ) = ClienteViewModel(
        registrarCliente = RegistrarClienteUseCase(repositorio),
        listarClientes = ListarClientesUseCase(repositorio)
    )

    @Test
    fun arrancaEnSinClientesCuandoLaCarteraEstaVacia() = runTest {

        val viewModel = nuevoViewModel()

        assertEquals(ClienteUiState.Fase.SinClientes, viewModel.uiState.value.fase)
    }

    /**
     * Antes el boton solo pintaba un mensaje de exito y el cliente no se
     * guardaba en ninguna parte: aqui se comprueba que vuelve en el listado.
     */
    @Test
    fun elClienteRegistradoApareceEnLaCartera() = runTest {

        val viewModel = nuevoViewModel()

        viewModel.onDniChange("70123456")
        viewModel.onNombreChange("Farmacia Nueva Vida")
        viewModel.onCorreoChange("ventas@central.pe")
        viewModel.onTelefonoChange("989789123")
        viewModel.registrar()

        val estado = viewModel.uiState.value
        val fase = assertIs<ClienteUiState.Fase.ConClientes>(estado.fase)

        assertEquals("Farmacia Nueva Vida", fase.clientes.single().nombre)
        assertEquals("989789123", fase.clientes.single().telefono)
        assertEquals("", estado.formulario.nombre)
    }

    @Test
    fun elTelefonoAusenteSeMuestraComoNoRegistrado() = runTest {

        val viewModel = nuevoViewModel()

        viewModel.onDniChange("70123456")
        viewModel.onNombreChange("Farmacia Nueva Vida")
        viewModel.onCorreoChange("ventas@central.pe")
        viewModel.registrar()

        val fase = assertIs<ClienteUiState.Fase.ConClientes>(viewModel.uiState.value.fase)

        assertEquals(TELEFONO_AUSENTE, fase.clientes.single().telefono)
    }

    @Test
    fun elCorreoInvalidoCaeEnElFormulario() = runTest {

        val viewModel = nuevoViewModel()

        viewModel.onDniChange("70123456")
        viewModel.onNombreChange("Farmacia Nueva Vida")
        viewModel.onCorreoChange("ventas.central.pe")
        viewModel.registrar()

        val estado = viewModel.uiState.value

        assertEquals("El correo no tiene un formato válido", estado.formulario.correoError)
        assertEquals(ClienteUiState.Fase.SinClientes, estado.fase)
    }
}
