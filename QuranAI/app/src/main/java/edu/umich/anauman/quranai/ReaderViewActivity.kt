package edu.umich.anauman.quranai

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.telecom.Call
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.android.volley.Response
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ReaderViewActivity : Activity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.reader_view)

        // Get the verse from the intent extra
        val verse = intent.getStringExtra("verse")

        // Make an API call to get the Quranic verse
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.quran.com/api/v4/verses/by_key/$verse")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API", "API call failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    // Parse the JSON response
                    val json = JSONObject(response.body!!.string())
                    val text = json.getJSONObject("verse").getString("text_simple")
                    val audioUrl = json.getJSONObject("verse").getJSONArray("audio").getJSONObject(0).getString("url")

                    // Update the XML fields with the Quranic verse
                    runOnUiThread {
                        val verseTextView = findViewById<TextView>(R.id.verse_text)
                        verseTextView.text = text

                        // Update the source of the MediaPlayer with the audio file
                        val mediaPlayer = MediaPlayer()
                        mediaPlayer.setDataSource(audioUrl)
                        mediaPlayer.prepareAsync()
                        mediaPlayer.setOnPreparedListener { mp -> mp.start() }
                    }
                }
            }
        })

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            // Your code here to handle the back button click event
            Log.d("Log:", "Navigating Backwards")
            finish()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
