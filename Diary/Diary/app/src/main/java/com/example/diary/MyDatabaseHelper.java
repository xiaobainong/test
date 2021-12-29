package com.example.diary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    //创建记录日记的日记表
    public static final String CREATE_Diary = "create table Diary ("
            + "id integer primary key autoincrement,author text," +
            "title text, content text,time text)";

    public static final String CREATE_Picture = "create table Picture ("
            + "id integer primary key autoincrement,path0 text," +
            "path1 text, path2 text,path3 text,path4 text)";



    private Context mContext;

    public MyDatabaseHelper(Context context, String name,
                            SQLiteDatabase.CursorFactory
                                    factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_Diary);
        db.execSQL(CREATE_Picture);
        Toast.makeText(mContext,
                "数据库创建成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
