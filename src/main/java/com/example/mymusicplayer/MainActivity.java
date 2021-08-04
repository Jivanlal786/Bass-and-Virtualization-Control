package com.example.mymusicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.BassBoost;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Virtualizer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView musicListView;
    ArrayList<modelSong> modelSongList;
    ListAdapter listAdapter;
    TextView tvStarttime, tvEndTime, tvTitle1;
    SeekBar seekBar;
    //    Handler myHandler = new Handler(getMainLooper());
    double current_pos, total_duration;
    int audio_index = 0;
    MediaPlayer mediaPlayer;
    OnClickListener onClickListener;
    Button btnPlaypause, btnNext, btnPrev;
    SwitchCompat btnBass, btnReverb, btnVirtualizer;
    int sessionId = 0;
    BassBoost bassBoost = null;
    Virtualizer virtualizer = null;
    PresetReverb reverb = null;
    private String TAG = "aaaaaaa";
    SeekBar bassSeekbar, virtualSeekbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        musicListView = findViewById(R.id.musiclistview);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        tvStarttime = findViewById(R.id.tv_starttime);
        tvEndTime = findViewById(R.id.tv_endtime);
        btnPlaypause = findViewById(R.id.btn_playpause);
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);
        tvTitle1 = findViewById(R.id.tv_title1);

        btnBass = findViewById(R.id.btn_bass);
        btnReverb = findViewById(R.id.btn_reverb);
        btnVirtualizer = findViewById(R.id.btn_virtual);

        bassSeekbar = findViewById(R.id.bass_seek);
        virtualSeekbar = findViewById(R.id.virtual_seek);

        modelSongList = new ArrayList<>();
        mediaPlayer = new MediaPlayer();

        onClickListener = new OnClickListener() {
            @Override
            public void itemClick(int position) {
//                mediaPlayer = MediaPlayer.create(MainActivity.this,modelSongList.get(position).getAudioUri());
                audio_index = position;
                playAudio(audio_index);
                seekBar.setClickable(false);

            }
        };

        getSongList();

        setBass();
        setReverb();
        setVirtualizer();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                current_pos = seekBar.getProgress();
                mediaPlayer.seekTo((int) current_pos);
            }
        });

        //    Handler myHandler = new Handler(getMainLooper());

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                audio_index++;
                if (audio_index < (modelSongList.size())) {
                    playAudio(audio_index);
                } else {
                    audio_index = 0;
                    playAudio(audio_index);
                }

            }
        });

        if (!modelSongList.isEmpty()) {
            playAudio(audio_index);
            prevAudio();
            nextAudio();
            setPause();
        }

//        startService();
    }

    public void startService() {
        Intent serviceIntent = new Intent(MainActivity.this, NotificationService.class);
        serviceIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        startService(serviceIntent);
    }

    public void getSongList() {
        //retrieve song info
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        //looping through all rows and adding to list
        if (cursor != null && cursor.moveToFirst()) {
            do {

                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                modelSong model = new modelSong(0, title, artist, Uri.parse(url), duration);

                modelSongList.add(model);

            } while (cursor.moveToNext());
        }
        listAdapter = new ListAdapter(modelSongList, MainActivity.this, onClickListener);
        LinearLayoutManager lm = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        musicListView.setLayoutManager(lm);
        musicListView.setAdapter(listAdapter);

    }

    //play audio file
    public void playAudio(int pos) {
        try {
            mediaPlayer.reset();
            //set file path
            mediaPlayer.setDataSource(this, modelSongList.get(pos).getAudioUri());
            mediaPlayer.prepare();
            mediaPlayer.start();
            btnPlaypause.setText("Pause");
            tvTitle1.setText(modelSongList.get(pos).getTitle());
            audio_index = pos;
        } catch (Exception e) {
            e.printStackTrace();
        }
        setAudioProgress();
        if (mediaPlayer != null) {
            sessionId = mediaPlayer.getAudioSessionId();
        }
    }

    //set audio progress
    public void setAudioProgress() {
        //get the audio duration
        current_pos = mediaPlayer.getCurrentPosition();
        total_duration = mediaPlayer.getDuration();

        //display the audio duration
        tvEndTime.setText(timerConversion((long) total_duration));
        tvStarttime.setText(timerConversion((long) current_pos));
        seekBar.setMax((int) total_duration);
        final Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    current_pos = mediaPlayer.getCurrentPosition();
                    tvStarttime.setText(timerConversion((long) current_pos));
                    seekBar.setProgress((int) current_pos);
                    handler.postDelayed(this, 1000);
                } catch (IllegalStateException ed) {
                    ed.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    //play previous audio
    public void prevAudio() {
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audio_index > 0) {
                    audio_index--;
                    playAudio(audio_index);
                } else {
                    audio_index = modelSongList.size() - 1;
                    playAudio(audio_index);
                }
                if (mediaPlayer != null) {
                    sessionId = mediaPlayer.getAudioSessionId();
                }
            }
        });
    }

    //play next audio
    public void nextAudio() {
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audio_index < (modelSongList.size() - 1)) {
                    audio_index++;
                    playAudio(audio_index);
                } else {
                    audio_index = 0;
                    playAudio(audio_index);
                }
                if (mediaPlayer != null) {
                    sessionId = mediaPlayer.getAudioSessionId();
                }
            }
        });
    }

    //pause audio
    public void setPause() {
        btnPlaypause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    btnPlaypause.setText("Play");
                } else {
                    mediaPlayer.start();
                    btnPlaypause.setText("Pause");
                }

                if (mediaPlayer != null) {
                    sessionId = mediaPlayer.getAudioSessionId();
                }
            }
        });
    }

    public void setBass() {

        btnBass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (bassBoost != null) {
                    bassBoost.release();
                }
                bassBoost = new BassBoost(0, sessionId);
                Log.d(TAG, "getRoundedStrength(): " + bassBoost.getRoundedStrength());
                if (bassBoost.getStrengthSupported()) {
                    bassBoost.setStrength((short) 1000);
                }

                if (isChecked) {
                    int result = bassBoost.setEnabled(isChecked);
                    if (result != AudioEffect.SUCCESS) {
                        Log.e(TAG, "Bass Boost: setEnabled(" + isChecked + ") = " + result);
                    } else {
                        bassBoost.setEnabled(isChecked);
                    }
                }
                Toast.makeText(MainActivity.this, "bassboost :- "+bassBoost.getEnabled(), Toast.LENGTH_SHORT).show();
            }
        });

        bassSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (bassBoost != null) {
                    if (progress > 0 && progress <= 1000) {
                        bassBoost.setStrength((short) progress);
                        System.out.println(TAG+" : "+bassBoost.getRoundedStrength());
                    }
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

    public void setReverb() {

        btnReverb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (reverb != null) {
                    reverb.release();
                }

                reverb = new PresetReverb(0, sessionId);
                int effectId = reverb.getId();
                reverb.setPreset(PresetReverb.PRESET_LARGEHALL);
//                // Send a broadcast containing the effect id to Music app to attach auxiliary effect to MediaPlayer instance
//                Intent it_aux = new Intent(ATTACHAUXAUDIOEFFECT);
//                it_aux.putExtra("auxaudioeffectid", mPresetReverb.getId());
//                sendBroadcast(it_aux);
                reverb.setEnabled(true);
//                mediaPlayer.attachAuxEffect(effectId);
//                mediaPlayer.setAuxEffectSendLevel(1.0f);

                if (isChecked) {
                    int result = reverb.setEnabled(isChecked);
                    if (result != AudioEffect.SUCCESS) {
                        Log.e(TAG, "PresetReverb: setEnabled("
                                + isChecked + ") = "
                                + result);
//                        reverb.
                    }
                }
                Toast.makeText(MainActivity.this, "reverb :- "+reverb.getEnabled()+"\n\n"+reverb.getProperties(), Toast.LENGTH_SHORT).show();
                if (!reverb.getEnabled()){
                    reverb.setEnabled(false);
//                    reverb.
                }
            }

        });

        btnPlaypause.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(MainActivity.this, "long clicked", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

    }

    public void setVirtualizer() {

        btnVirtualizer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (virtualizer != null) {
                    virtualizer.release();
                }
                virtualizer = new Virtualizer(0, sessionId);
                virtualizer.setStrength((short) 1000);

                int result = virtualizer.setEnabled(isChecked);
                if (result != AudioEffect.SUCCESS) {
                    Log.e(TAG, "Virtualizer: setEnabled(" + isChecked + ") = " + result);
                }
                Toast.makeText(MainActivity.this, "virtual :- "+virtualizer.getEnabled(), Toast.LENGTH_SHORT).show();
            }
        });

        virtualSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (virtualizer != null) {

                    if (progress > 0 && progress <= 1000) {
                        virtualizer.setStrength((short) progress);
                        System.out.println(TAG+" : "+virtualizer.getRoundedStrength());
                    }
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


    //time conversion
    public String timerConversion(long value) {
        String audioTime;
        int dur = (int) value;
        int hrs = (dur / 3600000);
        int mns = (dur / 60000) % 60000;
        int scs = dur % 60000 / 1000;

        if (hrs > 0) {
            audioTime = String.format("%02d:%02d:%02d", hrs, mns, scs);
        } else {
            audioTime = String.format("%02d:%02d", mns, scs);
        }
        return audioTime;
    }

    //release mediaplayer
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }


        if (bassBoost != null) {
            bassBoost.release();
        }
        if (virtualizer != null) {
            virtualizer.release();
        }
        if (reverb != null) {
            reverb.release();
        }
    }
}