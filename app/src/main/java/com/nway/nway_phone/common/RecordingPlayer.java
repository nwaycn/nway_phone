package com.nway.nway_phone.common;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

public class RecordingPlayer extends MediaPlayer {
    private static final String TAG="recordingPlayer";
    private MediaPlayer mediaPlayer;
    private int playing = 0; //0=未播放，1=播放中，2=暂停,3=播放完毕

    public RecordingPlayer(Context context,int resid){
            mediaPlayer = MediaPlayer.create(context,resid);
    }

    public RecordingPlayer(Context context, Uri uri){
        mediaPlayer = MediaPlayer.create(context,uri);
    }

    public RecordingPlayer(Context context, String localFile){
        if(localFile == null || localFile.equals("")){
            return;
        }
        Uri uri = Uri.parse(localFile);
        if (mediaPlayer != null){
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(context,uri);

    }

    public MediaPlayer getMediaPlayer(){
        return mediaPlayer;
    }

    public void start(){
        playing = 1;
        mediaPlayer.start();
    }

    public int getCurrentPosition(){
        if(mediaPlayer == null){
            return 0;
        }
       return  mediaPlayer.getCurrentPosition();
    }

    public void pause(){
        playing = 2;
        if(mediaPlayer != null){
            mediaPlayer.pause();
        }
    }


    public void stop(){
        playing = 3;
        if(mediaPlayer != null){
            mediaPlayer.stop();
        }

    }

    public int playStatus(){
        return playing;
    }
}
