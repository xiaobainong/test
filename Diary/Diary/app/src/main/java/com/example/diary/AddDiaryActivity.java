package com.example.diary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class AddDiaryActivity extends AppCompatActivity {
    public MyDatabaseHelper dbHelper;
    //是否按下“保存”
    private  boolean isSelected =false;
    //当前活动下日记的id(如果是新建则默认是作者名字)
    private String diaryId;
    //图片列表
    ArrayList<Bitmap> imageBitmapList=new ArrayList<>();
    //文件名列表
    ArrayList<String> fileNameList=new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_diary);

        //作者
        TextView author = findViewById(R.id.author);
        //标题
        EditText title = findViewById(R.id.diary_title);
        //日记内容
        EditText content = findViewById(R.id.edit_diary);
        //时间
        TextView time = findViewById(R.id.time);


        //知道数据从那个函数传来
        int function = getIntent().getIntExtra("function", 0);
        switch (function) {
            //如果是点击日记跳转过来
            case 1:
                isSelected = true;
                //获取日记id
                diaryId = getIntent().getStringExtra("diaryId");
                //数据库存在时打开数据库，不存在时创建数据库
                dbHelper = new MyDatabaseHelper(this, "Diary.db", null, 1);
                //1.获取可以写⼊的数据库对象
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                //2.查询Diary表中数据
                Cursor cursor = db.query("Diary", null, "id=?", new String[]{diaryId}, null, null, null);
                //3.适配器
                if (cursor.moveToFirst()) {
                    do {
                        //查询遍历Cursor对象，显示数据
                        author.setText(cursor.getString(cursor.getColumnIndexOrThrow("author")));
                        title.setText(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                        content.setText(cursor.getString(cursor.getColumnIndexOrThrow("content")));
                        time.setText(cursor.getString(cursor.getColumnIndexOrThrow("time")));

                    } while (cursor.moveToNext());
                }
                cursor.close();
                //获取图片
                requirePicture();
                break;
            //如果是点击“添加日记按钮”跳转来
            case 2:
                //接收上一个活动传来的作者名字，设置
                author.setText(getIntent().getStringExtra("author"));
                //获取系统时间
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
                diaryId = simpleDateFormat.format(new Date(System.currentTimeMillis()));
                time.setText(diaryId);
                break;
            default:
                Toast.makeText(this, "没有接收到数据", Toast.LENGTH_SHORT).show();

        }

       Button add_picture =findViewById(R.id.add_picture);
        //线性布局的图片
        add_picture.setOnClickListener(v -> {
            //需要保存
            isSelected=false;
            //查询有没有权限
            if (ContextCompat.checkSelfPermission(AddDiaryActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AddDiaryActivity.this, new String[]
                        {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }else{
                readPhoto();
            }

        });

        //点击“保存"按钮
        Button save = findViewById(R.id.save);
        save.setOnClickListener(v -> {
            //按钮被点击
            isSelected = true;
            //管理数据库
            dbHelper = new MyDatabaseHelper(AddDiaryActivity.this, "Diary.db", null, 1);
            //获取数据库的读写对象
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            //获取日记内容
            values.put("author", author.getText().toString());
            values.put("title", title.getText().toString());
            values.put("content", content.getText().toString());
            values.put("time", time.getText().toString());

            if (isNumeric(diaryId)) {
                //有id,进行更新
                db.update("Diary", values, "id=?", new String[]{diaryId});
            } else {
                // 插⼊数据
                long id=db.insert("Diary", null, values);
                //避免重复存数据，保存id
                if(id>0)
                    diaryId=id+"";
            }

            //同时存储图片
            savePicture();
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();

        });

        //点击“退出“按钮
        Button quit = findViewById(R.id.quit);
        quit.setOnClickListener(v -> {
            //退出提醒
            quitRemind();
        });


        //监控标题内容是否有变化
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                //文本有改变，需重新保存
                isSelected=false;

            }
        });

        //监控文本内容是否有变化
        content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                //文本有改变，需重新保存
                isSelected=false;
            }
        });
    }


    /**
     * 返回键监听
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //监听到返回键
        if (keyCode== KeyEvent.KEYCODE_BACK) {
            //退出提醒
            quitRemind();

        }
           return false;
    }


    /**
     * 写日记时的退出提示，如果保存，直接退出，如果没保存，用户做出选择（保存/退出）
     */
    public void quitRemind(){
        //点击了保存
        if(isSelected)
            //结束活动
            finish();
        //没有点击，输出提示
        else{
            //创建一个提示对话框的构造者对象
            AlertDialog.Builder builder = new AlertDialog.Builder(AddDiaryActivity.this);
            //设置弹出对话框的标题
            builder.setTitle("提示");
            //设置弹出对话框的内容
            builder.setMessage("没有保存，确定退出吗");

            //“确定”按钮
            builder.setPositiveButton("确定", (dialog, which) -> {
                dialog.cancel();
                finish();

            });
            //"取消"按钮
            builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
            builder.show();
        }
    }




    //权限访问结果
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readPhoto();
            } else {
                Toast.makeText(this, "⽤户拒绝读取相册", Toast.LENGTH_SHORT).show();
            }
        }
            }


    /**
     * 读取照片
     */
    public void readPhoto(){
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1);
    }


    //在相册里面选择好相片之后调回到现在的这个activity中（结果返回）
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //确定返回到那个Activity的标志
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                //获取系统返回的照片的Uri
                Uri selectedImage = data.getData();

                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                //查询相册
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);

                cursor.moveToFirst();
                //获取照片路径
                String path = cursor.getString(cursor.getColumnIndexOrThrow(filePathColumn[0]));

                //获取文件名
                String fileName = path.substring(path.lastIndexOf("/") + 1);

                //关闭cursor
                cursor.close();
                //转为Bitmap
                Bitmap bitmap = BitmapFactory.decodeFile(path);

                //加入列表
                if (imageBitmapList.size() <5) {
                    imageBitmapList.add(bitmap);
                    //放入文件名列表
                    fileNameList.add(fileName);
                } else {
                    Toast.makeText(AddDiaryActivity.this, "最多选择5张照片", Toast.LENGTH_SHORT).show();
                }

                //加载图片
                loadPicture();
            }
        }
    }


    /**
     * 加载从相册中获取的图片
     *
     */
    private void loadPicture(){
        //加载图片的线性布局
        LinearLayout l=findViewById(R.id.imglist);

        l.removeAllViews();

        for(int i = 0; i<imageBitmapList.size(); i++){
            //用布局服务加载布局
            View view=getLayoutInflater().inflate(R.layout.image, null);
            ImageView imageView  =  view.findViewById(R.id.imageView);
            //布局添加图片
            imageView.setImageBitmap(imageBitmapList.get(i));
            //线性布局中添加图片布局
            l.addView(view,i);
            //图片的长按删除
            imageView.setOnLongClickListener(v -> {
                //需要保存
                isSelected=false;
                //移除当前的长按的图片Uri

                imageBitmapList.remove(l.indexOfChild(view));
                fileNameList.remove(l.indexOfChild(view));

                dbHelper = new MyDatabaseHelper(AddDiaryActivity.this, "Diary.db", null, 1);
                //获取数据库的读写对象
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                Cursor cursor=db.query("Picture",null,"id=?",new String[]{diaryId},null,null,null);
                ContentValues values = new ContentValues();
                values.put("path"+l.indexOfChild(view),"");
                if (cursor.getCount()!=0){
                    //有id,进行更新
                    db.update("Picture",values,"id=?",new String[]{diaryId});
                }
                cursor.close();
                //移除所有布局
                l.removeAllViews();
                //重新加载图片
                loadPicture();

                return false;
            });
        }
    }


    /**
     * 图片存入到files
     */
    public void savePicture(){
        dbHelper = new MyDatabaseHelper(AddDiaryActivity.this, "Diary.db", null, 1);
        //获取数据库的读写对象
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        for(int i=0;i<imageBitmapList.size();i++) {
            Bitmap bitmap = imageBitmapList.get(i);
            values.put("path"+i,fileNameList.get(i));
            FileOutputStream localFileOutputStream;
            try {
                localFileOutputStream = openFileOutput(fileNameList.get(i), Context.MODE_PRIVATE);
                Bitmap.CompressFormat localCompressFormat = Bitmap.CompressFormat.JPEG;
                bitmap.compress(localCompressFormat, 100, localFileOutputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Cursor cursor1=db.query("Picture",null,"id=?",new String[]{diaryId},null,null,null);

        //已经存在id
        if (cursor1.getCount()!=0){
            //没有图片
            if(values.size()==0)
                db.delete("Picture",  "id=?", new String[]{diaryId});
            //只是图片的改变
            else
                db.update("Picture", values, "id=?", new String[]{diaryId});
        } else {
            //布局中有图片,插⼊数据
            if(values.size() != 0) {
                //设置日记id为图片表的id
                values.put("id", diaryId);
                db.insert("Picture", null, values);
            }
        }
        cursor1.close();

    }

    /**
     * 从files文件 图片读出
     */
    public void requirePicture() {
        imageBitmapList.clear();
        fileNameList.clear();
        //数据库存在时打开数据库，不存在时创建数据库
        dbHelper = new MyDatabaseHelper(this, "Diary.db", null, 1);
        //1.获取可以写⼊的数据库对象
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //2.查询Picture表中数据
        Cursor cursor = db.query("Picture", null, "id=?", new String[]{diaryId}, null, null, null);
        int i = -1;
        while(cursor.getCount()!=0) {
            i++;
            cursor.moveToFirst();
            String pictureName = cursor.getString(cursor.getColumnIndexOrThrow("path"+i));
            if (!"".equals(pictureName) && pictureName!=null) {
                try {
                    FileInputStream localStream = openFileInput(pictureName);
                    Bitmap bitmap = BitmapFactory.decodeStream(localStream);
                    imageBitmapList.add(bitmap);
                    fileNameList.add(pictureName);
                } catch (Exception e) {
                    Toast.makeText(AddDiaryActivity.this, "图片读出出错", Toast.LENGTH_SHORT).show();
                }
            }
            else break;
        }
        cursor.close();

        loadPicture();

    }




    /**
     * 判断是不是数字
     */
    public static boolean isNumeric(String str){
        try {
            Integer valueOf = Integer.valueOf(str);
            return true;
        }catch(Exception e) {
            return false;
        }
    }

}