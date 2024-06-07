package ru.mar4elkin.ruc_schedule

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class Lesson(val index: String, var info: String)
data class Day(val date: String, val day: String, var lessons: List<Lesson>)

val times = mapOf(
    "1" to "8:30 - 10:10",
    "2" to "10:30 - 12:10",
    "3" to "12:30 - 14:10",
    "4" to "14:30 - 16:10",
    "5" to "16:30 - 18:10",
    "6" to "18:30 - 20:10",
    "7" to "20:30 - 22:10",
)

fun createPayload(city: String, year: String, group: String, search: String): Map<String, String> {
    return mapOf(
        "branch" to city,
        "year" to year,
        "group" to group,
        "search-date" to "search-date",
        "date-search" to search
    )
}

fun request(apiUrl: String, payload: Map<String, String>?): HttpURLConnection {
    val url = URL(apiUrl)
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
    connection.doOutput = true

    if (payload != null) {
        val postData = payload.map { "${it.key}=${it.value}" }.joinToString("&").toByteArray()
        connection.outputStream.write(postData)
    }
    return connection
}

fun getGenericData(apiUrl: String, formName: String, payload: Map<String, String>?): List<Pair<String, String>> {
    val connection = request(apiUrl, payload)
    val responseCode = connection.responseCode
    val data = mutableListOf<Pair<String, String>>()

    if (responseCode == 200) {
        val document: Document = Jsoup.parse(connection.inputStream, "UTF-8", "")
        val form = document.select("[name=$formName]").first()

        if (form != null) {
            for (child in form.children()) {
                if (child.text() != "--") {
                    data.add(Pair(child.text(), child.`val`()))
                }
            }
        }
    }
    connection.disconnect()
    return data
}

fun getSchedule(apiUrl: String, payload: Map<String, String>): List<Day> {
    val connection = request(apiUrl, payload)
    val responseCode = connection.responseCode
    val schedule = mutableListOf<Day>()

    if (responseCode == 200) {
        val document: Document = Jsoup.parse(connection.inputStream, "UTF-8", "")
        val days = document.select(".p-2")

        for (day in days) {
            var date = ""
            var dayName = ""
            val _sub = mutableListOf<Lesson>()

            //get subjects
            val subjects = day.select(".card")
            for (sub in subjects) {
                val dayInfo = sub.text().trim().replace("\n", "").split(" ")
                val subjectInfo = sub.text().trim().replace("\n", "")

                val regex = """\d{2}\.\d{2}\.\d{4} \(\w+\) """.toRegex()
                val lessonsInfo = regex.replace(subjectInfo, "")
                val lessons = lessonsInfo.split(""" (?=\d+\.)""".toRegex())

                val lessonsList = lessons.mapNotNull { lesson ->
                    val parts = lesson.split(". ", limit = 2)
                    if (parts.size == 2) {
                        _sub.add(Lesson(parts[0], parts[1]))
                    } else {
                        null
                    }
                }

                date = dayInfo[0]
                dayName = dayInfo[1].replace("(", "").replace(")", "")
            }

            if (_sub.isNotEmpty()) {
                schedule.add(Day(date, dayName, _sub))
            }
        }
    }
    connection.disconnect();
    return schedule
}

fun getScheduleWithLecturerFast(apiUrl: String, payload: Map<String, String>, lecturer: String): List<Day> {
    val schedule = mutableListOf<Day>()
    val groups = getGenericData(
        apiUrl,
        "group",
        createPayload(payload["branch"].toString(), payload["year"].toString(), "", payload["date-search"].toString())
    )

    for (group in groups) {
        val currentSchedule = getSchedule(apiUrl, createPayload(payload["branch"].toString(), payload["year"].toString(), group.second, payload["date-search"].toString()))

        for (day in currentSchedule) {
            for (lesson in day.lessons) {
                val regex = Regex("\\b$lecturer\\b", RegexOption.IGNORE_CASE)
                val matchResult = regex.find(lesson.info)

                if (matchResult != null) {
                    lesson.info = lesson.info.plus(" ").plus(group.first)
                    val existingDay = schedule.find { it.date == day.date }
                    if (existingDay != null) {
                        existingDay.lessons += lesson
                        existingDay.lessons = existingDay.lessons.sortedBy { it.index }
                    } else {
                        val newDay = Day(day.date, day.day, listOf(lesson))
                        schedule.add(newDay)
                    }

                    Log.d("parser", "added")
                } else {
                    Log.d("parser", "pass")
                }
            }
        }
    }
    schedule.sortBy { it.date }
    return schedule;
}