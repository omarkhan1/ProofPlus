package edu.umich.anauman.quranai

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import android.util.Base64
import java.sql.DriverManager
import java.sql.Connection
import java.sql.Statement
import java.util.*


fun getPredictions(context: Context, filepath: String, successListener: Response.Listener<JSONObject>) {
    val queue = Volley.newRequestQueue(context)
    val serverUrl = "https://proof-plus.herokuapp.com/"

    val file = File(filepath)
    val inputStream = FileInputStream(file)
    val bytes = ByteArray(file.length().toInt())
    inputStream.read(bytes)
    inputStream.close()

    val base64FileData = Base64.encodeToString(bytes, Base64.DEFAULT)
    val jsonObject = mapOf(
        "file" to base64FileData
    )

    Log.d("HII", "hi")

    val postRequest = JsonObjectRequest(
        Request.Method.POST,
        serverUrl+"predict", JSONObject(jsonObject), successListener
    ) { error -> Log.e("sendAudio", error.localizedMessage ?: error.message.toString()) }

    postRequest.retryPolicy = DefaultRetryPolicy(
        5000,
        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

    queue.add(postRequest)
}

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


fun getQuranComLink(chapter: Int?, verse: Int?): String {
    return String.format("https://quran.com/%d?startingVerse=%d", chapter, verse)
}