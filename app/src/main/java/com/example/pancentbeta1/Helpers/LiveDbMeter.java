package com.example.pancentbeta1.Helpers;
import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;
import static androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;





import com.example.pancentbeta1.CalibrationActivity;

public class LiveDbMeter {

    private static final int SAMPLE_RATE = 44100; // Hz
    private static final int BUFFER_SIZE = 4096; // Samples
    private static final double BANDWIDTH = 100; // Hz (Adjust as needed)
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1984;

    private AudioRecord audioRecord;
    private boolean isRecording;



    String[] permissions;




    public LiveDbMeter(Context context) {

        if(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)== PackageManager.PERMISSION_GRANTED) {

            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    BUFFER_SIZE * 2
            );
        }
        else {
        }


    }


    
    
    public void start() {
        if(!isRecording) {
            isRecording = true;

            audioRecord.startRecording();


            new Thread(() -> {
                while (isRecording) {
                    short[] buffer = new short[BUFFER_SIZE];
                    audioRecord.read(buffer, 0, BUFFER_SIZE);

                    // Convert to double array for filtering
                    double[] audioData = new double[BUFFER_SIZE];
                    for (int i = 0; i < BUFFER_SIZE; i++) {
                        audioData[i] = buffer[i];
                    }

                    // Apply the goertzel algorythm as frequency isolator & calculate rms level at 800(±50)(hz) and at 600(±50)(hz)
                    double rms800 = goertzel(audioData, 800, SAMPLE_RATE) / 32767.0;

                    double rms600 = goertzel(audioData, 600, SAMPLE_RATE) / 32767.0;

                    double prog = 50 * (rms600 / rms800);//place in file 600 pan to right and 800 pan to left

                    CalibrationActivity.getInstance().updateRMS(prog);
                }
            }).start();
        }
    }

    public void stop() {
        if(isRecording) {
            isRecording = false;
            audioRecord.stop();
            audioRecord.release();
        }
    }



    private double goertzel(double[] data, double targetFrequency, double sampleRate) {
        int k = (int) (0.5 + ((data.length * targetFrequency) / sampleRate));
        double omega = (2.0 * Math.PI * k) / data.length;
        double cosine = Math.cos(omega);
        double sine = Math.sin(omega);
        double coeff = 2.0 * cosine;

        double q0 = 0;
        double q1 = 0;
        double q2 = 0;

        for (int i = 0; i < data.length; i++) {
            q0 = coeff * q1 - q2 + data[i];
            q2 = q1;
            q1 = q0;
        }

        double real = (q1 - q2 * cosine);
        double imag = (q2 * sine);
        double magnitude = Math.sqrt(real * real + imag * imag);

        return magnitude;
    }
}