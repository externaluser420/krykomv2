package com.veil.shared.di

import com.veil.shared.data.mock.InMemoryMessageStore
import com.veil.shared.data.mock.MockRelayClient
import com.veil.shared.data.mock.MockTurnService
import com.veil.shared.data.mock.NoOpPushService
import com.veil.shared.domain.port.MessageStore
import com.veil.shared.domain.port.PushService
import com.veil.shared.domain.port.RelayClient
import com.veil.shared.domain.port.TurnService
import com.veil.shared.domain.service.AppLockService
import com.veil.shared.domain.service.ContactService
import com.veil.shared.domain.service.IdentityService
import com.veil.shared.domain.service.MessageRepository
import com.veil.shared.domain.port.CryptoMessagingEngine
import com.veil.shared.platform.PlatformSecureStorageFactory
import org.koin.core.module.Module
import org.koin.dsl.module

data class VeilServiceConfig(
    val useMockServices: Boolean = true,
    val relayBaseUrl: String = "http://localhost:8080",
)

/** Network/mock relay services. */
fun veilServiceModule(config: VeilServiceConfig): Module =
    module {
        single { config }

        if (config.useMockServices) {
            single<RelayClient> { MockRelayClient() }
            single<PushService> { NoOpPushService() }
            single<TurnService> { MockTurnService() }
        } else {
            error("Production relay not yet available. Set useMockServices=true.")
        }
    }

/** Identity, app lock, and encrypted local storage. */
fun veilIdentityModule(storageFactory: PlatformSecureStorageFactory): Module =
    module {
        single { storageFactory.createSecureKeyStore() }
        single { storageFactory.createAppLockStore() }
        single<MessageStore> { storageFactory.createMessageStore() }
        single { IdentityService(get(), get()) }
        single { AppLockService(get()) }
    }

/** E2EE messaging (Phase 4). */
fun veilMessagingModule(storageFactory: PlatformSecureStorageFactory): Module =
    module {
        single<CryptoMessagingEngine> { storageFactory.createCryptoMessagingEngine() }
        single { ContactService(get(), get(), get()) }
        single { MessageRepository(get(), get(), get(), get()) }
    }

/** Combined module for tests using in-memory message store override. */
fun veilTestModule(config: VeilServiceConfig = VeilServiceConfig()): Module =
    module {
        includes(veilServiceModule(config))
        single<MessageStore> { InMemoryMessageStore() }
    }
