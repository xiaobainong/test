package com.example.musicplayer_yu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private boolean isClick=true;//判断当前“管理”按钮是否被点击状态
    private int[] flag = {0};
    private int clickzhiling=0,modezhiling=0,listzhiling=0;//clickzhiling判断“管理”是否被点击
    private ArrayList<Map<String,Object>> my_List,all_list;
    private ListView lv;
    private TextView nowMusicName,showAll,myList,manage,nowProgress,allProgress;
    private SeekBar seekBar;
    private Button bt_last,bt_playStop,bt_next;
    private MediaPlayer mediaPlayer;
    private int[] music = {R.raw.song1,R.raw.song2,R.raw.song3,R.raw.song4,R.raw.song5};
    private String[] s_name={"张三的歌","恋曲1990","挪威的森林","走样","野百合也有春天"},
            s_id={"1","2","3","4","5"},s_author={"李寿全","罗大佑","伍佰","张宇","罗大佑"};
    private int temp=0,mytemp=0,insertemp=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaPlayer = new MediaPlayer();

        seekBar = findViewById(R.id.mSeekbar);

        nowMusicName = findViewById(R.id.nowMusic);

        my_List = new ArrayList<Map<String, Object>>();//实例化我的歌单
        lv = findViewById(R.id.musicList);
        nowProgress = findViewById(R.id.nowProgress);
        allProgress = findViewById(R.id.allProgress);
        setListview(showAllList());
        new myThread().start();

        chushihua(temp);

        String[] mysong_name = new String[10],mysong_author=new String[10];
        int[] my_music = new int[10];
        for (int num = 0 ; num<my_List.size() ; num++){
            Map<String, Object> item1= my_List.get(num);
            mysong_name[num] = String.valueOf(item1.get("songs"));
            mysong_author[num] = String.valueOf(item1.get("author"));
            //my_music[num] = (Integer) item1.get("music");
        }


        //设置进度条可拖动，并同时调节media
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int all = mediaPlayer.getDuration() / 1000;
                int now = mediaPlayer.getCurrentPosition() / 1000;
                nowProgress.setText(calculateTime(now));
                allProgress.setText(calculateTime(all));
                if(fromUser){
                    mediaPlayer.seekTo(progress);
                }
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        Spinner spinner = findViewById(R.id.mode);
        final ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,R.array.items,R.layout.support_simple_spinner_dropdown_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_item);;
        spinner.setAdapter(adapter1);
        spinner.setSelection(flag[0]);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String choice = (String)adapter1.getItem(i);
                switch (choice)
                {
                    case "Order":
                        modezhiling=0;
                        autoplay();
                        Toast.makeText(MainActivity.this, "顺序播放", Toast.LENGTH_LONG).show();
                        break;
                    case "Random":
                        modezhiling=1;
                        autoplay();
                        Toast.makeText(MainActivity.this, "随机播放", Toast.LENGTH_LONG).show();
                        break;
                    case "Single":
                        modezhiling=2;
                        autoplay();
                        Toast.makeText(MainActivity.this, "单曲循环", Toast.LENGTH_LONG).show();
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        bt_playStop = findViewById(R.id.play_stop);
        bt_playStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer!=null&&!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                }else{
                    mediaPlayer.pause();
                }
            }
        });

        bt_last = findViewById(R.id.last);
        bt_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(temp>0){
                    temp = temp-1;
                    mediaPlayer.reset();
                    mediaPlayer=MediaPlayer.create(MainActivity.this,music[temp]);
                    nowMusicName.setText("正在播放："+s_name[temp]);
                    mediaPlayer.start();
                    seekBar.setMax(mediaPlayer.getDuration());
                }else {
                    temp=music.length-1;
                    mediaPlayer.reset();
                    mediaPlayer=MediaPlayer.create(MainActivity.this,music[music.length-1]);
                    nowMusicName.setText("正在播放："+s_name[temp]);
                    mediaPlayer.start();
                    seekBar.setMax(mediaPlayer.getDuration());
                }
            }
        });

        bt_next = findViewById(R.id.next);
        bt_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(temp<music.length-1){
                    temp = temp+1;
                    mediaPlayer.reset();
                    mediaPlayer=MediaPlayer.create(MainActivity.this,music[temp]);
                    nowMusicName.setText("正在播放："+s_name[temp]);
                    mediaPlayer.start();
                    seekBar.setMax(mediaPlayer.getDuration());
                }else {
                    temp=0;
                    mediaPlayer.reset();
                    mediaPlayer=MediaPlayer.create(MainActivity.this,R.raw.song1);
                    nowMusicName.setText("正在播放："+s_name[0]);
                    mediaPlayer.start();
                    seekBar.setMax(mediaPlayer.getDuration());
                }
            }
        });

        showAll = findViewById(R.id.showAllList);
        showAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setListview(showAllList());
                listzhiling=0;
            }
        });

        myList = findViewById(R.id.showMyList);
        myList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setListview(my_List);
                listzhiling=1;
            }
        });

        manage = findViewById(R.id.manage);
        manage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isClick == true){
                    clickzhiling = 1;
                    isClick=false;
                    manage.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

                }else{
                    clickzhiling = 0;
                    isClick=true;
                    manage.setTypeface(null, Typeface.NORMAL);

                }
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (clickzhiling==0){
                    Toast.makeText(MainActivity.this, "用户名:", Toast.LENGTH_LONG).show();
                    mediaPlayer.reset();
                    mediaPlayer=MediaPlayer.create(MainActivity.this,music[i]);
                    nowMusicName.setText(s_name[i]);
                    mediaPlayer.start();
                    seekBar.setMax(mediaPlayer.getDuration());//设置进度条长度，参数为当前mediaPlayer播放的资源最大长度
                    temp = i;
                } else if (clickzhiling==1){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("是否将"+s_name[i]+"添加到我的歌单");
                    builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_LONG).show();
                        }
                    });
                    builder.setNeutralButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int j) {
                                Map<String, Object> item = new HashMap<String, Object>();
                                item.put("id", s_id[i]);
                                item.put("songs", s_name[i]);
                                item.put("author", s_author[i]);
                                my_List.add(item);
                        }
                    });
                    builder.create().show();
                }
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return true;
            }
        });



    }


    public void autoplay() {
        Toast.makeText(MainActivity.this, "auto", Toast.LENGTH_LONG).show();
        if (modezhiling == 0) {//顺序播放
            if (temp < music.length ) {
                for (int i = temp-1; i < music.length ; i++) {
                    temp = temp + 1;
                    mediaPlayer.reset();
                    mediaPlayer = MediaPlayer.create(MainActivity.this, music[temp-1]);
                    nowMusicName.setText("正在播放：" + s_name[temp-1]);
                    mediaPlayer.start();
                    seekBar.setMax(mediaPlayer.getDuration());
                    break;
                }
            } else if (temp==music.length){
                temp = 0;
                mediaPlayer.reset();
                mediaPlayer = MediaPlayer.create(MainActivity.this, music[0]);
                nowMusicName.setText("正在播放：" + s_name[0]);
                mediaPlayer.start();
                seekBar.setMax(mediaPlayer.getDuration());
            }
        }else if (modezhiling==1){//随机播放

            int ran = (int)(Math.random()*music.length);
            mediaPlayer.reset();
            mediaPlayer = MediaPlayer.create(MainActivity.this, music[ran]);
            nowMusicName.setText("正在播放：" + s_name[ran]);
            mediaPlayer.start();
            seekBar.setMax(mediaPlayer.getDuration());
        }else if (modezhiling==2){
            boolean reverse = mediaPlayer.isLooping();
            mediaPlayer.setLooping(!reverse);
            if (!reverse) {
                mediaPlayer.start();

            } else {
                mediaPlayer.stop();
            }
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            public void onCompletion(MediaPlayer mp) {
                autoplay();

            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return true;
            }
        });
    }



    public ArrayList<Map<String,Object>> showAllList()
    {
        ArrayList<Map<String,Object>> list = new ArrayList<>();
        for(int i=0;i<music.length;i++) {
                Map<String,Object> item=new HashMap<String,Object>();
                item.put("id", s_id[i]);
                item.put("songs", s_name[i]);
                item.put("author",s_author[i]);
                item.put("music",music[i]);
                list.add(item);
        }
        return list;
    }

    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

    }

    //计算时间
    public String calculateTime(int time){
        int minute;
        int second;
        if(time > 60){
            minute = time / 60;
            second = time % 60;
            //判断秒
            if(second >= 0 && second < 10){
                return "0"+minute+":"+"0"+second;
            }else {
                return "0"+minute+":"+second;
            }
        }else if(time < 60){
            second = time;
            if(second >= 0 && second < 10){
                return "00:"+"0"+second;
            }else {
                return "00:"+ second;
            }
        }else{
            return "01:00";
        }
    }

    private void setListview(ArrayList<Map<String, Object>> items) {
        SimpleAdapter adapter = new SimpleAdapter(this, items, R.layout.item,
                new String[]{"id", "songs", "author"},
                new int[]{R.id.song_id, R.id.song_name, R.id.song_author});
        lv.setAdapter(adapter);
    }

    public void chushihua(int i){
        nowMusicName.setText(s_name[i]);
        if(mediaPlayer != null){
            mediaPlayer.stop();
        }

        mediaPlayer = MediaPlayer.create(this,music[i]);
        seekBar.setMax(mediaPlayer.getDuration());;
    }


    class myThread extends Thread{
        @Override
        public void run()
        {
            super.run();
            while(seekBar.getProgress()<=seekBar.getMax())
            {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
            }
        }
    }
}
