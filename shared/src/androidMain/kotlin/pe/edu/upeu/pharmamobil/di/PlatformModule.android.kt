package pe.edu.upeu.pharmamobil.di

import org.koin.core.module.Module
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import pe.edu.upeu.pharmamobil.data.network.ConfiguracionApi
import pe.edu.upeu.pharmamobil.data.network.configurarPharmaSoft
import org.koin.dsl.module
import org.koin.dsl.onClose
import org.koin.android.ext.koin.androidContext
import pe.edu.upeu.pharmamobil.domain.platform.Compartidor
import pe.edu.upeu.pharmamobil.platform.CompartidorAndroid

actual val platformModule: Module = module {
    single { ConfiguracionApi("http://10.0.2.2:8080/api/v1/") }
    single { HttpClient(OkHttp) { configurarPharmaSoft() } } onClose { it?.close() }
    single<Compartidor> { CompartidorAndroid(androidContext()) }
}
