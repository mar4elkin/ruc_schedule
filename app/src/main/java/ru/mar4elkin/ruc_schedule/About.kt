package ru.mar4elkin.ruc_schedule

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

data class ReleaseNote(
    val tagName: String,
    val name: String,
    val body: String
)

class AboutViewModel : ViewModel() {
    //val schedule: MutableLiveData<List<Day>> = MutableLiveData(listOf())
    fun getRelease() {
        viewModelScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://api.github.com/repos/Textualize/textual/releases/tags/v0.53.1")
                .build()

            val response = client.newCall(request).execute()

            Log.d("AboutViewModel", response.body.toString())
        }
    }
}
@Composable
fun AboutSubBlock(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.padding(
            start = 5.dp,
            end = 5.dp,
            top = 5.dp,
            bottom = 3.dp
        )
    )
    {
        content()
    }
}

@Composable
fun AboutBlock(label: String, content: @Composable () -> Unit) {
    Text(
        textAlign = TextAlign.Left,
        text = label,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(all = 2.dp),
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 10.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            content()
        }
    }
}

@Composable
fun AboutView(viewModel: AboutViewModel) {

//    LaunchedEffect(key1 = true) {
//        viewModel.getRelease()
////        viewModel.schedule.observe(lifecycleOwner) { value ->
////        }
//    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 25.dp)
    ) {
//        AboutBlock(label = "Изменения") {
//            AboutSubBlock {
//                Text(
//                    text = "",
//                    style = MaterialTheme.typography.bodyLarge,
//                    modifier = Modifier.padding(all = 2.dp),
//                )
//            }
//        }
        AboutBlock(label = "Версия") {
            AboutSubBlock {
                Row {
                    Text(
                        text = "$APP_VERSION (master branch)",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(all = 2.dp),
                    )
                    HyperlinkText(
                        modifier = Modifier.padding(all = 2.dp),
                        fullText = "github",
                        hyperLinks = mutableMapOf(
                            "github" to "https://github.com/mar4elkin/ruc_schedule",
                        ),
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.inverseOnSurface
                        ),
                        linkTextColor = MaterialTheme.colorScheme.inversePrimary,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize
                    )
                }
            }
        }
        AboutBlock(label = "Контакты") {
//            AboutSubBlock {
//                Text(
//                    text = "Сделано Марком Тамаровым по приколу",
//                    style = MaterialTheme.typography.bodyLarge,
//                    modifier = Modifier.padding(all = 2.dp),
//                )
//            }
            AboutSubBlock {
                HyperlinkText(
                    modifier = Modifier.padding(all = 2.dp),
                    fullText = "telegram",
                    hyperLinks = mutableMapOf(
                        "telegram" to "https://t.me/mar4elkin",
                    ),
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    ),
                    linkTextColor = MaterialTheme.colorScheme.inversePrimary,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                )
            }
            AboutSubBlock {
                HyperlinkText(
                    modifier = Modifier.padding(all = 2.dp),
                    fullText = "github",
                    hyperLinks = mutableMapOf(
                        "github" to "https://github.com/mar4elkin",
                    ),
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    ),
                    linkTextColor = MaterialTheme.colorScheme.inversePrimary,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                )
            }
        }
    }
}