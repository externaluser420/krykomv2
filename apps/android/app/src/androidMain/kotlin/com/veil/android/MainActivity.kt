package com.veil.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.veil.android.ui.components.VeilLoadingScreen
import com.veil.android.ui.lock.AppLockScreen
import com.veil.android.ui.messaging.AddContactScreen
import com.veil.android.ui.messaging.ChatScreen
import com.veil.android.ui.messaging.ChatsScreen
import com.veil.android.ui.messaging.ContactProfileScreen
import com.veil.android.ui.messaging.ContactsScreen
import com.veil.android.ui.onboarding.OnboardingScreen
import com.veil.android.ui.settings.AppearanceSettingsScreen
import com.veil.android.ui.settings.SecuritySettingsScreen
import com.veil.android.ui.settings.SettingsScreen
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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
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

private enum class MainTab {
    Chats,
    Contacts,
    Settings,
}

private sealed class HomeDestination {
    data class Tab(val tab: MainTab) : HomeDestination()

    data object AddContact : HomeDestination()

    data class Chat(val contact: Contact) : HomeDestination()

    data class ContactProfile(val contact: Contact, val returnTo: HomeDestination = HomeDestination.Tab(MainTab.Contacts)) : HomeDestination()

    data object SecuritySettings : HomeDestination()

    data object AppearanceSettings : HomeDestination()
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
            VeilLoadingScreen(
                message = "Starting Veil…",
                modifier = Modifier.fillMaxSize(),
            )
        }
        AppScreen.Onboarding ->
            OnboardingScreen(
                onComplete = { screen = AppScreen.Home },
            )
        AppScreen.Lock ->
            AppLockScreen(
                onUnlocked = { screen = AppScreen.Home },
            )
        AppScreen.Home -> VeilHomeNav(
            onAccountDeleted = { screen = AppScreen.Onboarding },
        )
    }
}

@Composable
private fun VeilHomeNav(onAccountDeleted: () -> Unit) {
    var destination by remember { mutableStateOf<HomeDestination>(HomeDestination.Tab(MainTab.Chats)) }

    when (val current = destination) {
        is HomeDestination.Tab -> {
            val selectedTab = current.tab
            Scaffold(
                bottomBar = {
                    VeilBottomNav(
                        selectedTab = selectedTab,
                        onTabSelected = { tab ->
                            destination = HomeDestination.Tab(tab)
                        },
                    )
                },
                containerColor = MaterialTheme.colorScheme.background,
            ) { padding ->
                when (selectedTab) {
                    MainTab.Chats ->
                        ChatsScreen(
                            modifier = Modifier.padding(padding),
                            onAddContact = { destination = HomeDestination.AddContact },
                            onOpenChat = { contact -> destination = HomeDestination.Chat(contact) },
                        )
                    MainTab.Contacts ->
                        ContactsScreen(
                            modifier = Modifier.padding(padding),
                            onAddContact = { destination = HomeDestination.AddContact },
                            onOpenContact = { contact ->
                                destination = HomeDestination.ContactProfile(contact)
                            },
                        )
                    MainTab.Settings ->
                        SettingsScreen(
                            modifier = Modifier.padding(padding),
                            onNavigateToSecurity = { destination = HomeDestination.SecuritySettings },
                            onNavigateToAppearance = { destination = HomeDestination.AppearanceSettings },
                            onAccountDeleted = onAccountDeleted,
                        )
                }
            }
        }
        HomeDestination.AddContact ->
            AddContactScreen(
                onBack = { destination = HomeDestination.Tab(MainTab.Chats) },
                onAdded = { destination = HomeDestination.Tab(MainTab.Chats) },
            )
        is HomeDestination.Chat ->
            ChatScreen(
                contact = current.contact,
                onBack = { destination = HomeDestination.Tab(MainTab.Chats) },
                onOpenProfile = { contact ->
                    destination = HomeDestination.ContactProfile(contact, HomeDestination.Chat(current.contact))
                },
            )
        is HomeDestination.ContactProfile ->
            ContactProfileScreen(
                contact = current.contact,
                onBack = { destination = current.returnTo },
                onOpenChat = { contact -> destination = HomeDestination.Chat(contact) },
            )
        HomeDestination.SecuritySettings ->
            SecuritySettingsScreen(
                onBack = { destination = HomeDestination.Tab(MainTab.Settings) },
            )
        HomeDestination.AppearanceSettings ->
            AppearanceSettingsScreen(
                onBack = { destination = HomeDestination.Tab(MainTab.Settings) },
            )
    }
}

@Composable
private fun VeilBottomNav(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        NavigationBarItem(
            selected = selectedTab == MainTab.Chats,
            onClick = { onTabSelected(MainTab.Chats) },
            icon = {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chats")
            },
            label = { Text("Chats") },
            colors = veilNavItemColors(),
        )
        NavigationBarItem(
            selected = selectedTab == MainTab.Contacts,
            onClick = { onTabSelected(MainTab.Contacts) },
            icon = {
                Icon(Icons.Default.Contacts, contentDescription = "Contacts")
            },
            label = { Text("Contacts") },
            colors = veilNavItemColors(),
        )
        NavigationBarItem(
            selected = selectedTab == MainTab.Settings,
            onClick = { onTabSelected(MainTab.Settings) },
            icon = {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            },
            label = { Text("Settings") },
            colors = veilNavItemColors(),
        )
    }
}

@Composable
private fun veilNavItemColors() =
    NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    )
