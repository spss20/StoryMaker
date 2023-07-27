package com.ssoftwares.lublu.views;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.ssoftwares.lublu.R;
import com.ssoftwares.lublu.models.Template;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import io.realm.Realm;

public class StoryFragment extends Fragment implements View.OnClickListener{

    private static final int PERMISSION_REQUEST_CODE = 30 ;
    private Template template;
    private ImageView selectImage;
    private ImageView image;
    private static final int IMAGE_REQUEST_CODE = 20;
    private ImageView editDesc;
    private ImageView changeDesc;
    private ImageView cancelDesc;
    private EditText templateText;
    private Realm realm;
    private boolean isPlay = false;
    private ImageView increaseTextSize;
    private ImageView decreaseTextSize;
    private AsyncTask asyncTask;
    private boolean leftButtonDown = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view;
        if (template == null)
        view = inflater.inflate(R.layout.template_1 , container , false);
        else view = inflater.inflate(template.getLayoutId() , container , false);

        //Change Image
        selectImage = view.findViewById(R.id.select_image);
        image = view.findViewById(R.id.image_1);
        if (selectImage != null){
            selectImage.setOnClickListener(this);
        }

        //Change Text
        editDesc = view.findViewById(R.id.edit_desc);
        changeDesc = view.findViewById(R.id.change_desc);
        cancelDesc = view.findViewById(R.id.cancel_desc);
        templateText = view.findViewById(R.id.template_text);
        increaseTextSize = view.findViewById(R.id.increase_text_size);
        decreaseTextSize = view.findViewById(R.id.decrease_text_size);

        if (isPlay){
            if (selectImage != null)
            selectImage.setVisibility(View.GONE);
            if (editDesc != null)
            editDesc.setVisibility(View.GONE);
        }

        if (editDesc != null){
            editDesc.setOnClickListener(this);
            changeDesc.setOnClickListener(this);
            cancelDesc.setOnClickListener(this);
        }
        if (templateText != null) {
            if (template.getText() != null && !template.getText().isEmpty()) {
                templateText.setText(template.getText());
            }
            if (template.getTextSize() != 0){
                templateText.setTextSize(TypedValue.COMPLEX_UNIT_PX ,template.getTextSize());
            }
        }
        if (image != null){
            if (template.getImageBase64() != null){
                byte[] imageBytes = Base64.decode(template.getImageBase64() , Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes , 0 , imageBytes.length);
                image.setImageBitmap(bitmap);
            }
        }
        if (increaseTextSize != null && decreaseTextSize != null){
            increaseTextSize.setOnClickListener(this);
            decreaseTextSize.setOnClickListener(this);
        }
        return view;
    }


    String oldText;
    @Override
    public void onClick(View v) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        switch (v.getId()){
            case R.id.increase_text_size:
                if (templateText != null){
                    templateText.setTextSize(TypedValue.COMPLEX_UNIT_PX , templateText.getTextSize() + 3);
                    realm.beginTransaction();
                    template.setTextSize(templateText.getTextSize());
                    realm.commitTransaction();
                }
                break;
            case R.id.decrease_text_size:
                if (templateText != null){
                    templateText.setTextSize(TypedValue.COMPLEX_UNIT_PX , templateText.getTextSize() - 3);
                    realm.beginTransaction();
                    template.setTextSize(templateText.getTextSize());
                    realm.commitTransaction();
                }
                break;
            case R.id.select_image:
                if (checkForPermission()) {
//                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                    intent.setType("image/*");
//                    startActivityForResult(intent, IMAGE_REQUEST_CODE);
                    Intent intent = new Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent , IMAGE_REQUEST_CODE);
                }
                break;
            case R.id.edit_desc:
                oldText = templateText.getText().toString();
                templateText.setFocusableInTouchMode(true);
                editDesc.setVisibility(View.GONE);
                changeDesc.setVisibility(View.VISIBLE);
                cancelDesc.setVisibility(View.VISIBLE);
                templateText.setBackgroundResource(android.R.drawable.editbox_background_normal);
                templateText.setBackgroundColor(ContextCompat.getColor(getContext() , android.R.color.transparent));
                templateText.requestFocus();
                imm.showSoftInput(templateText, InputMethodManager.SHOW_IMPLICIT);
                break;

            case R.id.cancel_desc:
                templateText.setText(oldText);
                templateText.clearFocus();
                templateText.setFocusableInTouchMode(false);
                templateText.setBackgroundResource(android.R.color.transparent);
                changeDesc.setVisibility(View.GONE);
                cancelDesc.setVisibility(View.GONE);
                editDesc.setVisibility(View.VISIBLE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                break;

            case R.id.change_desc:
                templateText.clearFocus();
                templateText.setFocusableInTouchMode(false);
                templateText.setBackgroundResource(android.R.color.transparent);
                changeDesc.setVisibility(View.GONE);
                cancelDesc.setVisibility(View.GONE);
                editDesc.setVisibility(View.VISIBLE);
                realm.beginTransaction();
                template.setText(templateText.getText().toString());
                realm.commitTransaction();
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                break;

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("image/*");
//                startActivityForResult(intent, IMAGE_REQUEST_CODE);
                Intent intent = new Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent , IMAGE_REQUEST_CODE);
            }
        }
    }

    private boolean checkForPermission() {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE , Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(getContext() , permissions[0]) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), permissions , PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            if (data != null && data.getData() != null && image != null) {
                image.setImageURI(data.getData());
                try {
                    InputStream in = getContext().getContentResolver().openInputStream(data.getData());
                    byte[] imageBytes = IOUtils.toByteArray(in);
                    String image64 = Base64.encodeToString(imageBytes , Base64.DEFAULT);
                    realm.beginTransaction();
                    template.setImageBase64(image64);
                    realm.commitTransaction();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static StoryFragment newInstance(Template template , boolean isPlay) {
        StoryFragment fragment = new StoryFragment();
        fragment.template = template;
        fragment.isPlay = isPlay;
        return fragment;
    }


}
