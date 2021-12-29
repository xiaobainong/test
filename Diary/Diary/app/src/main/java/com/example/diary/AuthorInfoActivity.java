package com.example.diary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AuthorInfoActivity extends AppCompatActivity {
    private TextView author_name;
    private TextView author_age;
    private TextView author_other;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author_info);

        //获得作者信息
        loadAuthorInfo();

        //保存
        Button btn_save=findViewById(R.id.btn_save);
        btn_save.setOnClickListener(v -> {
            //保存作者
            saveAuthor();
        });

    }

    /**
     * 获得作者信息
     */
    private void loadAuthorInfo(){
        SharedPreferences prefs = getSharedPreferences("authorInfo", MODE_PRIVATE);

        //作者姓名
        author_name=findViewById(R.id.author_name);
        author_name.setText(getIntent().getStringExtra("author"));

        //获取年龄
        author_age=findViewById(R.id.author_age);
        int age = prefs.getInt("age",0);
        author_age.setText(age+"");

        //
        author_other=findViewById(R.id.author_other);
        String other = prefs.getString("other", "");
        author_other.setText(other);

    }

    /**
     * 保存作者信息
     */
    private void saveAuthor(){
        SharedPreferences.Editor editor = getSharedPreferences("authorInfo", MODE_PRIVATE).edit();
        //写入姓名
        editor.putString("name", author_name.getText().toString());

        if(TextUtils.isEmpty(author_age.getText().toString()))
            editor.putInt("age", 0);
        else
            editor.putInt("age", Integer.parseInt(author_age.getText().toString()));

        editor.putString("other",author_other.getText().toString());
        editor.apply();
        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
    }

}