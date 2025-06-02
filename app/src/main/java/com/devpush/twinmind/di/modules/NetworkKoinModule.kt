package com.devpush.twinmind.di.modules

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.serialization.gson.gson
import org.koin.dsl.module

val networkModule = module {
    single {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                gson()
            }
            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.ALL
            }
            engine {
                config {
                    retryOnConnectionFailure(true)
                }
            }
        }
    }
}

