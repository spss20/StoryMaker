package com.ssoftwares.lublu.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ssoftwares.lublu.R;
import com.ssoftwares.lublu.models.Template;

import java.util.List;

public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.TemplateViewHolder> {

    private final Context mContext;
    private final List<Template> templatesList;

    public TemplateAdapter(Context context , List<Template> templatesList){
        mContext = context;
        this.templatesList = templatesList;
    }

    @NonNull
    @Override
    public TemplateAdapter.TemplateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.template_item , parent , false);
        return new TemplateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TemplateAdapter.TemplateViewHolder holder, int position) {
        Template template = templatesList.get(position);
        holder.templateImage.setImageDrawable(ContextCompat.getDrawable(mContext , template.getImageId()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("template-selected");
                intent.putExtra("data" , template);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return templatesList.size();
    }

    public class TemplateViewHolder extends RecyclerView.ViewHolder {
        ImageView templateImage;
        public TemplateViewHolder(@NonNull View itemView) {
            super(itemView);
            templateImage = itemView.findViewById(R.id.template_image);
        }
    }
}
