package com.example.diary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import java.util.List;

/**
 * 日记适配器
 */
public class DiaryAdapter extends ArrayAdapter<Diary> {
    private final int resourceId;

    public DiaryAdapter(Context context, int textViewResourceId, List<Diary> objects) {
        super(context, textViewResourceId, objects);
        this.resourceId=textViewResourceId;
    }
    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Diary diary = getItem(position);
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
        }else {
            view = convertView;
        }
        TextView diaryTime= view.findViewById(R.id.text_view_time);
        diaryTime.setText(diary.getTime());
        //设置checkbox不可见
        view.findViewById(R.id.chb_select).setVisibility(View.INVISIBLE);
        //设置checkbox不可点击
        view.findViewById(R.id.chb_select).setClickable(false);
        String str= (position + 1) +"\t\t\t"+diary.getTime();
        diaryTime.setText(str);
        return view;
    }
}
