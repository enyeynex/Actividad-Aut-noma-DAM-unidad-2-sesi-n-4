package pe.edu.upeu.pharmamobil.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobil.data.local.FakeAlmacenSeguro
import pe.edu.upeu.pharmamobil.data.repository.AuthRepositorioRest
import pe.edu.upeu.pharmamobil.data.repository.ClienteRepositorioRest
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioRest
import pe.edu.upeu.pharmamobil.domain.platform.Compartidor
import pe.edu.upeu.pharmamobil.domain.platform.FakeCompartidor
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.domain.sesion.AvisosDeSesion
import pe.edu.upeu.pharmamobil.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarClientesUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarClienteUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.SesionGuardadaUseCase
import pe.edu.upeu.pharmamobil.presentation.cliente.ClienteViewModel
import pe.edu.upeu.pharmamobil.presentation.detalle.DetalleProductoViewModel
import pe.edu.upeu.pharmamobil.presentation.diagnostico.DiagnosticoViewModel
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoViewModel
import pe.edu.upeu.pharmamobil.presentation.sesion.SesionViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame

/**
 * Comprueba que el grafo se ensambla sin arrancar la aplicacion. El
 * platformModule real necesita Context o UIKit, que en una prueba no
 * existen: se reemplaza por dobles de lo que aporta cada plataforma
 * (motor HTTP, AlmacenSeguro, ES_DEPURACION y Compartidor).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppModuleTest {

    @BeforeTest
    fun instalarMain() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun detenerKoin() {
        stopKoin()
        Dispatchers.resetMain()
    }

    private val plataformaDePrueba = module {
        single<HttpClientEngine> { MockEngine { respondOk() } }
        single<AlmacenSeguro> { FakeAlmacenSeguro() }
        single(ES_DEPURACION) { false }
        single<Compartidor> { FakeCompartidor() }
    }

    private fun grafoCompleto(): Koin = startKoin {
        modules(dataModule, domainModule, presentationModule, plataformaDePrueba)
    }.koin

    @Test
    fun resuelveLosRepositoriosRestPorSuInterfazDeDominio() {

        val koin = grafoCompleto()

        assertIs<AuthRepositorioRest>(koin.get<AuthRepository>())
        assertIs<ProductoRepositorioRest>(koin.get<ProductoRepository>())
        assertIs<ClienteRepositorioRest>(koin.get<ClienteRepository>())
    }

    @Test
    fun elClienteHttpYLosAvisosSonUnicos() {

        val koin = grafoCompleto()

        assertSame(koin.get<io.ktor.client.HttpClient>(), koin.get<io.ktor.client.HttpClient>())
        assertSame(koin.get<AvisosDeSesion>(), koin.get<AvisosDeSesion>())
    }

    @Test
    fun resuelveLosCasosDeUso() {

        val koin = grafoCompleto()

        koin.get<RegistrarProductoUseCase>()
        koin.get<ListarProductosUseCase>()
        koin.get<RegistrarClienteUseCase>()
        koin.get<ListarClientesUseCase>()
        koin.get<IniciarSesionUseCase>()
        koin.get<CerrarSesionUseCase>()
        koin.get<SesionGuardadaUseCase>()
    }

    @Test
    fun resuelveLosViewModel() {

        val koin = grafoCompleto()

        koin.get<ProductoViewModel>()
        koin.get<ClienteViewModel>()
        koin.get<DetalleProductoViewModel>()
        koin.get<SesionViewModel>()
        koin.get<DiagnosticoViewModel>()
    }
}
