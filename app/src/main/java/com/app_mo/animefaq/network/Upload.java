package com.app_mo.animefaq.network;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;

import com.app_mo.animefaq.ChangeTypeface;
import com.app_mo.animefaq.Login;
import com.app_mo.animefaq.R;
import com.app_mo.animefaq.behavior.ImageUtils;
import com.app_mo.animefaq.databinding.ActivityUploadBinding;
import com.app_mo.animefaq.device.InputOutput;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Upload extends AppCompatActivity {
    ActivityUploadBinding binding;
    private int REQUEST = 200;
    private static final int STORAGE_PERMISSION_CODE = 123;
    private Uri filePath;
    private Response response;
    private int id = 0;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_upload);

        ChangeTypeface.setTypeface(this, binding.browse);
        ChangeTypeface.setTypeface(this, binding.noteText);
        ChangeTypeface.setTypeface(this, binding.smallNote);

        //id = getIntent().getExtras().getInt("id");

        binding.browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

        binding.next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestStoragePermission();
            }
        });
    }

    private void uploadImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_note)), REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                filePath = data.getData();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                    Matrix matrix = new Matrix();
                    String path = getPath(filePath);
                    File file = new File(path);
                    String filePath = file.getAbsolutePath();
                    int orientation = ImageUtils.getOrientation(filePath);
                    matrix.postRotate(orientation);
                    Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0,(int) (bitmap.getWidth() * 0.8),
                            (int) (bitmap.getHeight() * 0.8), matrix, true);
                    binding.profilePicture.setImageBitmap(newBitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                    e.getMessage();
                }
            }
        }
    }

    private void uploadImageToServer(final int id) {
        final String uploadID = UUID.randomUUID().toString();
        final String path = getPath(filePath);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("اتنظر");
        progressDialog.setMessage("جاري تجسيل الحساب");
        progressDialog.setMax(100);
        progressDialog.setProgress(100);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(path);
                String contentType = getMimeType(file.getPath());
                String filePath = file.getAbsolutePath();
                InputOutput inOut = new InputOutput();
                Bitmap bitmap = inOut.fileToBitmap(path);
                bitmap = ImageUtils.rotateImage(bitmap, filePath);
                String newName = uploadID + file.getName().substring(file.getName().lastIndexOf('.'));
                String newPath = inOut.bitmapToFile(bitmap, Upload.this, newName, Upload.this);
                file = new File(newPath);

                OkHttpClient client = new OkHttpClient();
                RequestBody fileBody = RequestBody.create(MediaType.parse(contentType), file);

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("type", contentType)
                        .addFormDataPart("image", filePath.substring(filePath.lastIndexOf('/') + 1), fileBody)
                        .addFormDataPart("name", String.valueOf(id))
                        .build();

                Request request = new Request.Builder()
                        .url(NetworkInfo.HOST_URL + "profile/picture/upload")
                        .post(requestBody)
                        .build();

                try {
                    response = client.newCall(request).execute();
                    progressDialog.dismiss();

                    if(!response.isSuccessful()){
                        throw new IOException("Error : " + response);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    e.getMessage();
                } finally {
                    response.body().close();
                }
            }
        });

        thread.start();

        startActivity(new Intent(this, Login.class));
    }

    private String getPath(Uri filePath) {
        Cursor cursor = getContentResolver().query(filePath, null, null, null, null);
        cursor.moveToFirst();
        String documentID = cursor.getString(0);
        cursor.close();

        String[] selectionArgs = new String[] {documentID};

        cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ?",
                selectionArgs, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            uploadImageToServer(id);
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //Here you can explain why you need this permission
        }

        ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("grantResults", "PERMISSION_GRANTED");
                uploadImageToServer(id);
            } else {
                Log.d("grantResults", "PERMISSION_DENIED");
            }
        }
    }

    private String getMimeType(String path) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
