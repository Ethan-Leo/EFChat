package com.rance.chatui.util;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class AudioRecorderUtils {

    //file path
    private String filePath;
    //folder path
    private String FolderPath;

    private MediaRecorder mMediaRecorder;
    private final String TAG = "EFChat";
    public static final int MAX_LENGTH = 1000 * 60 * 10;// Max recording time up to 1000*60*10;

    private OnAudioStatusUpdateListener audioStatusUpdateListener;

    /**
     * file is saved as sdcard/cadyd/record by default
     */
    public AudioRecorderUtils() {

        //Default location of the file is in /sdcard/record/
        this(Environment.getStorageDirectory() + "/cadyd/record/");
    }

    public AudioRecorderUtils(String filePath) {

        File path = new File(filePath);
        if (!path.exists())
            path.mkdirs();

        this.FolderPath = filePath;
    }

    private long startTime;
    private long endTime;

    /**
     * Start recording using amr format
     * recording file
     *
     * @return
     */
    public void startRecord(Context context) {
        if (!CheckPermissionUtils.isHasPermission(context)) {
            audioStatusUpdateListener.onError();
            return;
        }
        // Start to record
        /* ①Initial：Create the instance of Object MediaRecorder */
        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();
        try {
            /* ②setAudioSource/setVedioSource */
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// Set Microphone
            /* ②set the encoding mode of audio file：AAC/AMR_NB/AMR_MB/Default sample of sound (sound wave) */
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            /*
             * ②Set the format of the exported file：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp
             * ，H263/ARM)、MPEG-4、RAW_AMR(Only supports audio and the encoding of it should be AMR_NB)
             */
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            filePath = FolderPath + Utils.getCurrentTime() + ".amr";
            /* ③Ready */
            mMediaRecorder.setOutputFile(filePath);
            mMediaRecorder.setMaxDuration(MAX_LENGTH);
            mMediaRecorder.prepare();
            /* ④Start */
            mMediaRecorder.start();
            // AudioRecord audioRecord.
            /* record the starting time* */
            startTime = System.currentTimeMillis();
            updateMicStatus();
            Log.e("fan", "startTime" + startTime);
        } catch (IllegalStateException e) {
            audioStatusUpdateListener.onError();
            Log.i(TAG, "call startAmr(File mRecAudioFile) failed!" + e.getMessage());
        } catch (IOException e) {
            audioStatusUpdateListener.onError();
            Log.i(TAG, "call startAmr(File mRecAudioFile) failed!" + e.getMessage());
        }
    }

    /**
     * Stop recording
     */
    public long stopRecord() {
        if (mMediaRecorder == null)
            return 0L;
        endTime = System.currentTimeMillis();
        mMediaRecorder.setOnErrorListener(null);
        mMediaRecorder.setPreviewDisplay(null);
        try {
            mMediaRecorder.stop();
        } catch (IllegalStateException e) {
            Log.d("stopRecord", e.getMessage());
        } catch (RuntimeException e) {
            Log.d("stopRecord", e.getMessage());
        } catch (Exception e) {
            Log.d("stopRecord", e.getMessage());
        }
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;
        long time = endTime - startTime;
        audioStatusUpdateListener.onStop(time, filePath);
        filePath = "";
        return endTime - startTime;
    }

    /**
     * Cancel recording
     */
    public void cancelRecord() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        File file = new File(filePath);
        if (file.exists())
            file.delete();
        filePath = "";

    }

    private final Handler mHandler = new Handler();
    private Runnable mUpdateMicStatusTimer = new Runnable() {
        public void run() {
            updateMicStatus();
        }
    };


    private int BASE = 1;
    private int SPACE = 100;// sampling time gap

    public void setOnAudioStatusUpdateListener(OnAudioStatusUpdateListener audioStatusUpdateListener) {
        this.audioStatusUpdateListener = audioStatusUpdateListener;
    }

    /**
     * 更新麦克状态
     */
    private void updateMicStatus() {

        if (mMediaRecorder != null) {
            double ratio = (double) mMediaRecorder.getMaxAmplitude() / BASE;
            double db = 0;// 分贝
            if (ratio > 1) {
                db = 20 * Math.log10(ratio);
                if (null != audioStatusUpdateListener) {
                    audioStatusUpdateListener.onUpdate(db, System.currentTimeMillis() - startTime);
                }
            }
            mHandler.postDelayed(mUpdateMicStatusTimer, SPACE);
        }
    }

    public interface OnAudioStatusUpdateListener {
        /**
         * 录音中...
         *
         * @param db   当前声音分贝
         * @param time 录音时长
         */
        public void onUpdate(double db, long time);

        /**
         * 停止录音
         *
         * @param time     录音时长
         * @param filePath 保存路径
         */
        public void onStop(long time, String filePath);

        /**
         * 录音失败
         */
        public void onError();
    }

}
