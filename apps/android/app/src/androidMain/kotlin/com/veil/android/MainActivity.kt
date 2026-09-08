package com.veil.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.veil.android.ui.lock.AppLockScreen
import com.veil.android.ui.messaging.AddContactScreen
import com.veil.android.ui.messaging.ChatScreen
import com.veil.android.ui.messaging.ContactsScreen
import com.veil.android.ui.onboarding.OnboardingScreen
import com.veil.android.ui.theme.VeilTheme
import com.veil.shared.domain.model.Contact
import com.veil.shared.domain.service.AppLockService
import com.veil.shared.domain.service.IdentityService
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VeilTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    VeilRoot()
                }
            }
        }
    }
}

private enum class AppScreen {
    Loading,
    Onboarding,
    Lock,
    Home,
}

private sealed class HomeScreen {
    data object Contacts : HomeScreen()

    data object AddContact : HomeScreen()

    data class Chat(val contact: Contact) : HomeScreen()
}

@Composable
fun VeilRoot() {
    val identityService: IdentityService = koinInject()
    val appLockService: AppLockService = koinInject()
    var screen by remember { mutableStateOf(AppScreen.Loading) }

    LaunchedEffect(Unit) {
        screen =
            when {
                !identityService.hasIdentity() -> AppScreen.Onboarding
                appLockService.isEnabled() -> AppScreen.Lock
                else -> AppScreen.Home
            }
    }

    when (screen) {
        AppScreen.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Loading…")
            }
        }
        AppScreen.Onboarding ->
            OnboardingScreen(
                onComplete = { screen = AppScreen.Home },
            )
        AppScreen.Lock ->
            AppLockScreen(
                onUnlocked = { screen = AppScreen.Home },
            )
        AppScreen.Home -> VeilHomeNav()
    }
}

@Composable
private fun VeilHomeNav() {
    var homeScreen by remember { mutableStateOf<HomeScreen>(HomeScreen.Contacts) }

    when (val current = homeScreen) {
        HomeScreen.Contacts ->
            ContactsScreen(
                onAddContact = { homeScreen = HomeScreen.AddContact },
                onOpenChat = { contact -> homeScreen = HomeScreen.Chat(contact) },
            )
        HomeScreen.AddContact ->
            AddContactScreen(
                onBack = { homeScreen = HomeScreen.Contacts },
                onAdded = { homeScreen = HomeScreen.Contacts },
            )
        is HomeScreen.Chat ->
            ChatScreen(
                contact = current.contact,
                onBack = { homeScreen = HomeScreen.Contacts },
            )
    }
}
