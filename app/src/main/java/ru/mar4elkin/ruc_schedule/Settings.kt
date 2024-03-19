package ru.mar4elkin.ruc_schedule

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Local_ExposedDropdownMenuBox_Static(
    selectName: String,
    option: String
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

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
                    value = option,
                    onValueChange = {},
                    colors = TextFieldDefaults.colors(
                        //MaterialTheme.colorScheme.background
                    ),
                    readOnly = true
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Local_ExposedDropdownMenuBox(
    selectName: String,
    options: List<Pair<String, String>>,
    onValueChange: (Pair<String, String>) -> Unit
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
                        DropdownMenuItem(
                            onClick = {
                                selectedText = item
                                expanded = false
                                onValueChange(selectedText)
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

    val branchHumanReadable: MutableLiveData<String> = MutableLiveData("")
    val yearHumanReadable: MutableLiveData<String> = MutableLiveData("")
    val groupHumanReadable: MutableLiveData<String> = MutableLiveData("")

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
    var userBranchReadAble by remember { mutableStateOf("") }
    var userYearReadAble by remember { mutableStateOf("") }
    var userGroupReadAble by remember { mutableStateOf("") }
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

    LaunchedEffect(key1 = !editSettings) {
        viewModel.getBranch("branch", null)
        viewModel.branches.observe(lifecycleOwner) { value ->
            branches.value = value
        }
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
                userBranchReadAble = selectedCity.first
                userBranch = selectedCity.second
            }
            Local_ExposedDropdownMenuBox(
                "Год",
                years.value
            ) { selectedYear ->
                userYearReadAble = selectedYear.first
                userYear = selectedYear.second
            }
            Local_ExposedDropdownMenuBox(
                "Группа",
                groups.value
            ) { selectedGroup ->
                userGroupReadAble = selectedGroup.first
                userGroup = selectedGroup.second
            }
            Button(
                //colors = ButtonColors.buttonColors(backgroundColor = MaterialTheme.colorScheme.inversePrimary),
                onClick = {
                    val editor = mSettings!!.edit()
                    if (userBranch.isNotEmpty() && userYear.isNotEmpty() && userGroup.isNotEmpty()) {
                        editor.putString(APP_PREFERENCES_CITY, userBranch)
                        editor.putString(APP_PREFERENCES_YEAR, userYear)
                        editor.putString(APP_PREFERENCES_GROUP, userGroup)
                        editor.putString(APP_PREFERENCES_CITY_READABLE, userBranchReadAble)
                        editor.putString(APP_PREFERENCES_YEAR_READABLE, userYearReadAble)
                        editor.putString(APP_PREFERENCES_GROUP_READABLE, userGroupReadAble)
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            mSettings?.getString(APP_PREFERENCES_CITY_READABLE, "")?.let {
                Local_ExposedDropdownMenuBox_Static(
                    "Филиал",
                    it
                )
            }
            mSettings?.getString(APP_PREFERENCES_YEAR_READABLE, "")?.let {
                Local_ExposedDropdownMenuBox_Static(
                    "Год",
                    it
                )
            }
            mSettings?.getString(APP_PREFERENCES_GROUP_READABLE, "")?.let {
                Local_ExposedDropdownMenuBox_Static(
                    "Группа",
                    it
                )
            }
            Button(
                //colors = ButtonColors.buttonColors(backgroundColor = MaterialTheme.colorScheme.inversePrimary),
                onClick = {
                    val editor = mSettings!!.edit()
                    editor.putString(APP_PREFERENCES_CITY, "")
                    editor.putString(APP_PREFERENCES_YEAR, "")
                    editor.putString(APP_PREFERENCES_GROUP, "")
                    editor.putString(APP_PREFERENCES_CITY_READABLE, "")
                    editor.putString(APP_PREFERENCES_YEAR_READABLE, "")
                    editor.putString(APP_PREFERENCES_GROUP_READABLE, "")
                    editor.apply()
                    editSettings = true
                }) {
                Text("Изменить данные")
            }
        }
    }
}