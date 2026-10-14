package pe.edu.upeu.pharmamobil.domain.usecase

import pe.edu.upeu.pharmamobil.domain.model.Sesion
import pe.edu.upeu.pharmamobil.domain.repository.AuthRepository

/** Al arrancar: si el almacen guarda un token vigente, no se pide login. */
class SesionGuardadaUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Sesion? =
        resultadoDe { authRepository.sesionGuardada() }.getOrNull()
}
