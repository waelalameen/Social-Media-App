package com.app_mo.animefaq;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.MimeTypeMap;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.app_mo.animefaq.behavior.ImageUtils;
import com.app_mo.animefaq.databinding.ActivityAccountBinding;
import com.app_mo.animefaq.device.InputOutput;
import com.app_mo.animefaq.model.UserSignOutInfo;
import com.app_mo.animefaq.network.MySingleton;
import com.app_mo.animefaq.network.NetworkInfo;
import com.app_mo.animefaq.storage.SavePreferences;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.gson.Gson;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class Account extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {
    private int userId = 0;
    private ActivityAccountBinding binding;
    private int REQUEST = 200;
    private static final int STORAGE_PERMISSION_CODE = 123;
    private Uri filePath;
    private okhttp3.Response response;
    private ProgressDialog progressDialog;
    private Gson gson;
    private UserSignOutInfo info;
    private GoogleApiClient mGoogleApiClient;
    private TwitterAuthClient mTwitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_account);
        setSupportActionBar(binding.toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        changeFonts();

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

        binding.content.editDescription.setOnClickListener(this);
        binding.content.connectAccounts.setOnClickListener(this);
        binding.content.signOut.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            startActivity(new Intent(this, MainActivity.class));
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
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
                requestStoragePermission();
            }
        }
    }

    private void uploadImageToServer(final int id) {
        final String uploadID = UUID.randomUUID().toString();
        final String path = getPath(filePath);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("اتنظر");
        progressDialog.setMessage("جاري تبديل الصورة");
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
                String newPath = inOut.bitmapToFile(bitmap, Account.this, newName, Account.this);
                file = new File(newPath);

                OkHttpClient client = new OkHttpClient();
                RequestBody fileBody = RequestBody.create(MediaType.parse(contentType), file);

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("type", contentType)
                        .addFormDataPart("image", filePath.substring(filePath.lastIndexOf('/') + 1), fileBody)
                        .addFormDataPart("name", String.valueOf(id))
                        .build();

                okhttp3.Request request = new okhttp3.Request.Builder()
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
            uploadImageToServer(userId);
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
                uploadImageToServer(userId);
            } else {
                Log.d("grantResults", "PERMISSION_DENIED");
            }
        }
    }

    private String getMimeType(String path) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    private void changeFonts() {
        ChangeTypeface.setTypeface(this, binding.content.questionsLabel);
        ChangeTypeface.setTypeface(this, binding.content.answersLabel);
        ChangeTypeface.setTypeface(this, binding.content.solutionsLabel);
        ChangeTypeface.setTypeface(this, binding.content.descriptionLabel);
        ChangeTypeface.setTypeface(this, binding.content.linksLabel);
        ChangeTypeface.setTypeface(this, binding.content.signOut);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.edit_description:
                startActivity(new Intent(this, EditDescription.class));
                break;
            case R.id.connect_accounts:
                break;
            case R.id.sign_out:
                signOut();
                break;
            default:
                break;
        }
    }

    private void signOut() {
        String token = new SavePreferences(this).getValues("userWebToken", "webToken");
        info = new UserSignOutInfo(String.valueOf(userId), token);
        gson = new Gson();
        final String jsonString = gson.toJson(info);
        //new SavePreferences(this).getValues("userId", "id"));

        StringRequest stringRequest = new StringRequest(Request.Method.POST, NetworkInfo.HOST_URL + "",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("sign_out_response", response);
                        try {
                            gson = new Gson();
                            info = gson.fromJson(response, UserSignOutInfo.class);

                            if (info.getResult().equalsIgnoreCase("Success")) {
                                if (info.getMethod().equalsIgnoreCase("Facebook")) {
                                    signOutFromFacebook();
                                } else if (info.getMethod().equalsIgnoreCase("Google")) {
                                    signOutFromGoogle();
                                } else if (info.getMethod().equalsIgnoreCase("Twitter")) {
                                    signOutFromTwitter();
                                } else {
                                    signOutNormal();
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    Log.i("VolleyError", error.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("info", jsonString);
                return map;
            }
        };

        MySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    private void signOutNormal() {
    }

    private void signOutFromTwitter() {
        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeSessionCookie();

        //mTwitter.setOAuthAccessToken(null);
        //mTwitter.shutdown();
    }

    private void signOutFromGoogle() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            startActivity(new Intent(Account.this, Login.class));
                        }
                    }
                });
    }

    private void signOutFromFacebook() {
        if (AccessToken.getCurrentAccessToken() != null && com.facebook.Profile.getCurrentProfile() != null) {
            LoginManager.getInstance().logOut();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}