package com.ssoftwares.lublu.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ssoftwares.lublu.R;
import com.ssoftwares.lublu.adapters.TemplateAdapter;
import com.ssoftwares.lublu.models.Template;
import com.ssoftwares.lublu.utils.GridSpacingItemDecoration;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StoryTemplateFragment extends BottomSheetDialogFragment {

    private static final int SELECT_PICTURE = 90;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL , R.style.CustomBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_story_templates , container , false);

        List<Template> templatesList = new ArrayList<>();
        templatesList.add(new Template("1" , R.drawable.template_1 , R.layout.template_1));
        templatesList.add(new Template("2" , R.drawable.template_2 , R.layout.template_2));
        templatesList.add(new Template("3" , R.drawable.template_3 , R.layout.template_3));
        templatesList.add(new Template("4" , R.drawable.template_4 , R.layout.template_4));

        RecyclerView templateRecycler = view.findViewById(R.id.templates_recycler);
        templateRecycler.setLayoutManager(new GridLayoutManager(getContext() , 2));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing);
        GridSpacingItemDecoration decoration = new GridSpacingItemDecoration(2 , spacingInPixels , true );
        templateRecycler.addItemDecoration(decoration);
        TemplateAdapter adapter = new TemplateAdapter(getContext() , templatesList);
        templateRecycler.setAdapter(adapter);

        Button pickFromGallery = view.findViewById(R.id.pick_gallery);
        pickFromGallery.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent , SELECT_PICTURE);
//            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//            intent.setType("image/*");
//            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PICTURE && resultCode == Activity.RESULT_OK){
            if (data != null && data.getData() != null){
                Template template = new Template("5" , -1 , R.layout.template_5);
                try {
                    InputStream in = getContext().getContentResolver().openInputStream(data.getData());
                    byte[] imageBytes = IOUtils.toByteArray(in);
                    String image64 = Base64.encodeToString(imageBytes , Base64.DEFAULT);
                    template.setImageBase64(image64);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent("template-selected");
                intent.putExtra("data" , template);
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
            }
        }
    }
}
