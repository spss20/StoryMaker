package com.ssoftwares.lublu.views;


import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.jaeger.library.StatusBarUtil;
import com.shuhart.stepview.StepView;
import com.ssoftwares.lublu.BuildConfig;
import com.ssoftwares.lublu.R;
import com.ssoftwares.lublu.utils.AppUtils;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private int currentStep = 0;
    private Dialog resultDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setColor(this , ContextCompat.getColor(this , R.color.colorDarkBlue) , 0);
        setContentView(R.layout.activity_main);
        AppUtils.showResultDialog(this , "https://oyocabs.in//process/storymaker_ddfd1c2d83.txt");
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            if (!getPackageManager().canRequestPackageInstalls()) {
//                startActivityForResult(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse(String.format("package:%s", getPackageName()))), 1234);
//            }
//        }
//        findViewById(R.id.button_1).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                showDialog();
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    if (getPackageManager().canRequestPackageInstalls()) {
//                        downloadFile(MainActivity.this  , "https://oyocabs.in/apks/WahWahh.apk");
//                    } else
//                        Toast.makeText(MainActivity.this, "We dont have perimission to install app", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

    }

    private boolean checkForPermission() {
        String[] permission = {Manifest.permission.REQUEST_INSTALL_PACKAGES};
        if (ContextCompat.checkSelfPermission(this , permission[0]) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this , permission , 99);
            return false;
        }
        return true;
    }

    boolean isProcessCompleted;
    String rawOutput;
    private void showDialog() {
        if (resultDialog != null) {
            resultDialog.dismiss();
            resultDialog = null;
        }
        isProcessCompleted = false;
        StringBuilder stringBuilder = new StringBuilder();
        resultDialog = new Dialog(this);
        resultDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        resultDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        resultDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT ,
                WindowManager.LayoutParams.MATCH_PARENT);
        resultDialog.setContentView(R.layout.dialog_stepper);
        resultDialog.setCancelable(false);

        resultDialog.show();
        final StepView stepView = resultDialog.findViewById(R.id.step_view);
        final TextView progressText = resultDialog.findViewById(R.id.progress_text);
        final ProgressBar progressBar = resultDialog.findViewById(R.id.progress_bar);
        final ImageView errorBar = resultDialog.findViewById(R.id.error_bar);
        final Button downloadApk = resultDialog.findViewById(R.id.download_apk);
        final Button cancelButton = resultDialog.findViewById(R.id.cancel_button);

        List<String> steps = new ArrayList<>();
        steps.add("Configuring");
        steps.add("Building");
        steps.add("Apk Built Success");
        stepView.setSteps(steps);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultDialog.dismiss();
            }
        });

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    stringBuilder.setLength(0);
                    URL url = new URL("http://ssoft.xyz/ipa/process/test.txt");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    InputStream stream = url.openStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(stream);
                    BufferedReader br = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = br.readLine()) != null){
                        stringBuilder.append(line + " ");
                        final String finalLine = line;
                        if (line.contains("78599")) {
                            isProcessCompleted = true;
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    switch (finalLine){
                                        case "STARTING BUILD":
                                            if (stepView.getCurrentStep() < 1)
                                            stepView.go(1 , true);
                                            break;
                                        case "BUILD SUCCESSFUL":
                                            if (stepView.getCurrentStep() < 2)
                                                stepView.go(2 , true);
                                            stepView.done(true);
                                            break;
                                        case "BUILD FAILED":
                                            progressBar.setVisibility(View.GONE);
                                            errorBar.setVisibility(View.VISIBLE);
                                            progressText.setText("Error : Build Failed");
                                        default:
                                            progressText.setText(finalLine);
                                    }
                                }
                            });
                        }
                    }
                    br.close();
                    inputStreamReader.close();
                    stream.close();
                    connection.disconnect();

                    if (!isProcessCompleted){
                        Thread.sleep(4000);
                        new Thread(this).start();
                    } else {
                        runOnUiThread(() -> {
                            rawOutput = stringBuilder.toString();
                            Log.v("rawOutput" , rawOutput);
                            if (rawOutput.contains("APK=")){
                                int startIndex = rawOutput.indexOf("APK=") + 4;
                                int endIndex = rawOutput.indexOf(" " , startIndex);
                                if (startIndex != -1 && endIndex != -1){
                                    String downloadUrl = rawOutput.substring(startIndex , endIndex);
                                    progressBar.setVisibility(View.GONE);
                                    Log.v("DownloadUrl" , downloadUrl);
                                    downloadApk.setVisibility(View.VISIBLE);
//                                    downloadFile(downloadApk , downloadUrl);
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    errorBar.setVisibility(View.VISIBLE);
                                    progressText.setText("Error: Apk Download Url not found");
                                }
                            }
                        });
                    }

                } catch (IOException | InterruptedException e) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                    e.printStackTrace();
                }

            }
        });
        thread.start();
    }

    private static void downloadFile(Context mContext, String downloadUrl) {
        DownloadManager dm = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(downloadUrl));
        request.setTitle("Downloading Apk");
        request.setDescription("downloading apk..");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOCUMENTS , "MyApp4.apk" );

        final BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

                if (downloadId == -1)
                    return;

                // query download status
                Cursor cursor = dm.query(new DownloadManager.Query().setFilterById(downloadId));
                if (cursor.moveToFirst()) {
                    int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    if(status == DownloadManager.STATUS_SUCCESSFUL){

                        // download is successful
                        String uri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        File file = new File(Uri.parse(uri).getPath());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Uri contentUri = FileProvider.getUriForFile(ctxt, BuildConfig.APPLICATION_ID + ".provider", file);
                            Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
                            openFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            openFileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            openFileIntent.setData(contentUri);
                            mContext.startActivity(openFileIntent);
                            mContext.unregisterReceiver(this);
                        } else {
                            Intent install = new Intent(Intent.ACTION_VIEW);
                            install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            install.setDataAndType(Uri.parse(uri),
                                    "application/vnd.android.package-archive");
                            mContext.startActivity(install);
                            mContext.unregisterReceiver(this);
//                    finish();
                        }
                    }
                    else {
                        Log.v("Status" , "Download Cancelled");
                    }
                }
                else {
                    Log.v("Status" , "Download Cancelled");
                }

            }
        };
        mContext.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        dm.enqueue(request);

        if (DownloadManager.STATUS_SUCCESSFUL == 8) {
            Log.v("Status" , "Download Successful");
        } else Log.v("Status" , "Download Failed");

    }
}