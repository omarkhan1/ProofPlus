package edu.umich.anauman.quranai

import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore.Audio.Media
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


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

    private lateinit var bottomSheetBehavior : BottomSheetBehavior<LinearLayout>

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

        timer = Timer(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator


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
            bottomsheetSubheader.text = "File was saved at $dirPath$filename.mp3"
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetBG.visibility = View.VISIBLE;
        }


        btnDelete.setOnClickListener {
            stopRecorder()
            File("$dirPath$filename.mp3").delete()
            Toast.makeText(this, "Discarded recording", Toast.LENGTH_SHORT).show()
        }

        btnDelete.isClickable = false

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