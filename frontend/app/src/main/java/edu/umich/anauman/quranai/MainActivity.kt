package edu.umich.anauman.quranai

import android.content.pm.PackageManager
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore.Audio.Media
import android.view.View
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

const val REQUEST_CODE = 200;

class MainActivity : AppCompatActivity() {
    private var permissions = arrayOf(Manifest.permission.RECORD_AUDIO);
    private var permissionGranted = false;

    private lateinit var recorder: MediaRecorder;
    private var dirpath =""
    private var filename = ""
    private var isRecording = false;
    private var isPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionGranted = ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED;

        if(!permissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
        }

        btnRecord.setOnClickListener {
            when {
                isPaused->resumeRecording()
                isRecording->pauseRecorder()
                else -> startRecording()
            }
            startRecording();
        }

        btnList.setOnClickListener() {
            // nothing for now
        }

        btnDone.setOnClickListener() {

        }

        btnDelete.setOnClickListener() {
            stopRecorder();
            File("$dirpath$filename.mp3")
        }


    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE) {
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
    }

    private fun pauseRecorder() {
        recorder.pause();
        isPaused = true;
        btnRecord.setImageResource(R.drawable.ic_record);
    }

    private fun resumeRecording() {
        recorder.resume();
        isPaused = false;
        btnRecord.setImageResource(R.drawable.ic_pause);
    }
    private fun startRecording() {
        if(!permissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
            return;
        }

        recorder = MediaRecorder()
        dirpath = "${externalCacheDir?.absolutePath}/"

        var simpleDateFormat = SimpleDateFormat("yyyy.MM.DD_hh.mm.ss")
        var date = simpleDateFormat.format(Date());
        filename = "audio_record_$date"

        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // MP3 isnt' avaible so this is best option
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            setOutputFile("$dirpath$filename.mp3"); // we just convert it here

            try {
                prepare()
            } catch(e: IOException) {
                start()
            }
            btnRecord.setImageResource(R.drawable.ic_pause)
            isRecording = true;
            isPaused = false;
        }


    }

    private fun stopRecorder() {
        recorder.apply() {
            stop();
            release();
        }

        isPaused = false;
        isRecording = false;
        btnList.visibility = View.VISIBLE;
        btnDone.visibility = View.VISIBLE;

        btnDelete.isClickable = false;
        btnDelete.setImageResource(R.drawable.ic_delete_disabled);
        btnRecord.setImageResource(R.drawable.ic_record);
        tvTimer.text = "00:00:00";


    }
}

