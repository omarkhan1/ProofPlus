package edu.umich.anauman.quranai

import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject

// Send the click for a chapter and verse to the second server.
fun sendClick(chapter: Int, verse: Int) {
    val serverUrl = "" // TBD

    val jsonObject = mapOf(
        "chapter" to chapter,
        "verse" to verse
    )

    JsonObjectRequest(
        Request.Method.POST,
        serverUrl+"sendclick/", JSONObject(jsonObject),
        {},
        { error -> Log.e("sendClick", error.localizedMessage ?: "JsonObjectRequest error") }
    )
}

// Get the five most popular verses. Not all of them are potentially valid (some may be null).
fun getPopular(): Map<String, Int> {
    val serverUrl = "" // TBD

    var returnObj: Map<String, Int> = mutableMapOf()

    val postRequest = JsonObjectRequest(
        Request.Method.GET,
        serverUrl+"getpopular/", null,
        {
                response ->
            val chapter_1 = response.getJSONObject("location_1").getInt("chapter")
            val verse_1 = response.getJSONObject("location_1").getInt("verse")
            val chapter_2 = response.getJSONObject("location_2").getInt("chapter")
            val verse_2 = response.getJSONObject("location_2").getInt("verse")
            val chapter_3 = response.getJSONObject("location_3").getInt("chapter")
            val verse_3 = response.getJSONObject("location_3").getInt("verse")
            val chapter_4 = response.getJSONObject("location_4").getInt("chapter")
            val verse_4 = response.getJSONObject("location_4").getInt("verse")
            val chapter_5 = response.getJSONObject("location_5").getInt("chapter")
            val verse_5 = response.getJSONObject("location_5").getInt("verse")

            returnObj = mapOf(
                "chapter_1" to chapter_1,
                "verse_1" to verse_1,
                "chapter_2" to chapter_2,
                "verse_2" to verse_2,
                "chapter_3" to chapter_3,
                "verse_3" to verse_3,
                "chapter_4" to chapter_4,
                "verse_4" to verse_4,
                "chapter_5" to chapter_5,
                "verse_5" to verse_5,
            )

            Log.d("getPopular", "Most popular verses received")
        },
        { error -> Log.e("getPopular", error.localizedMessage ?: "JsonObjectRequest error") }
    )

    return returnObj
}