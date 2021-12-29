package com.example.diary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //对数据库的创建和管理
    private MyDatabaseHelper dbHelper;
    //作者
    private TextView author;
    //需要加载的日记
    private final List<Diary> storeListDiary =new ArrayList<>();
    //需要删除的日记
    private final List<Diary> deleteListDiary =new ArrayList<>();
    //日记视图
    private ListView listViewDiary ;
    //日记适配器
    private  DiaryAdapter diaryAdapter;
    //复选框的id和点击次数
    HashMap<Integer,Integer> checkBoxId=new HashMap<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //作者框
       author = findViewById(R.id.textview_author);

        //取出SharedPreferences里面作者的名字
        restoreAuthor();

        //作者框监听器
        author.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到编辑作者的界面
                Intent intent = new Intent(MainActivity.this, AuthorInfoActivity.class);
                //把作者名字传到第二个活动
                intent.putExtra("author",author.getText().toString());
                startActivity(intent);
            }
        });

        //获取日记列表视图
        listViewDiary = findViewById(R.id.list_view);

        //加载日记
        loadDiaryList();
        //多选模式
        listViewDiary.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        //监听是否点击某条日记
        listViewDiary.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Diary diary = diaryAdapter.getItem(position);
                //跳转到添加日记的界面
                Intent intent = new Intent(MainActivity.this, AddDiaryActivity.class);
                //用于区别是传送数据
                intent.putExtra("function", 1);
                //传输id
                intent.putExtra("diaryId", diary.getId()+"");
                System.out.println(diary.getId());
                startActivity(intent);

            }
        });

        //长按监听
        listViewDiary.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
                //获得选框
                CheckBox cb=arg1.findViewById(R.id.chb_select);
                //没有id(第一次点击)
                if (!checkBoxId.containsKey(arg2))
                    checkBoxId.put(arg2,1);

                //模2余1，需要显示
                if(checkBoxId.get(arg2) % 2 == 1) {

                    checkBoxId.put(arg2,checkBoxId.get(arg2)+1);
                    //设置按钮可见
                    cb.setVisibility(View.VISIBLE);
                    //设置被选中
                    cb.setChecked(true);
                    //获得选中的日记
                    Diary diary = diaryAdapter.getItem(arg2);
                    //加入到需要删除的日记中
                    deleteListDiary.add(diary);
                }
                else {
                    checkBoxId.put(arg2,checkBoxId.get(arg2)+1);
                    arg1.findViewById(R.id.chb_select).setVisibility(View.INVISIBLE);
                    }
                return true;
            }
        });


        //点击“添加日记”按钮
        Button addDiary = findViewById(R.id.add_diary);
        addDiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到添加日记的界面
                Intent intent = new Intent(MainActivity.this, AddDiaryActivity.class);
                //把作者名字传到第二个活动
                intent.putExtra("author",author.getText());
                intent.putExtra("function", 2);
                System.out.println(author.getText());
                startActivity(intent);
            }
        });


        //点击“删除日记”按钮
        Button deleteDiary = findViewById(R.id.delete_diary);
        deleteDiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取数据库的读写对象
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                //删除选中的日记
                for(int i=0;i<deleteListDiary.size();i++){
                    Diary diary=deleteListDiary.get(i);
                    String id =diary.getId()+"";
                    db.delete("Diary", "id=?", new String[]{id});
                    db.delete("Picture","id=?", new String[]{id});
                    deleteListDiary.remove(i);
                }
                db.close();
                //重新加载日记
                loadDiaryList();
                //刷新
                checkBoxId.clear();

            }
        });

    }

    /**
     * 导入数据库中的日记到ListView
     */
    public void loadDiaryList(){
        storeListDiary.removeAll(storeListDiary);
        //数据库存在时打开数据库，不存在时创建数据库
        dbHelper = new MyDatabaseHelper(this,"Diary.db",null,1);
        //1.获取可以写⼊的数据库对象
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //2.查询Diary表中数据
        Cursor cursor = db.query("Diary",null, null, null, null, null, null);
        //3.适配器
        if (cursor.moveToFirst()) {
            do {
                Diary diary=new Diary();
                //遍历Cursor对象，取出数据
               diary.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
               diary.setAuthor(cursor.getString(cursor.getColumnIndexOrThrow("author")));
               diary.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
               diary.setContent(cursor.getString(cursor.getColumnIndexOrThrow("content")));
               diary.setTime(cursor.getString(cursor.getColumnIndexOrThrow("time")));
               storeListDiary.add(diary);
            }while (cursor.moveToNext());
        }
        cursor.close();
        diaryAdapter = new DiaryAdapter(this,R.layout.list_diary_item,storeListDiary);
        listViewDiary.setAdapter(diaryAdapter);
    }


    /**
     *加载作者姓名，SharedPreferences里有作者姓名，获取，没有默认doris
     */
    private void restoreAuthor(){
        SharedPreferences prefs = getSharedPreferences("authorInfo", MODE_PRIVATE);
        //获取姓名
        String name = prefs.getString("name", "doris");
        author.setText(name);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //重新加载作者
        restoreAuthor();
        //重新获取数据
        loadDiaryList();
        //刷新
        checkBoxId.clear();

    }
}
