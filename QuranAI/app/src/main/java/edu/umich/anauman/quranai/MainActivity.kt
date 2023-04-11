package edu.umich.anauman.quranai

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore.Audio.Media
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.bottom_sheeta.*
import kotlinx.coroutines.NonCancellable.start
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import com.android.volley.Request
import java.util.*
import java.util.concurrent.TimeUnit


const val REQUEST_CODE = 200;

class MainActivity : AppCompatActivity(), Timer.OnTimerTickListener {

    private var permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO)
    private var permissionGranted = false;

    private lateinit var recorder: MediaRecorder
    private var dirPath = ""
    private var filename = ""
    private var isRecording = false;
    private var isPaused = false;


    private lateinit var timer : Timer
    private lateinit var vibrator: Vibrator
    //val LinearLayout1 = findViewById<LinearLayout>(R.id.bottomSheet)
    //val bottomSheetLayout1 = findViewById<LinearLayout>(R.id.bottomSheetA)


   // val bottomSheetLayout2 = findViewById<LinearLayout>(R.id.five)
    //val bottomSheetBehavior2 = BottomSheetBehavior.from(bottomSheetLayout2)




    private lateinit var bottomSheetBehavior : BottomSheetBehavior<LinearLayout>

    private lateinit var bottomSheetBehavior1 : BottomSheetBehavior<LinearLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionGranted = ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED;
        if(!permissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
        }

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        bottomSheetBehavior1 = BottomSheetBehavior.from(bottomSheetA)
        bottomSheetBehavior1.peekHeight = 0
        bottomSheetBehavior1.state = BottomSheetBehavior.STATE_COLLAPSED





        timer = Timer(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        Recommendation.setOnClickListener {
            bottomSheetBehavior1 = BottomSheetBehavior.from(bottomSheetA)
            mainlayout.visibility = View.GONE
            bottomSheetBG.visibility = View.GONE
            bottomSheetBehavior1.state = BottomSheetBehavior.STATE_EXPANDED
            //recommendation()
            bottomSheetBG.visibility = View.VISIBLE
        }

        Email.setOnClickListener {
            //var cv = ContentValues()
            var username = editTextTextEmailAddress.text.toString()
            //cv.put("UNAME", editTextTextEmailAddress.text.toString())
            ///db.insert("USERS", null, cv)

            val server = ""
            val jsonObject = mapOf(
                "user" to username
            )

            val successListener = Response.Listener<JSONObject> { response ->
                // Handle successful response
                Log.d("sendEmail", "Response: $response")
                // You can extract data from the response JSONObject and perform further actions as needed
                // For example:
                val success = response.optBoolean("success")
                val message = response.optString("message")
                // ... do something with success and message ...
            }

            val postRequest = JsonObjectRequest(
                Request.Method.POST,
                server+username, JSONObject(jsonObject), successListener,
                {error -> Log.e("sendEmail", error.localizedMessage ?: "Json Object error")}
            )

            editTextTextEmailAddress.setText("")

        }



        btnRecord.setOnClickListener {
            when {
                isPaused->resumeRecorder()
                isRecording -> pauseRecorder()
                else -> startRecording()
            }

            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }

        btnList.setOnClickListener{
            // TODO
            Toast.makeText(this, "Feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        btnDone.setOnClickListener {
            stopRecorder()
//            Toast.makeText(this, "Analyzing data..", Toast.LENGTH_SHORT).show()
            println("$dirPath$filename.mp3")
//            bottomsheetSubheader.text = "File was saved at $dirPath$filename.mp3"

            val results = getPredictions("$dirPath$filename.mp3")
            //val res = getData()

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

            val textView1 = findViewById<TextView>(R.id.verse1)
            val textView2 = findViewById<TextView>(R.id.verse2)
            val textView3 = findViewById<TextView>(R.id.verse3)


            val webView = findViewById<WebView>(R.id.webView)



            textView1.setOnClickListener {
                mainlayout.visibility = View.GONE
                bottomSheetBG.visibility = View.GONE
                webView.loadUrl(getQuranComLink(results["chapter_1"], results["verse_1"]))
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }

            textView2.setOnClickListener {
                mainlayout.visibility = View.GONE
                bottomSheetBG.visibility = View.GONE
                webView.loadUrl(getQuranComLink(results["chapter_2"], results["verse_2"]))
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }

            textView3.setOnClickListener {
                mainlayout.visibility = View.GONE
                bottomSheetBG.visibility = View.GONE
                webView.loadUrl(getQuranComLink(results["chapter_3"], results["verse_3"]))
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }

            bottomSheetBG.visibility = View.VISIBLE
        }


        btnDelete.setOnClickListener {
            stopRecorder()
            File("$dirPath$filename.mp3").delete()
            Toast.makeText(this, "Discarded recording", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_CODE) {
            permissionGranted = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
        }
    }

    private fun pauseRecorder() {
        recorder.pause();
        isPaused = true;
        println("here")
        btnRecord.setImageResource(R.drawable.ic_record)

        timer.pause()
    }

    private fun resumeRecorder() {
        recorder.resume();
        isPaused = false;
        btnRecord.setImageResource(R.drawable.ic_pause)

        timer.start()
    }

    private fun recommendation() {

           // stopRecorder()
            val results = getPopular()


            val textView1 = findViewById<TextView>(R.id.verse1)
            val textView2 = findViewById<TextView>(R.id.verse2)
            val textView3 = findViewById<TextView>(R.id.verse3)
          //  val textView4 = findViewById<TextView>(R.id.verse4)
           // val textView5 = findViewById<TextView>(R.id.verse5)
            val webView = findViewById<WebView>(R.id.webView)


            textView1.setOnClickListener {
                mainlayout.visibility = View.GONE
                bottomSheetBG.visibility = View.GONE
                webView.loadUrl(getQuranComLink(results["chapter_1"], results["verse_1"]))
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }

            textView2.setOnClickListener {
                mainlayout.visibility = View.GONE
                bottomSheetBG.visibility = View.GONE
                webView.loadUrl(getQuranComLink(results["chapter_2"], results["verse_2"]))
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }

            textView3.setOnClickListener {
                mainlayout.visibility = View.GONE
                bottomSheetBG.visibility = View.GONE
                webView.loadUrl(getQuranComLink(results["chapter_3"], results["verse_3"]))
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }

            bottomSheetBG.visibility = View.VISIBLE

    }


    private fun startRecording() {
        if(!permissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
            return;
        }
        recorder = MediaRecorder()
        dirPath = "${externalCacheDir?.absolutePath}/"
        var simpleDateFormat = SimpleDateFormat("yy.MM.DD_hh.mm.ss")
        var date = simpleDateFormat.format(Date())
        filename = "audio_record_$date"
        recorder.apply{
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("$dirPath$filename.mp3")

            try {
                prepare()
            } catch (e: IOException) {}

            start()
        }
        btnRecord.setImageResource(R.drawable.ic_pause)
        isRecording = true;
        isPaused = false;

        timer.start()

        btnDelete.isClickable = true
        btnDelete.setImageResource(R.drawable.ic_delete)

        btnList.visibility = View.GONE
        btnDone.visibility = View.VISIBLE
    }

    private fun stopRecorder() {
        timer.stop();

        recorder.apply {
            stop()
            release()
        }

        isPaused = false;
        isRecording = false;

        btnList.visibility = View.VISIBLE
        btnDone.visibility = View.GONE

        btnDelete.isClickable = false;
        btnDelete.setImageResource(R.drawable.ic_delete_disabled)
        btnRecord.setImageResource(R.drawable.ic_record)

        tvTimer.text = "00:00:00";
    }

    override fun onTimerTick(duration: String) {
//        println(duration)
        tvTimer.text = duration
    }

}