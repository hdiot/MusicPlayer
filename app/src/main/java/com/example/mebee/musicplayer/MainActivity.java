package com.example.mebee.musicplayer;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {

    MediaPlayer MP;
    Button Start;
    Button Stop;
    Button Pause;
    ProgressBar TimeProgress;
    Thread GetTimeThread;
    int MusicTime = 0;
    int CurrentTime = 0;
    boolean IsPause = true;

    // 设置跨线程系信息传输
    Handler TmieHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            CurrentTime = (int) msg.obj;
            TimeProgress.setProgress(CurrentTime/MusicTime);
            System.out.println(msg.obj+"---"+ MusicTime);

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        setListaner();
    }

    /**
     * 初始化 MediaPlayer对象
     */
    private void setMediaPlayer() {
        MP = MediaPlayer.create(this,R.raw.a);
        MusicTime = MP.getDuration()/100;
    }

    /**
     * 初始化控件
     */
    private void findView() {
        Start = (Button) findViewById(R.id.start);
        Stop = (Button) findViewById(R.id.stop);
        Pause = (Button) findViewById(R.id.pause);
        TimeProgress = (ProgressBar) findViewById(R.id.progress);
    }

    /**
     * 设置控件点击监听
     */
    private void setListaner() {
        Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 重置MediaPlayer,避免开启多个播放线程
                if (MP != null) {
                    MP.reset();
                }
                // 设置/初始化MediaPlayer
                setMediaPlayer();
                // 开启播放
                MP.start();
                // 设置释放停止播放状态为 false
                IsPause = false;
                // 判断时间监听线程是否为空，是则开启播放时间时间监听
                if (GetTimeThread == null){
                    getTime();
                }
            }
        });

        Stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 将停止播放状态设为 True
                IsPause = true;
                // 判断是否正在播放，是则停止播放并释放 MediaPlayer 对象
                if (MP.isPlaying()){
                    MP.stop();
                    MP.release();
                }
            }
        });

        Pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // 是否已暂停播放，是则继续从上次停止的时间开始播放，否则暂停播放
                if (IsPause){
                    MP.seekTo(CurrentTime);
                    MP.start();
                    getTime();
                } else {
                    MP.pause();

                }
                // 播放状态取反
                IsPause = !IsPause;
            }
        });
    }


    /**
     * 监听播放进度
     */
    private void getTime(){
        // 开启播放进度监听线程
        GetTimeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!IsPause){
                    MP.getCurrentPosition();
                    Message  message = new Message();
                    message.obj = MP.getCurrentPosition();
                    TmieHandler.sendMessage(message);
                }
            }
        });

        GetTimeThread.start();
    }
}
