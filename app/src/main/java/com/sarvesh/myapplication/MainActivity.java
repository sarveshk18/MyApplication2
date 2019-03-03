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

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import net.dongliu.apk.parser.*;
import net.dongliu.apk.parser.bean.ApkMeta;
import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int READ_REQUEST_CODE = 42;
    private static String TAG = "logdata for testing purpose";
    private String ExceptionTag = "Exception Occured due to";
    private String path = null;
    private String[] perms = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
    int permsRequestCode = 200;
    String bucket = "androidpermissions";
    AmazonS3 s3Client;


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
        s3credentialsProvider();  // callback method to call credentialsProvider method.
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
            new UploadPermissionsToS3(permissions).execute("");
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
                for (String permission : apkMeta.getUsesPermissions()) {
                    permissions = permissions + permission + "\n";
                    Log.d(TAG, permission);
                }
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
                new UploadPermissionsToS3(permissions).execute("");
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

    public void s3credentialsProvider(){
        // Initialize the AWS Credential
        CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "ap-south-1:c27db2c5-b43f-41ae-98fc-11e4f08b5429", // Identity Pool ID
                Regions.AP_SOUTH_1 // Region
        );
        setAmazonS3Client(cognitoCachingCredentialsProvider);
    }

    public void setAmazonS3Client(CognitoCachingCredentialsProvider credentialsProvider){
        // Create an S3 client
        s3Client = new AmazonS3Client(credentialsProvider);
        // Set the region of your S3 bucket
        s3Client.setRegion(Region.getRegion(Regions.AP_SOUTH_1));
    }

   private class UploadPermissionsToS3 extends AsyncTask<String, Void, String> {

        private String permissions;

        public UploadPermissionsToS3(String permissions) {
            this.permissions = permissions;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                s3Client.putObject(bucket,"ApkPermissions.txt",permissions );
            }
            catch (Exception E)
            {
                Log.i(ExceptionTag, E.toString());
            }
            return null;
        }
    }

}
