package com.example.mechrevo.media;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private MediaPlayer mediaPlayer;
    private TextView allTime;
    private TextView playTime;
    private TextView songTime;
    private SeekBar seekBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    private void initView(){
        playTime=(TextView)findViewById(R.id.played_time);
        allTime=(TextView)findViewById(R.id.all_time);
        songTime=(TextView)findViewById(R.id.song_name);
        seekBar=(SeekBar)findViewById(R.id.seek_bar);
        Button stop=(Button)findViewById(R.id.stop);
        Button play=(Button)findViewById(R.id.play);
        Button pause=(Button)findViewById(R.id.pause);
        stop.setOnClickListener(this);
        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser==true){
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    private void initMediaPlayer(){
        mediaPlayer=new MediaPlayer();
        Uri uri=Uri.parse("http://192.168.1.115:8083/music/music/music2.mp3");
        try{
            mediaPlayer.setDataSource(this,uri);
            mediaPlayer.prepare();
            seekBar.setMax(mediaPlayer.getDuration());
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case  R.id.play:
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                    Message message=handler.obtainMessage();
                    message.what=1;
                    message.arg1=mediaPlayer.getDuration();
                    handler.sendMessage(message);
                    handler.post(updateThread);
                }
                break;
            case R.id.pause:
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    handler.removeCallbacks(updateThread);
                }
                break;
            case R.id.stop:
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    initMediaPlayer();
                    handler.removeCallbacks(updateThread);
                    handler.sendEmptyMessage(3);
                }
                break;
            default:
                break;
        }
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
    }
    private Handler handler=new Handler(){
       public void handlerMessage(Message msg){
           super.handleMessage(msg);
           switch (msg.what){
               case 1:
                   allTime.setText(msg.arg1/60000+":"+msg.arg1/1000%6);
                   break;
               case 3:
                   allTime.setText("00:00");
                   playTime.setText("00:00");
                   seekBar.setProgress(0);
                   break;
           }
       }
    };
    Runnable updateThread=new Runnable() {
        @Override
        public void run() {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            playTime.setText(mediaPlayer.getCurrentPosition()/60000+":"+mediaPlayer.getCurrentPosition()/1000%60);
            handler.postDelayed(updateThread,100);
        }
    };
    public void getPermission(){
        if(Build.VERSION.SDK_INT>=23){
            int checkPermission= ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (checkPermission!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},111);
                return;
            }
            else {
                initMediaPlayer();
            }
        }else {
            initMediaPlayer();
        }
    }
    public void OnRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 111:
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"获取权限成功",Toast.LENGTH_SHORT)
                            .show();
                    initMediaPlayer();
                }else {
                    Toast.makeText(this,"获取权限失败",Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }
}
