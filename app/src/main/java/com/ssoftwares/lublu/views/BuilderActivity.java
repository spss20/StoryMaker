package com.ssoftwares.lublu.views;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager2.widget.ViewPager2;

import com.jaeger.library.StatusBarUtil;
import com.ssoftwares.lublu.R;
import com.ssoftwares.lublu.adapters.ViewpagerAdapter;
import com.ssoftwares.lublu.models.Template;

import io.realm.Realm;
import io.realm.RealmResults;
import me.relex.circleindicator.CircleIndicator3;

public class BuilderActivity extends AppCompatActivity implements View.OnClickListener{

    ViewPager2 viewPager;
    ImageView addTemplate;
    RealmResults<Template> templateList;
    ViewpagerAdapter adapter;
    Realm realm;
    ImageView resetAll;
    ImageView removeTemplate;
    ImageView play;
    ImageView startBuilding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setTransparent(this);
        setContentView(R.layout.activity_builder);

        realm = Realm.getDefaultInstance();

        templateList = realm.where(Template.class).findAll();
        if (templateList.size() == 0) {
            realm.beginTransaction();
            realm.copyToRealm(new Template("1", R.drawable.template_1, R.layout.template_1));
            realm.copyToRealm(new Template("3", R.drawable.template_3, R.layout.template_3));
            realm.commitTransaction();
        }

        viewPager = findViewById(R.id.view_pager);
        resetAll = findViewById(R.id.reset_all);
        startBuilding = findViewById(R.id.start_building);
        CircleIndicator3 indicator = findViewById(R.id.indicator);
        addTemplate = findViewById(R.id.add_template);
        removeTemplate = findViewById(R.id.remove_template);
        play = findViewById(R.id.play);

        adapter = new ViewpagerAdapter(this, templateList);
        viewPager.setAdapter(adapter);

        indicator.setViewPager(viewPager);
        adapter.registerAdapterDataObserver(indicator.getAdapterDataObserver());


        addTemplate.setOnClickListener(this);
        removeTemplate.setOnClickListener(this);
        resetAll.setOnClickListener(this);
        play.setOnClickListener(this);
        startBuilding.setOnClickListener(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(templateReceiver, new IntentFilter("template-selected"));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.add_template:
                openStoryTemplates();
                break;
            case R.id.remove_template:
                removeTemplate();
                break;
            case R.id.reset_all:
                resetAll();
                break;
            case R.id.play:
                Intent intent = new Intent(BuilderActivity.this , PlayActivity.class);
                startActivity(intent);
                break;
            case R.id.start_building:
                if (templateList.size() == 0){
                    Toast.makeText(this, "Please add at least 1 template", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent1 = new Intent(this , PublishActivity.class);
                startActivity(intent1);
                break;
        }
    }
    private void resetAll() {
        new AlertDialog.Builder(this)
                .setTitle("Delete All Templates")
                .setMessage("Are you sure you want to continue? All changes you have made will be lost.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        realm.beginTransaction();
                        realm.where(Template.class).findAll().deleteAllFromRealm();
                        realm.commitTransaction();
                        adapter.notifyChangeInPosition(1);
                        adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    BroadcastReceiver templateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Template template = (Template) intent.getSerializableExtra("data");
            realm.beginTransaction();
            realm.copyToRealm(template);
            realm.commitTransaction();
            adapter.notifyDataSetChanged();
            StoryTemplateFragment fragment = (StoryTemplateFragment) getSupportFragmentManager().findFragmentByTag("TemplateSheet");
            if (fragment != null)
                fragment.dismiss();
            viewPager.setCurrentItem(templateList.size() - 1, true);
        }
    };

    private void openStoryTemplates() {
        StoryTemplateFragment fragment = new StoryTemplateFragment();
        fragment.show(getSupportFragmentManager(), "TemplateSheet");
    }

    public void removeTemplate() {
        if (templateList.size() != 0) {
            int position = viewPager.getCurrentItem();
            realm.beginTransaction();
//            realm.where(Template.class).findAll().dele
            templateList.deleteFromRealm(position);
            realm.commitTransaction();
            adapter.notifyChangeInPosition(1);
            adapter.notifyDataSetChanged();
            if (position != 0)
                viewPager.setCurrentItem(position - 1, false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(templateReceiver);
    }

}