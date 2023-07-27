package com.ssoftwares.lublu.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.ssoftwares.lublu.models.Template;
import com.ssoftwares.lublu.views.StoryFragment;

import io.realm.RealmResults;

public class ViewpagerAdapter extends FragmentStateAdapter {

    RealmResults<Template> templateList;
    private long baseId = 0;
    private boolean isPlay = false;

    public ViewpagerAdapter(@NonNull FragmentActivity fragmentActivity , RealmResults<Template> templateList) {
        super(fragmentActivity);
        this.templateList = templateList;
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return StoryFragment.newInstance(templateList.get(position) , isPlay);
    }

    @Override
    public int getItemCount() {
        return templateList.size();
    }

    @Override
    public long getItemId(int position) {
        return baseId + position;
    }
    public void notifyChangeInPosition(int n) {
        // shift the ID returned by getItemId outside the range of all previous fragments
        baseId += getItemCount() + n;
    }

    public boolean isPlay() {
        return isPlay;
    }

    public void setPlay(boolean play) {
        isPlay = play;
    }
}
