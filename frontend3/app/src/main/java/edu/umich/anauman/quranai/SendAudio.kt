package edu.umich.anauman.quranai

import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject

fun getPredictions(file: String): Map<String, Int> {
    val serverUrl = "https://proof-plus.herokuapp.com/"

    val jsonObject = mapOf(
        "file" to file
    )

    var returnObj: Map<String, Int> = mutableMapOf()

    val postRequest = JsonObjectRequest(
        Request.Method.POST,
        serverUrl+"predict/", JSONObject(jsonObject),
        {

            response ->
            val chapter_1 = response.getJSONObject("location_1").getInt("chapter")
            val verse_1 = response.getJSONObject("location_1").getInt("verse")
            val chapter_2 = response.getJSONObject("location_2").getInt("chapter")
            val verse_2 = response.getJSONObject("location_2").getInt("verse")
            val chapter_3 = response.getJSONObject("location_3").getInt("chapter")
            val verse_3 = response.getJSONObject("location_3").getInt("verse")

            returnObj = mapOf(
                "chapter_1" to chapter_1,
                "verse_1" to verse_1,
                "chapter_2" to chapter_2,
                "verse_2" to verse_2,
                "chapter_3" to chapter_3,
                "verse_3" to verse_3,
            )

            Log.d("sendAudio", "predictions received")
        },
        { error -> Log.e("sendAudio", error.localizedMessage ?: "JsonObjectRequest error") }
    )

    return returnObj
}

fun getQuranComLink(chapter: Int?, verse: Int?): String {
    return String.format("https://quran.com/%d?startingVerse=%d", chapter, verse)
}