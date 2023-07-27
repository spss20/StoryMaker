package com.ssoftwares.lublu.utils;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.OpenableColumns;
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

import androidx.core.content.FileProvider;

import com.shuhart.stepview.StepView;
import com.ssoftwares.lublu.BuildConfig;
import com.ssoftwares.lublu.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class AppUtils {

    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private static Dialog resultDialog;
    private static Dialog dialog;
    private static boolean isProcessCompleted = false;
    private static String rawOutput;
    private static int retry = 0;

    public static void handleNoInternetConnection(Context context) {
        Toast.makeText(context, "No Internet. Please check your internet connection", Toast.LENGTH_SHORT).show();
    }

    public static void showResultDialogRaw(Activity activity, String scripturl) {
        if (resultDialog != null) {
            resultDialog.dismiss();
            resultDialog = null;
        }
        isProcessCompleted = false;
        resultDialog = new Dialog(activity);
        resultDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        resultDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        resultDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        resultDialog.setContentView(R.layout.dialog_result);
        resultDialog.setCancelable(false);
        resultDialog.show();
        TextView outputTv = resultDialog.findViewById(R.id.output_text);
        outputTv.setMovementMethod(new ScrollingMovementMethod());

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        outputTv.setText("Fetching Data...");
                    }
                });
                try {
                    URL url = new URL(scripturl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    InputStream stream = url.openStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(stream);
                    BufferedReader br = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = br.readLine()) != null) {
                        final String finalLine = line;
                        if (line.contains("78599")) {
                            isProcessCompleted = true;
//                            break;
                        } else {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    outputTv.append("\n" + finalLine);
                                }
                            });
                        }
                    }
                    br.close();
                    inputStreamReader.close();
                    stream.close();
                    connection.disconnect();

                    if (!isProcessCompleted) {
                        Thread.sleep(4000);
                        new Thread(this).start();
                    }

                } catch (IOException | InterruptedException e) {
                    if (retry < 4){
                        try {
                            Thread.sleep(3000);
                            new Thread(this).start();
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                    } else {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    e.printStackTrace();
                }

            }
        });
        thread.start();

        resultDialog.findViewById(R.id.view_stepped_progress).setOnClickListener((v)->{
            resultDialog.dismiss();
            showResultDialog(activity , scripturl);
        });
        resultDialog.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissResultDialog();
            }
        });
    }

    public static void showResultDialog(Activity activity, String scripturl) {
        if (resultDialog != null) {
            resultDialog.dismiss();
            resultDialog = null;
        }
        retry = 0;
        isProcessCompleted = false;
        StringBuilder stringBuilder = new StringBuilder();
        resultDialog = new Dialog(activity);
        resultDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        resultDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        resultDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
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
        final ImageView copyUrl = resultDialog.findViewById(R.id.copy_url);
        final Button viewRaw = resultDialog.findViewById(R.id.view_raw);

        viewRaw.setOnClickListener((v) -> {
            resultDialog.dismiss();
            showResultDialogRaw(activity , scripturl);
        });
        List<String> steps = new ArrayList<>();
        steps.add("Configuring");
        steps.add("Building");
        steps.add("Apk Built Success");
        stepView.setSteps(steps);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultDialog.dismiss();
                activity.finish();
            }
        });
        progressText.setText("Initialising & Configuring project");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    stringBuilder.setLength(0);
                    URL url = new URL(scripturl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    InputStream stream = url.openStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(stream);
                    BufferedReader br = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = br.readLine()) != null) {
                        stringBuilder.append(line + " ");
                        final String finalLine = line;
                        if (line.contains("78599")) {
                            isProcessCompleted = true;
                        } else {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    switch (finalLine) {
                                        case "STARTING BUILD":
                                            if (stepView.getCurrentStep() < 1)
                                                stepView.go(1, true);
                                            progressText.setText("Building app in progress");
                                            break;
                                        case "BUILD SUCCESSFUL":
                                            if (stepView.getCurrentStep() < 2)
                                                stepView.go(2, true);
                                            stepView.done(true);
                                            progressText.setText("Build Successful, baking apk file");
                                            break;
                                        case "BUILD FAILED":
                                            progressBar.setVisibility(View.GONE);
                                            errorBar.setVisibility(View.VISIBLE);
                                            progressText.setText("Error: Build Failed");
                                        default:
                                    }
                                }
                            });
                        }
                    }
                    br.close();
                    inputStreamReader.close();
                    stream.close();
                    connection.disconnect();

                    if (!isProcessCompleted) {
                        Thread.sleep(4000);
                        new Thread(this).start();
                    } else {
                        activity.runOnUiThread(() -> {
                            rawOutput = stringBuilder.toString();
                            Log.v("rawOutput", rawOutput);
                            if (rawOutput.contains("APK=")) {
                                int startIndex = rawOutput.indexOf("APK=") + 4;
                                int endIndex = rawOutput.indexOf(" ", startIndex);
                                if (startIndex != -1 && endIndex != -1) {
                                    String downloadUrl = rawOutput.substring(startIndex, endIndex);
//                                    progressLayout.setVisibility(View.GONE);
                                    progressBar.setVisibility(View.GONE);
                                    progressText.setText("Copy Download Url");
                                    cancelButton.setText("Dismiss");
                                    copyUrl.setVisibility(View.VISIBLE);
                                    copyUrl.setOnClickListener((v -> {
                                        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("downloadUrl", downloadUrl);
                                        clipboard.setPrimaryClip(clip);
                                        Toast.makeText(activity, "Download url copied, paste it in browser to download apk", Toast.LENGTH_SHORT).show();
                                    }));
                                    Log.v("DownloadUrl", downloadUrl);
                                    downloadApk.setVisibility(View.VISIBLE);
                                    downloadApk.setOnClickListener((v) -> downloadFile(activity, downloadUrl));
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    errorBar.setVisibility(View.VISIBLE);
                                    progressText.setText("Error: Apk Download Url not found");
                                }
                            }
                        });
                    }

                } catch (IOException | InterruptedException e) {
                    if (retry < 3){
                        try {
                            Thread.sleep(3000);
                            new Thread(this).start();
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                        retry++;
                    } else {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                errorBar.setVisibility(View.VISIBLE);
                                progressText.setText("Error: Log Url not found");
                            }
                        });
                    }
                    e.printStackTrace();
                }

            }
        });
        thread.start();
    }

    private static void downloadFile(Context mContext, String downloadUrl) {
        if (!checkInstallPermission(mContext))
            return;

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
        Toast.makeText(mContext, "Download Started in background, check notification for progress", Toast.LENGTH_SHORT).show();
        if (DownloadManager.STATUS_SUCCESSFUL == 8) {
            Log.v("Status" , "Download Successful");
        } else Log.v("Status" , "Download Failed");

    }

    private static boolean checkInstallPermission(Context mContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!mContext.getPackageManager().canRequestPackageInstalls()) {
                if (mContext instanceof Activity) {
                    ((Activity) mContext).startActivityForResult(
                            new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).
                                    setData(Uri.parse(String.format("package:%s", mContext.getPackageName()))), 1234);
                } else {
                    Log.e("Error" ,"mContext should be an instanceof Activity.");
                }
                return false;
            } else return true;
        } else return true;
    }

    public static void dismissResultDialog() {
        if (resultDialog != null)
            resultDialog.dismiss();
        resultDialog = null;
    }

    public static void showLoadingBar(Context context) {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_progress);
        dialog.setCancelable(false);
        dialog.show();
    }

    public static void dissmissLoadingBar() {
        if (dialog != null)
            dialog.dismiss();
        dialog = null;
    }

    public static String getMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
