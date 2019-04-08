package com.sarvesh.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import net.dongliu.apk.parser.*;
import net.dongliu.apk.parser.bean.ApkMeta;
import org.json.JSONObject;
import java.io.File;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int READ_REQUEST_CODE = 42;
    private static String TAG = "logdata for testing purpose";
    private String ExceptionTag = "Exception Occured due to";
    private String path = null;
    private String[] perms = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
    int permsRequestCode = 200;
    public String[] SelectedPermissions = {"SEND_SMS","READ_PHONE_STATE","GET_ACCOUNTS","RECEIVE_SMS","GET_ACCOUNTS","USE_CREDENTIALS","MANAGE_ACCOUNTS","WRITE_SMS","READ_SYNC_SETTINGS","AUTHENTICATE_ACCOUNTS","WRITE_HISTORY_BOOKMARKS","INSTALL_PACKAGES","WRITE_SYNC_SETTINGS","READ_HISTORY_BOOKMARKS","INTERNET","RECORD_AUDIO","ACCESS_LOCATION_EXTRA_COMMANDS","WRITE_APN_SETTINGS","BIND_REMOTEVIEWS","READ_PROFILE","MODIFY_AUDIO_SETTINGS","READ_SYNC_STATS","WAKE_LOCK","RECEIVE_BOOT_COMPLETED","RESTART_PACKAGES","BLUETOOTH","READ_CALENDAR","READ_CALL_LOG","READ_EXTERNAL_STORAGE","VIBRATE","ACCESS_NETWORK_STATE","SUBSCRIBED_FEEDS_READ","CHANGE_WIFI_MULTICAST_STATE","WRITE_CALENDAR","MASTER_CLEAR","UPDATE_DEVICE_STATS","WRITE_CALL_LOG","DELETE_PACKAGES","GET_TASKS","GLOBAL_SEARCH","DELETE_CACHE_FILES","WRITE_USER_DICTIONARY","REORDER_TASKS","WRITE_PROFILE","SET_WALLPAPER","BIND_INPUT_METHOD","READ_SOCIAL_STREAM","READ_USER_DICTIONARY","PROCESS_OUTGOING_CALLS","CALL_PRIVILEGED","BIND_WALLPAPER","RECEIVE_WAP_PUSH","DUMP","BATTERY_STATS","ACCESS_COARSE_LOCATION","SET_TIME","WRITE_SOCIAL_STREAM","WRITE_SETTINGS","REBOOT","BLUETOOTH_ADMIN","BIND_DEVICE_ADMIN","WRITE_GSERVICES","KILL_BACKGROUND_PROCESSES","SET_ALARM","ACCOUNT_MANAGER","STATUS_BAR","PERSISTENT_ACTIVITY","CHANGE_NETWORK_STATE","RECEIVE_MMS","SET_TIME_ZONE","CONTROL_LOCATION_UPDATES","BROADCAST_WAP_PUSH","BIND_ACCESSIBILITY_SERVICE","ADD_VOICEMAIL","CALL_PHONE","BIND_APPWIDGET","READ_LOGS","SET_PROCESS_LIMIT","MOUNT_UNMOUNT_FILESYSTEMS","BIND_TEXT_SERVICE","INSTALL_LOCATION_PROVIDER","SYSTEM_ALERT_WINDOW","MOUNT_FORMAT_FILESYSTEMS","CHANGE_CONFIGURATION","CLEAR_APP_USER_DATA","CHANGE_WIFI_STATE","READ_FRAME_BUFFER","ACCESS_SURFACE_FLINGER","BROADCAST_SMS","EXPAND_STATUS_BAR","INTERNAL_SYSTEM_WINDOW","SET_ACTIVITY_WATCHER","WRITE_CONTACTS","BIND_VPN_SERVICE","DISABLE_KEYGUARD","ACCESS_MOCK_LOCATION","GET_PACKAGE_SIZE","MODIFY_PHONE_STATE","CHANGE_COMPONENT_ENABLED_STATE","CLEAR_APP_CACHE","SET_ORIENTATION","READ_CONTACTS","DEVICE_POWER","HARDWARE_TEST","ACCESS_WIFI_STATE","WRITE_EXTERNAL_STORAGE","ACCESS_FINE_LOCATION","SET_WALLPAPER_HINTS","SET_PREFERRED_APPLICATIONS","WRITE_SECURE_SETTINGS","CAMERA"};
//    String bucket = "androidpermissions";
//    AmazonS3 s3Client;

    int[] vector=new int[111];
    String input;
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults){
        switch(permsRequestCode){
            case 200:
                boolean read = grantResults[0]== PackageManager.PERMISSION_GRANTED;
                boolean write = grantResults[1]==PackageManager.PERMISSION_GRANTED;
                break;
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,perms, permsRequestCode);
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),android.Manifest.permission.READ_EXTERNAL_STORAGE );
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(!checkPermission())
            requestPermission();
//        s3credentialsProvider();  // callback method to call credentialsProvider method.
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        Uri uri = null;
        uri= intent.getData();
        if(uri!=null){
            path = getPath( MainActivity.this, uri );
            Log.i(TAG, "Uri: " + path);
            String permissions = showPermission();
//            new UploadPermissionsToS3(permissions).execute("");
            getIntent().removeExtra("key");
        }
    }

    //called when user clicks on button to get apk file to scan
    public void onclickbutton(View view)
    {
        performFileSearch();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String showPermission()
    {
        //if path not found
        if(path!=null) {
            File f = new File(path);
            String permissions = "";
            try (ApkFile apkFile = new ApkFile(new File(f.getPath()))) {
                ApkMeta apkMeta = apkFile.getApkMeta();
                apkMeta.getUsesFeatures();
                Log.d(TAG, SelectedPermissions.length + "");
                for (String permission : apkMeta.getUsesPermissions()) {
                    String lastone = permission.substring(permission.lastIndexOf('.') + 1);
//                    String[] bits = permission.split(".");
//                    String lastOne = bits[bits.length-1];
                    for (int i = 0; i < SelectedPermissions.length; i++) {
                        if (SelectedPermissions[i].equals(lastone)) {
                            vector[i] = 1;
                            break;
                        }
                    }
                    permissions = permissions + permission + "\n";
                    Log.d(TAG, permission);
                }
                input = "[";
                for (int i = 0; i < 110; i++) {
                    input = input + vector[i] + ",";
                }
                input = input + vector[110] + "]";
                Log.d(TAG, input);
                JSONObject postData = new JSONObject();
                //postData.put("input", input);
                ApiInterface predictApi = ApiUtils.getAPIService();
                Toast.makeText(MainActivity.this, "call: " + input, Toast.LENGTH_LONG).show();
                String json = "{\n" +
                        "\"vector\":\"" + input.trim() + "\"\n" +
                        "}";

                Log.d("retrofit2msg", "json: " + json);

                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);



                predictApi.getLocation(requestBody).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                       // if (response.body() != null) {
                            try {
                                //JSONObject loc = new JSONObject(response.body().string());
//                                lat = Double.parseDouble(loc.getString("lat"));
//                                lon = Double.parseDouble(loc.getString("lon"));
                                Log.d("retrofit2msg", "json: kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk");
                                Log.d("retrofit2msg", "" + response.body().string() + " , ");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        //}
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.d("FailFailFailFailFailFai", "FailFailFail");
                    }
                });

    //            new SendDetails().execute("https://opportune-geode-236811.appspot.com/", postData.toString());
            }
            catch (Exception E) {
                Log.e(ExceptionTag, "" + E.getLocalizedMessage() + "  " + E.toString());
            }
            return permissions;
        }
        return null;
    }

    public void performFileSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.android.package-archive");   //To select on .apk files
        startActivityForResult(intent, READ_REQUEST_CODE);          // when user select any apk file
    }

    // when user select any apk file
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
                path = getPath( MainActivity.this, uri );
                Log.i(TAG, "Uri: " + path);
                Log.i(TAG, "Uri: " +Environment.getExternalStorageDirectory()+ "  "+uri.toString());
                String permissions = showPermission();
 //               new UploadPermissionsToS3(permissions).execute("");
            }
        }
    }

    // To get path of Apk file
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {
        // check here to KITKAT or new version
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider provides Apk file
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }
            }
            // DownloadsProvider provides Apk file
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        //Service provides Apk file
        else if (isServiceDocument(uri)) {
            final String[] split = uri.getPath().split("device_storage");
                return Environment.getExternalStorageDirectory() + "/"
                        + split[1];
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        Log.i(TAG, "To test method is called or not");
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        Log.i(TAG, "Uri: " + "To test method is called or not");
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isServiceDocument(Uri uri) {
        Log.i(TAG, "Uri: " + "To test method is called or not");
        return "com.sec.android.app.myfiles.FileProvider".equals(uri.getAuthority());
    }

//    public void s3credentialsProvider(){
//        // Initialize the AWS Credential
//        CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider = new CognitoCachingCredentialsProvider(
//                getApplicationContext(),
//                "ap-south-1:c27db2c5-b43f-41ae-98fc-11e4f08b5429", // Identity Pool ID
//                Regions.AP_SOUTH_1 // Region
//        );
//        setAmazonS3Client(cognitoCachingCredentialsProvider);
//    }

//    public void setAmazonS3Client(CognitoCachingCredentialsProvider credentialsProvider){
//        // Create an S3 client
//        s3Client = new AmazonS3Client(credentialsProvider);
//        // Set the region of your S3 bucket
//        s3Client.setRegion(Region.getRegion(Regions.AP_SOUTH_1));
//    }

//   private class UploadPermissionsToS3 extends AsyncTask<String, Void, String> {
//
//        private String permissions;
//
//        public UploadPermissionsToS3(String permissions) {
//            this.permissions = permissions;
//        }
//
//        @Override
//        protected String doInBackground(String... strings) {
//            try {
//                s3Client.putObject(bucket,"ApkPermissions.txt",permissions );
//            }
//            catch (Exception E)
//            {
//                Log.i(ExceptionTag, E.toString());
//            }
//            return null;
//        }
//    }

}
