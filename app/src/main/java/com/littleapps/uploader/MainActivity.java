package com.littleapps.uploader;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ImageView uploadImg;
    private Button uploadBtn,chooseImgBtn;
    private EditText name;
    private StorageReference mStorageRef;
    private Uri selectedImage;
    private ProgressDialog dialog;
    private Spinner spinner;
    private ArrayList<String> categoryList=new ArrayList<>();

    private DatabaseReference imageReference,noOfWallRef;
    private HashMap<String, Object> uploadMap;
    private int no;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dialog = new ProgressDialog(this);
        uploadImg = findViewById(R.id.uploadImg);
        uploadBtn = findViewById(R.id.uploadBtn);
        chooseImgBtn = findViewById(R.id.chooseBtn);
        name=findViewById(R.id.name);
        spinner = findViewById(R.id.categorySpinner);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        imageReference = FirebaseDatabase.getInstance().getReference();
        noOfWallRef=imageReference.child("Wall2");

        getCategoryList();

        noOfWallRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    no = dataSnapshot.child("wall_id").getValue(Integer.class);
                }catch (Exception e){
                    no=0;
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        SpinnerAdapter adapter = new StatusSpinnerAdapter(this, android.R.layout.simple_spinner_item, R.id.status, categoryList);
        spinner.setAdapter(adapter);

        dialog.setMessage("Uploading....");

        chooseImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 2);
            }
        });


        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                StorageReference riversRef = mStorageRef.child("Wall2/"+categoryList.get(spinner.getSelectedItemPosition())+"/" + String.valueOf(++no) + ".jpg");
                if (checkCredentials()) {

                    dialog.show();

                    riversRef.putFile(selectedImage)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // Get a URL to the uploaded content
                                    dialog.dismiss();
                                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                    uploadData(downloadUrl);

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                    // ...
                                    Toast.makeText(MainActivity.this, "fail", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });


    }

    private boolean checkCredentials() {

        boolean b=false;

        if(selectedImage == null){
            Toast.makeText(MainActivity.this, "Select a Picture First", Toast.LENGTH_SHORT).show();
        }else if(spinner.getSelectedItemPosition()==0){
            Toast.makeText(MainActivity.this, "Select a Category First", Toast.LENGTH_SHORT).show();
        }else{
            b=true;
        }

        return b;
    }

    private void uploadData(Uri downloadUrl) {

        DatabaseReference uploadRef=imageReference.child("Wall2").child(categoryList.get(spinner.getSelectedItemPosition())).push();
        DatabaseReference uploadRef2=imageReference.child("Wall2").child("Recent").push();
        DatabaseReference noRefUpload=noOfWallRef.child("wall_id");

        HashMap wallIdMap=new HashMap();
        wallIdMap.put("wall_id",no);

        uploadMap = new HashMap<>();
        uploadMap.put("name", name.getText().toString());
        uploadMap.put("url",downloadUrl.toString());
        uploadMap.put("favourite",0);

        uploadRef.setValue(uploadMap);
        uploadRef2.setValue(uploadMap);
        noRefUpload.setValue(wallIdMap);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2 && resultCode == RESULT_OK) {
            selectedImage = data.getData();
            Glide.with(MainActivity.this)
                    .load(selectedImage)
                    .apply(RequestOptions
                            .centerCropTransform())
                    .into(uploadImg);
        }
    }

    //for varun
    public void getCategoryList() {
        categoryList.add("Select Category");
        categoryList.add("Material");
        categoryList.add("Minimal");
        categoryList.add("Abstract");
        categoryList.add("Space");
        categoryList.add("Photography");
        categoryList.add("Dark");
    }

    //for morad
    /*public void getCategoryList() {
        categoryList.add("Select Category");
        categoryList.add("Nature");
        categoryList.add("Space");
        categoryList.add("Cartoon");
        categoryList.add("Cars");
        categoryList.add("Sports");
        categoryList.add("Games");
        categoryList.add("Night");
        categoryList.add("Anime");
    }*/
}
