package com.ssoftwares.lublu.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.jaeger.library.StatusBarUtil;
import com.ssoftwares.lublu.R;
import com.ssoftwares.lublu.adapters.ViewpagerAdapter;
import com.ssoftwares.lublu.models.Template;

import io.realm.Realm;
import io.realm.RealmResults;

public class PlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setTransparent(this);
        setContentView(R.layout.activity_play);
        Realm realm = Realm.getDefaultInstance();

        RealmResults<Template> templateList = realm.where(Template.class).findAll();
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        ViewpagerAdapter adapter = new ViewpagerAdapter(this, templateList);
        adapter.setPlay(true);
        viewPager.setAdapter(adapter);
    }
}