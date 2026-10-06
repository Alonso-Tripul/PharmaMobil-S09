package pe.edu.upeu.pharmamobil.di

import org.koin.core.module.Module
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import pe.edu.upeu.pharmamobil.data.network.ConfiguracionApi
import pe.edu.upeu.pharmamobil.data.network.configurarPharmaSoft
import org.koin.dsl.module
import org.koin.dsl.onClose
import pe.edu.upeu.pharmamobil.domain.platform.Compartidor
import pe.edu.upeu.pharmamobil.platform.CompartidorIos

actual val platformModule: Module = module {
    single { ConfiguracionApi("http://localhost:8080/api/v1/") }
    single { HttpClient(Darwin) { configurarPharmaSoft() } } onClose { it?.close() }
    single<Compartidor> { CompartidorIos() }
}
