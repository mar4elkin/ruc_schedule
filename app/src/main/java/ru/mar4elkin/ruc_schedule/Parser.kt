package ru.mar4elkin.ruc_schedule

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log

data class Lesson(val index: String, val info: String)
data class Day(val date: String, val day: String, val lessons: List<Lesson>)

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
    return schedule
}

fun getScheduleWithLecturer(apiUrl: String, payload: Map<String, String>, Lecturer: String): List<Day> {
    val schedule = mutableListOf<Day>()
    getSchedule(apiUrl, payload).forEach { day ->
        day.lessons.forEach { lesson ->
            if (Regex(Lecturer).containsMatchIn(lesson.info)) {
                schedule.add(day)
            }
        }
    }
    return schedule;
}