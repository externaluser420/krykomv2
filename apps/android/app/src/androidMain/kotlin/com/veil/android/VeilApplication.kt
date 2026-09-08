package com.veil.android

import android.app.Application
import com.veil.shared.di.VeilServiceConfig
import com.veil.shared.di.veilIdentityModule
import com.veil.shared.di.veilMessagingModule
import com.veil.shared.di.veilServiceModule
import com.veil.shared.platform.PlatformSecureStorageFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class VeilApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            System.loadLibrary("sqlcipher")
        } catch (error: UnsatisfiedLinkError) {
            throw IllegalStateException("Failed to load SQLCipher native library", error)
        }
        val storageFactory = PlatformSecureStorageFactory(applicationContext)
        startKoin {
            androidContext(this@VeilApplication)
            modules(
                veilServiceModule(
                    VeilServiceConfig(
                        useMockServices = BuildConfig.USE_MOCK_SERVICES,
                        relayBaseUrl = BuildConfig.RELAY_BASE_URL,
                    ),
                ),
                veilIdentityModule(storageFactory),
                veilMessagingModule(storageFactory),
            )
        }
    }
}
