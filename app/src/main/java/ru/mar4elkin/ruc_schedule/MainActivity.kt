package ru.mar4elkin.ruc_schedule

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ModalDrawer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberDrawerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.mar4elkin.ruc_schedule.ui.theme.Ruc_scheduleTheme


const val APP_PREFERENCES = "app_settings"
const val APP_API_URL = "https://schedule.ruc.su"
const val APP_PREFERENCES_CITY = "city"
const val APP_PREFERENCES_YEAR = "year"
const val APP_PREFERENCES_GROUP = "group"

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
}

val items = listOf(
    Screen.Home,
    Screen.Settings,
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
                            HomeView(HomeViewModel())
                        }
                        composable(Screen.Settings.route) {
                            SettingsView(SettingsViewModel(), navController)
                        }
                    }
                }
            )
        }
    )
}

@Composable
fun RenderSchedule(schedule: List<Day>) {
    schedule.forEach {day ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 25.dp)
        ) {
            Row(
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                Card(
                    modifier = Modifier.padding(all = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Text(
                        modifier = Modifier.padding(all = 3.dp),
                        text = day.day
                    )
                }
                Card(
                    modifier = Modifier.padding(all = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.inverseOnSurface,
                    ),
                ) {
                    Text(
                        modifier = Modifier.padding(all = 3.dp),
                        text = day.date
                    )
                }
            }
            Card {
                day.lessons.forEach {lesson ->
                    Row {
                        Card(
                            modifier = Modifier.padding(all = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        ) {
                            Text(
                                modifier = Modifier.padding(all = 3.dp),
                                text = "${lesson.index} пара"
                            )
                        }
                        Card(
                            modifier = Modifier.padding(all = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.inverseOnSurface,
                            ),
                        ) {
                            times.get(lesson.index)?.let {
                                Text(
                                    modifier = Modifier.padding(all = 3.dp),
                                    text = it
                                )
                            }
                        }
                    }
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        modifier = Modifier
                            .padding(all = 10.dp)
                            .fillMaxWidth()
                    ){
                        Text(
                            modifier = Modifier.padding(all = 2.dp),
                            text = lesson.info
                        )
                    }
                }
            }
        }
    }
}

class HomeViewModel : ViewModel() {
    val schedule: MutableLiveData<List<Day>> = MutableLiveData(listOf())

    fun getSchedule(payload: Map<String, String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = getSchedule(APP_API_URL, payload)
                Log.d("SettingsViewModel", result.toString())
                schedule.postValue(result)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

@Composable
fun HomeView(viewModel: HomeViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val schedule = remember { mutableStateOf(emptyList<Day>()) }
    var canRender by remember { mutableStateOf(true) }

    if (
        !mSettings?.getString(APP_PREFERENCES_CITY, "").isNullOrEmpty() &&
        !mSettings?.getString(APP_PREFERENCES_YEAR, "").isNullOrEmpty() &&
        !mSettings?.getString(APP_PREFERENCES_GROUP, "").isNullOrEmpty()
    ) {
        canRender = false
    }

    LaunchedEffect(key1 = canRender) {
        val city = mSettings?.getString(APP_PREFERENCES_CITY, "")
        val year = mSettings?.getString(APP_PREFERENCES_YEAR, "")
        val group = mSettings?.getString(APP_PREFERENCES_GROUP, "")

        val payload = city?.let { year?.let { it1 -> group?.let { it2 -> createPayload(it, it1, it2, "") } } }
        payload?.let { viewModel.getSchedule(it) }
        viewModel.schedule.observe(lifecycleOwner) { value ->
            schedule.value = value
        }
    }

    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            modifier = Modifier
                .padding(all = 10.dp)
                .fillMaxWidth()
        ) {
            RenderSchedule(schedule.value)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Local_ExposedDropdownMenuBox(
    selectName: String,
    options: List<Pair<String, String>>,
    onValueChange: (String) -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember(options) { mutableStateOf(if (options.isNotEmpty()) options[0] else Pair("", "")) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.secondary
        ),
        modifier = Modifier.padding(all = 10.dp)
    ) {
        Text(
            text = selectName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start=25.dp, top = 10.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange =  {expanded = !expanded}
            ) {
                TextField(
                    value = selectedText.first,
                    onValueChange = {},
                    colors = TextFieldDefaults.colors(
                        //MaterialTheme.colorScheme.background
                    ),
                    readOnly = true
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    options.forEach { item ->
                        androidx.compose.material.DropdownMenuItem(
                            onClick = {
                                selectedText = item
                                expanded = false
                                onValueChange(selectedText.second)
                            }
                        ) {
                            Text(item.first)
                        }
                    }
                }
            }
        }
    }
}

class SettingsViewModel : ViewModel() {
    val branches: MutableLiveData<List<Pair<String, String>>> = MutableLiveData(listOf())
    val years: MutableLiveData<List<Pair<String, String>>> = MutableLiveData(listOf())
    val groups: MutableLiveData<List<Pair<String, String>>> = MutableLiveData(listOf())

    fun getBranch(formIndex: String, payload: Map<String, String>?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = getGenericData(APP_API_URL, formIndex, payload)
                Log.d("SettingsViewModel", result.toString())
                branches.postValue(result)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun getYear(formIndex: String, payload: Map<String, String>?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = getGenericData(APP_API_URL, formIndex, payload)
                Log.d("SettingsViewModel", result.toString())
                years.postValue(result)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun getGroup(formIndex: String, payload: Map<String, String>?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = getGenericData(APP_API_URL, formIndex, payload)
                Log.d("SettingsViewModel", result.toString())
                groups.postValue(result)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

@Composable
fun SettingsView(viewModel: SettingsViewModel, navController: NavController) {
    val context = LocalContext.current

    var userBranch by remember { mutableStateOf("") }
    var userYear by remember { mutableStateOf("") }
    var userGroup by remember { mutableStateOf("") }

    var editSettings by remember { mutableStateOf(true) }

    val lifecycleOwner = LocalLifecycleOwner.current

    val branches = remember { mutableStateOf(listOf<Pair<String, String>>()) }
    val years = remember { mutableStateOf(listOf<Pair<String, String>>()) }
    val groups = remember { mutableStateOf(listOf<Pair<String, String>>()) }

    if (
        !mSettings?.getString(APP_PREFERENCES_CITY, "").isNullOrEmpty() &&
        !mSettings?.getString(APP_PREFERENCES_YEAR, "").isNullOrEmpty() &&
        !mSettings?.getString(APP_PREFERENCES_GROUP, "").isNullOrEmpty()
        ) {
        editSettings = false
    }

    LaunchedEffect(key1 = editSettings) {
        viewModel.getBranch("branch", null)
        viewModel.branches.observe(lifecycleOwner) { value ->
            branches.value = value
        }
    }

    LaunchedEffect(key1 = userBranch) {
        val payload = userBranch?.let { createPayload(it, "", "", "") }
        viewModel.getYear("year", payload)
        viewModel.years.observe(lifecycleOwner) { value ->
            years.value = value
        }
    }

    LaunchedEffect(key1 = userYear, key2 = userBranch) {
        val payload = userBranch?.let { userYear?.let { it1 -> createPayload(it, it1, "", "") } }
        viewModel.getGroup("group", payload)
        viewModel.groups.observe(lifecycleOwner) { value ->
            groups.value = value
        }
    }

    if (editSettings) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Local_ExposedDropdownMenuBox(
                "Филиал",
                branches.value
            ) { selectedCity ->
                userBranch = selectedCity
            }
            Local_ExposedDropdownMenuBox(
                "Год",
                years.value
            ) { selectedYear ->
                userYear = selectedYear
            }
            Local_ExposedDropdownMenuBox(
                "Группа",
                groups.value
            ) { selectedGroup ->
                userGroup = selectedGroup
            }
            androidx.compose.material3.Button(
                //colors = ButtonColors.buttonColors(backgroundColor = MaterialTheme.colorScheme.inversePrimary),
                onClick = {
                    val editor = mSettings!!.edit()
                    if (userBranch.isNotEmpty() && userYear.isNotEmpty() && userGroup.isNotEmpty()) {
                        editor.putString(APP_PREFERENCES_CITY, userBranch)
                        editor.putString(APP_PREFERENCES_YEAR, userYear)
                        editor.putString(APP_PREFERENCES_GROUP, userGroup)
                        editor.apply()
                        Toast.makeText(context, "Сохранено!", Toast.LENGTH_SHORT).show()
                        navController.navigate(items[0].route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    } else {
                        Toast.makeText(context, "Проверьте все поля", Toast.LENGTH_SHORT).show()
                    }
                }) {
                Text("Сохранить")
            }
        }
    } else {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.padding(all = 10.dp)
            ) {
                androidx.compose.material3.Button(
                    //colors = ButtonColors.buttonColors(backgroundColor = MaterialTheme.colorScheme.inversePrimary),
                    onClick = {
                        val editor = mSettings!!.edit()
                        editor.putString(APP_PREFERENCES_CITY, "")
                        editor.putString(APP_PREFERENCES_YEAR, "")
                        editor.putString(APP_PREFERENCES_GROUP, "")
                        editor.apply()
                        editSettings = true
                    }) {
                    Text("Изменить данные")
                }
            }
        }
    }
}