package ru.mar4elkin.ruc_schedule

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

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
                Log.d("SettingsViewModel", "some error!")
                e.printStackTrace()
            }
        }
    }
}

@Composable
fun HomeView(viewModel: HomeViewModel, navController: NavController) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val formatter_render = DateTimeFormatter.ofPattern("dd.mm.yyyy")

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val schedule = remember { mutableStateOf(emptyList<Day>()) }
    var loading by remember { mutableStateOf(true) }
    var selectedDate by remember { mutableStateOf(LocalDate.now().format(formatter).toString()) }

    if (
        mSettings?.getString(APP_PREFERENCES_CITY, "").isNullOrEmpty() &&
        mSettings?.getString(APP_PREFERENCES_YEAR, "").isNullOrEmpty() &&
        mSettings?.getString(APP_PREFERENCES_GROUP, "").isNullOrEmpty()
    ) {
        navController.navigate(items[1].route) {
            popUpTo(navController.graph.startDestinationId)
            launchSingleTop = true
        }
    }

    LaunchedEffect(key1 = selectedDate) {
        loading = true
        val city = mSettings?.getString(APP_PREFERENCES_CITY, "")
        val year = mSettings?.getString(APP_PREFERENCES_YEAR, "")
        val group = mSettings?.getString(APP_PREFERENCES_GROUP, "")

        val payload = city?.let { year?.let { it1 -> group?.let { it2 -> createPayload(it, it1, it2, selectedDate) } } }
        payload?.let { viewModel.getSchedule(it) }
        viewModel.schedule.observe(lifecycleOwner) { value ->
            if (value.isNotEmpty()) {
                schedule.value = value
                loading = false
            } else {
                schedule.value = value
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(64.dp)
                    .align(Alignment.Center),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        } else {
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
                    modifier = Modifier.padding(bottom = 15.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.padding(all = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 5.dp, end = 5.dp, top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                modifier = Modifier.height(32.3.dp),
                                onClick = {
                                    val calendar = Calendar.getInstance()
                                    val year = calendar.get(Calendar.YEAR)
                                    val month = calendar.get(Calendar.MONTH)
                                    val day = calendar.get(Calendar.DAY_OF_MONTH)

                                    DatePickerDialog(
                                        context,
                                        { _, selectedYear, selectedMonth, selectedDay ->
                                            selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                                        },
                                        year,
                                        month,
                                        day
                                    ).show()
                                }) {
                                Icon(
                                    Icons.Rounded.DateRange,
                                    contentDescription = stringResource(id = R.string.date_range_desc)
                                )
                                Text(
                                    text = "Выбрать дату"
                                )
                            }
                            Card(
                                modifier = Modifier.padding(all = 10.dp).height(32.3.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.inverseOnSurface,
                                ),
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(text = "Текущая: $selectedDate", textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                }
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
    }
}