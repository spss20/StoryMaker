package com.ssoftwares.lublu.views;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.JsonObject;
import com.ssoftwares.lublu.R;
import com.ssoftwares.lublu.models.Template;
import com.ssoftwares.lublu.utils.ApiClient;
import com.ssoftwares.lublu.utils.ApiService;
import com.ssoftwares.lublu.utils.AppUtils;
import com.ssoftwares.lublu.utils.IdInputFilter;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublishActivity extends AppCompatActivity {

    private static final int APP_ICON_CODE = 10;
    private static final int PERMISSION_REQUEST_CODE = 20;
    private ImageView appIcon;
    private EditText appName;
    private Button submit;
    private ImageView icDownArrow;
    private Uri imageUri = null;
    private Realm realm;
    private ApiService apiService;
    private int rotationAngle = 0;
    private ExpandableLayout expandableLayout;
    private EditText packageName;
    private EditText versionCode;
    private EditText versionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        realm = Realm.getDefaultInstance();
        apiService = ApiClient.create();

        appIcon = findViewById(R.id.app_icon);
        appName = findViewById(R.id.app_name);
        submit = findViewById(R.id.build_app);
        icDownArrow = findViewById(R.id.ic_down_arrow);
        expandableLayout = findViewById(R.id.expandable_layout);
        packageName = findViewById(R.id.package_name);
        versionCode = findViewById(R.id.versionCode);
        versionName = findViewById(R.id.versionName);

        appIcon.setOnClickListener(v -> {
            if (checkForPermission()) {
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("image/png");
//                startActivityForResult(intent, APP_ICON_CODE);
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, APP_ICON_CODE);
            }
        });
        submit.setOnClickListener(v -> {
            try {
                sendMakeAppRequest();
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        });

        findViewById(R.id.open_advanced).setOnClickListener(v -> {
            ObjectAnimator anim = ObjectAnimator.ofFloat(icDownArrow, "rotation", rotationAngle, rotationAngle + 180);
            anim.setDuration(500);
            anim.start();
            rotationAngle += 180;
            rotationAngle = rotationAngle % 360;
            if (expandableLayout.isExpanded()) {
                expandableLayout.collapse();
            } else {
                expandableLayout.expand();
            }
        });
        appName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String name = appName.getText().toString();
                    StringBuilder filteredName = new StringBuilder();

                    for (int i = 0; i < name.length(); i++) {
                        char c = name.charAt(i);
                        if (Character.toString(c).matches("^[a-zA-Z]?$")) {
                            filteredName.append(c);
                        }
                    }

                    packageName.setText("com.valentine." + filteredName.toString().toLowerCase());
                }
            }
        });
        packageName.setFilters(new InputFilter[]{new IdInputFilter(this, "^([a-z0-9\\\\.])+$")});
        packageName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    boolean isMatch = packageName.getText().toString().matches("^([A-Za-z]{1}[A-Za-z\\d_]*\\.)+[A-Za-z][A-Za-z\\d_]*$");
                    if (!isMatch) {
                        packageName.setError("Invalid Package Name");
                        return;
                    }
                }
                packageName.setError(null);
            }
        });
    }

    private void sendMakeAppRequest() throws JSONException, IOException {
        if (imageUri == null) {
            Toast.makeText(this, "Please select app icon first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (appName.getText().toString().isEmpty()) {
            Toast.makeText(this, "App Name can't be empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (packageName.getText().toString().isEmpty()) {
            Toast.makeText(this, "Package Name can't be empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!packageName.getText().toString().matches("^([A-Za-z]{1}[A-Za-z\\d_]*\\.)+[A-Za-z][A-Za-z\\d_]*$")) {
            Toast.makeText(this, "Invalid Package Name.", Toast.LENGTH_SHORT).show();
            return;
        }
        //Make Api Request Here.
        RealmResults<Template> templatesList = realm.where(Template.class).findAll();
        if (templatesList.size() == 0) {
            Toast.makeText(this, "Unknown Error Occured. Exiting..", Toast.LENGTH_SHORT).show();
            return;
        }
        AppUtils.showLoadingBar(this);
        JSONArray config = new JSONArray();
        for (Template template : templatesList) {
            JSONObject object = new JSONObject();
            object.put("id", template.getId());
            object.put("text", template.getText());
            object.put("image", template.getImageBase64());
            object.put("textSize", template.getTextSize());
            config.put(object);
        }

        JSONObject data = new JSONObject();
        data.put("appName", appName.getText().toString());
        data.put("packageName", packageName.getText().toString());
        if (!versionCode.getText().toString().isEmpty())
            data.put("versionCode", versionCode.getText().toString());
        if (!versionName.getText().toString().isEmpty())
            data.put("versionName", versionName.getText().toString());

        Log.v("Data", data.toString());
        RequestBody dataBody = ApiClient.createPartFromString(data.toString());
        MultipartBody.Part appIconPart = ApiClient.prepareFilePart(this, "files.icon", imageUri);

        RequestBody configBody =
                RequestBody.create(
                        config.toString().getBytes(),
                        MediaType.parse("application/json")
                );
        MultipartBody.Part configPart = MultipartBody.Part.createFormData("files.config",
                appName.getText().toString() + "_config", configBody);

        apiService.buildApp(dataBody, appIconPart, configPart).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                AppUtils.dissmissLoadingBar();
                if (response.code() != 200) {
                    Toast.makeText(PublishActivity.this, "Err: " + response.code() + " received", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (response.body() == null) {
                    Toast.makeText(PublishActivity.this, "An Unknown error has occured", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (response.body().has("outputUrl")) {
                    String outputUrl = response.body().get("outputUrl").getAsString();
                    AppUtils.showResultDialog(PublishActivity.this, outputUrl);
                } else {
                    Toast.makeText(PublishActivity.this, "Error: Output Url Path not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                AppUtils.dissmissLoadingBar();
                AppUtils.handleNoInternetConnection(PublishActivity.this);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, APP_ICON_CODE);
            }
        }
    }

    private boolean checkForPermission() {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_ICON_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
                appIcon.setImageURI(imageUri);
            }
        }
    }
}