package com.example.xiaoyangmusic;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicService extends Service {
    public static boolean isStarted = false;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private MediaPlayer mPlayer;
    private List<Song> list;
    private TextView name;
    private TextView play;
    private int index = 0;
    private ArrayList<Integer> lastIndexList;


    @SuppressLint("RtlHardcoded")
    @Override
    public void onCreate() {
        super.onCreate();
        lastIndexList = new ArrayList<>();
        isStarted = true;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = 400;
        layoutParams.height = 200;
        layoutParams.x = 100;
        layoutParams.y = 100;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        list = MusicUtils.getMusicData(MusicService.this);
        showFloatingWindow();
        return super.onStartCommand(intent, flags, startId);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showFloatingWindow() {
        if (Settings.canDrawOverlays(this)) {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            View displayView = layoutInflater.inflate(R.layout.service_music, null);
            displayView.setOnTouchListener(new FloatingOnTouchListener());
            displayView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
            TextView last = displayView.findViewById(R.id.last);
            TextView next = displayView.findViewById(R.id.next);
            play = displayView.findViewById(R.id.play);
            name = displayView.findViewById(R.id.name);
            windowManager.addView(displayView, layoutParams);
            if (list != null) {
                play();
                last.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (index == 0)
                            return;
                        play.setText("暂停");
                        lastSong();

                    }
                });
                next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (index == list.size() - 1)
                            return;
                        play.setText("暂停");
                        nextSong();
                    }
                });
                play.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mPlayer != null && mPlayer.isPlaying()) {
                            mPlayer.pause();
                            play.setText("播放");
                        } else if (mPlayer != null && !mPlayer.isPlaying()) {
                            mPlayer.start();
                            play.setText("暂停");
                        }
                    }
                });
            }
        }
    }

    private void play() {
        try {
            mPlayer = new MediaPlayer();
            Random r = new Random();
            index = r.nextInt(list.size());
            lastIndexList.add(index);
            name.setText(list.get(index).song);
            mPlayer.setDataSource(list.get(index).path);
            mPlayer.prepareAsync();
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    nextSong();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void nextSong() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer.release();
        }
        mPlayer = null;
        mPlayer = new MediaPlayer();
        Random r = new Random();
        index = r.nextInt(list.size());
        lastIndexList.add(index);
        name.setText(list.get(index).song);
        try {
            mPlayer.setDataSource(list.get(index).path);
            mPlayer.prepareAsync();
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    nextSong();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void lastSong() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer.release();
        }
        mPlayer = null;
        mPlayer = new MediaPlayer();
        int lastIndex = index;
        if (lastIndexList.size() >= 2) {
            lastIndexList.remove(lastIndexList.size() - 1);
        }
        if (lastIndexList.size() >= 1) {
            lastIndex = lastIndexList.get(lastIndexList.size() - 1);
        }

        name.setText(list.get(lastIndex).song);
        try {
            mPlayer.setDataSource(list.get(lastIndex).path);
            mPlayer.prepareAsync();
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    nextSong();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int x;
        private int y;
        private boolean isClick;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isClick = false;
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    isClick = true;
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;
                    windowManager.updateViewLayout(view, layoutParams);
                    break;
                default:
                    break;
            }
            return isClick;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }
}
