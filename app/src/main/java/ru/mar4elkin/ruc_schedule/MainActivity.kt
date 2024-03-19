package ru.mar4elkin.ruc_schedule

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DrawerValue
import androidx.compose.material.ModalDrawer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberDrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import ru.mar4elkin.ruc_schedule.ui.theme.Ruc_scheduleTheme

const val APP_VERSION = "0.0.2a"
const val APP_PREFERENCES = "app_settings"
const val APP_API_URL = "https://schedule.ruc.su"
const val APP_PREFERENCES_CITY = "city"
const val APP_PREFERENCES_YEAR = "year"
const val APP_PREFERENCES_GROUP = "group"
const val APP_PREFERENCES_CITY_READABLE = "city_human"
const val APP_PREFERENCES_YEAR_READABLE = "year_human"
const val APP_PREFERENCES_GROUP_READABLE = "group_human"

var mSettings: SharedPreferences? = null

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        setContent {
            Ruc_scheduleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    DrawerWithNavigation(navController)
                }
            }
        }
    }
}

sealed class Screen(val route: String, val label: String) {
    object Home : Screen("home", "Расписание")
    object Settings : Screen("settings", "Настройки")
    object About : Screen("about", "О приложении")
}

val items = listOf(
    Screen.Home,
    Screen.Settings,
    Screen.About
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerWithNavigation(navController: NavHostController) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalDrawer(
        drawerBackgroundColor = MaterialTheme.colorScheme.surface,
        drawerState = drawerState,
        drawerContent = {
            List(items.size) { index ->
                val screen = items[index]
                TextButton(onClick = {
                    scope.launch {
                        drawerState.close()
                        navController.navigate(screen.route)
                    }
                }) {
                    Text(
                        screen.label,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 20.dp),
                    )
                }
            }
        },
        content = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        title = { Text("РУК расписание") },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) {
                                        drawerState.open()
                                    } else {
                                        drawerState.close()
                                    }
                                }
                            }) {
                                Icon(Icons.Filled.Menu, contentDescription = null)
                            }
                        }
                    )
                },
                content = { innerPadding ->
                    NavHost(
                        navController = navController,
                        modifier = Modifier.padding(top = 60.dp),
                        startDestination = Screen.Home.route,
                    ) {
                        composable(Screen.Home.route) {
                            HomeView(HomeViewModel(), navController)
                        }
                        composable(Screen.Settings.route) {
                            SettingsView(SettingsViewModel(), navController)
                        }
                        composable(Screen.About.route) {
                            AboutView(AboutViewModel())
                        }
                    }
                }
            )
        }
    )
}