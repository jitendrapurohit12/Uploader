package com.littleapps.uploader;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ImageView uploadImg;
    private Button uploadBtn;
    private EditText name;
    private StorageReference mStorageRef;
    private Uri selectedImageFromStorage, thumbnailFromUri, downloadUri;
    private ProgressDialog dialog;
    private Spinner spinner;
    private ArrayList<String> categoryList = new ArrayList<>();
    private ArrayList<String> validUserList = new ArrayList<>();
    private DatabaseReference imageReference, noOfWallRef;
    private int no;
    private FirebaseAuth mAuth;
    private boolean thumbnailUploadComplete, mainUploadComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dialog = new ProgressDialog(this);
        uploadImg = findViewById(R.id.uploadImg);
        uploadBtn = findViewById(R.id.uploadBtn);
        Button chooseImgBtn = findViewById(R.id.chooseBtn);
        name = findViewById(R.id.name);
        spinner = findViewById(R.id.categorySpinner);
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        imageReference = FirebaseDatabase.getInstance().getReference();
        noOfWallRef = imageReference.child("Wall").child("Category").child("wall_id");

        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("Please wait....");
        dialog.show();

        checkValidUploader();

        getCategoryList();

        noOfWallRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    no = dataSnapshot.child("wall_id").getValue(Integer.class);
                    Log.e("has child", String.valueOf(dataSnapshot.hasChild("wall_id")));
                } catch (Exception e) {
                    no = 0;
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        chooseImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, 2);
                }
            }
        });


        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                no++;

                thumbnailUploadComplete = false;
                mainUploadComplete = false;

                uploadMainImage();
                uploadThumbImage();
            }
        });


    }

    private void uploadThumbImage() {

        InputStream inStream = null;
        try {
            inStream = getContentResolver().openInputStream(selectedImageFromStorage);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(inStream),
                512, 512);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ThumbImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        ThumbImage.recycle();

        StorageReference riversRef = mStorageRef.child("Wall/" + categoryList
                .get(spinner.getSelectedItemPosition()) + "/" + String.valueOf(no) + "/" + String.valueOf(no) + "_thumbnail.jpg");
        if (checkCredentials()) {
            dialog.show();
            riversRef.putBytes(byteArray)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            thumbnailUploadComplete = true;
                            // Get a URL to the uploaded content
                            thumbnailFromUri = taskSnapshot.getDownloadUrl();
                            if (mainUploadComplete) {
                                uploadData();
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                            exception.printStackTrace();
                            Toast.makeText(MainActivity.this, "fail", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void uploadMainImage() {
        Log.e("main path", String.valueOf(selectedImageFromStorage));
        StorageReference riversRef = mStorageRef.child("Wall/" + categoryList
                .get(spinner.getSelectedItemPosition()) + "/" + String.valueOf(no) + "/" + String.valueOf(no) + ".jpg");
        if (checkCredentials()) {
            dialog.show();
            riversRef.putFile(selectedImageFromStorage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mainUploadComplete = true;
                            // Get a URL to the uploaded content
                            downloadUri = taskSnapshot.getDownloadUrl();
                            if (thumbnailUploadComplete) {
                                uploadData();
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                            exception.printStackTrace();
                            Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void checkValidUploader() {
        FirebaseDatabase.getInstance().getReference().child("Wall").child("Uploaders")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            validUserList.add(String.valueOf(snapshot.getValue()));
                        }
                        if (!validUserList.contains(mAuth.getCurrentUser().getEmail()))
                            uploadBtn.setEnabled(false);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

    }

    private boolean checkCredentials() {

        boolean b = false;

        if (selectedImageFromStorage == null) {
            Toast.makeText(MainActivity.this, "Select a Picture First", Toast.LENGTH_SHORT).show();
        } else if (spinner.getSelectedItemPosition() == 0) {
            Toast.makeText(MainActivity.this, "Select a Category First", Toast.LENGTH_SHORT).show();
        } else {
            b = true;
        }

        return b;
    }

    private void uploadData() {

        long currentTime = System.currentTimeMillis();

        DatabaseReference uploadRef = imageReference.child("Wall").child("Category")
                .child(categoryList.get(spinner.getSelectedItemPosition())).child(String.valueOf(no));
        DatabaseReference uploadRef2 = imageReference.child("Wall").child("Category")
                .child("Recent").child(String.valueOf(no));

        HashMap wallIdMap = new HashMap();
        wallIdMap.put("wall_id", no);

        HashMap<String, Object> uploadMap = new HashMap<>();
        uploadMap.put("name", name.getText().toString());
        uploadMap.put("url", downloadUri.toString());
        uploadMap.put("thumbnail", thumbnailFromUri.toString());
        uploadMap.put("favourite", 0);
        uploadMap.put("time", currentTime);
        uploadMap.put("wall_id", no);
        uploadMap.put("category", categoryList.get(spinner.getSelectedItemPosition()));

        uploadRef.setValue(uploadMap);
        uploadRef2.setValue(uploadMap);
        noOfWallRef.setValue(wallIdMap);

        dialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2 && resultCode == RESULT_OK) {
            selectedImageFromStorage = data.getData();
            Glide.with(MainActivity.this)
                    .load(selectedImageFromStorage)
                    .apply(RequestOptions
                            .centerCropTransform())
                    .into(uploadImg);
        }
    }

    public void getCategoryList() {
        FirebaseDatabase.getInstance().getReference().child("Wall").child("Categories")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            categoryList.add(String.valueOf(snapshot.getValue()));
                        }
                        SpinnerAdapter adapter = new StatusSpinnerAdapter(MainActivity.this, android.R.layout.simple_spinner_item, R.id.status, categoryList);
                        spinner.setAdapter(adapter);
                        dialog.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private boolean checkPermission() {
        boolean permissionGranted = false;

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            permissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }
        return permissionGranted;
    }

}
