package pe.edu.upeu.pharmamobil.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.data.repository.AuthRepositorioRest
import pe.edu.upeu.pharmamobil.data.repository.ClienteRepositorioRest
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioRest
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
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoViewModel
import pe.edu.upeu.pharmamobil.presentation.sesion.SesionViewModel

/** Calificador del Boolean que dice si la app es una compilacion de depuracion. */
val ES_DEPURACION = named("esDepuracion")

/**
 * Desde la sesion 10 los repositorios hablan con PharmaSoft. Cada plataforma
 * aporta en su platformModule el motor HTTP, el AlmacenSeguro y ES_DEPURACION.
 */
val dataModule = module {
    single { crearHttpClient(motor = get(), almacen = get(), esDepuracion = get(ES_DEPURACION)) }
    single<AuthRepository> { AuthRepositorioRest(get(), get()) }
    single<ProductoRepository> { ProductoRepositorioRest(get(), get()) }
    single<ClienteRepository> { ClienteRepositorioRest(get(), get()) }
}

val domainModule = module {
    single { AvisosDeSesion() }
    factory { RegistrarProductoUseCase(get()) }
    factory { ListarProductosUseCase(get()) }
    factory { RegistrarClienteUseCase(get()) }
    factory { ListarClientesUseCase(get()) }
    factory { IniciarSesionUseCase(get()) }
    factory { CerrarSesionUseCase(get()) }
    factory { SesionGuardadaUseCase(get()) }
}

val presentationModule = module {
    viewModel { ProductoViewModel(get(), get()) }
    viewModel { ClienteViewModel(get(), get()) }
    // El Compartidor lo aporta el platformModule de cada plataforma.
    viewModel { DetalleProductoViewModel(get(), get()) }
    viewModel { SesionViewModel(get(), get(), get(), get()) }
}


expect val platformModule: Module

fun initKoin(configuracionAdicional: KoinApplication.() -> Unit = {}) {
    startKoin {
        configuracionAdicional()
        modules(
            dataModule,
            domainModule,
            presentationModule,
            platformModule
        )
    }
}
