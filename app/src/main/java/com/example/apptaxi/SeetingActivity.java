package com.example.apptaxi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;


import java.io.File;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SeetingActivity extends AppCompatActivity implements View.OnClickListener {
    private CircleImageView circleImageView;
    private EditText nameText,phoneText,driverCarName;
    private ImageView closeBtn;
    private Button saveBtn;
    private TextView profileChangeBtn;
    private String getString;
    private Intent intent;
    private Uri imageUri;
    private String myUri="";
    private StorageTask uploadTask;
    private StorageReference storageProfilePickRef;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private String cheked="";
    private ImageView img;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seeting);
        circleImageView=findViewById(R.id.profile_image);
        nameText=findViewById(R.id.name);
        phoneText=findViewById(R.id.phone_number);
        driverCarName=findViewById(R.id.driver_car);
        closeBtn=findViewById(R.id.close_button);
        saveBtn=findViewById(R.id.save_setting);
        profileChangeBtn=findViewById(R.id.change_picture_btn);
        getString=getIntent().getStringExtra("type");
        closeBtn.setOnClickListener(this);
        profileChangeBtn.setOnClickListener(this);
        mAuth=FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(getString);
        storageProfilePickRef = FirebaseStorage.getInstance().getReference().child("profile picture");
        saveBtn.setOnClickListener(this);
        img=findViewById(R.id.img);
        if(getString.equals("Custumers"))
        {
            driverCarName.setVisibility(View.INVISIBLE);
            img.setVisibility(View.INVISIBLE);
        }
        displayUserInformation();
    }

    @Override
    public void onClick(View v) {
        if(v==closeBtn)
        {
            if(getString=="Drivers")
            {
                intent=new Intent(getApplicationContext(),DriversMapsActivity.class);
                startActivity(intent);
            }
            else
            {
                intent=new Intent(getApplicationContext(),CustomersMapsActivity.class);
                startActivity(intent);
            }
        }
        if(v==profileChangeBtn)
        {
            cheked="clicked";
            //ajouter la fonctionnalite crop a l'application
            //pour aller au galerie
            CropImage.activity()
                    .setAspectRatio(1,1)
                    .start(SeetingActivity.this);
        }
        if(v==saveBtn)
        {
            //if(cheked.equals("clicked"))
            //{
                uploadProfilePicture();
            ///}
            //else
            //{

            //}
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode==RESULT_OK && data!=null)
        {

            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            imageUri =result.getUri();
            circleImageView.setImageURI(imageUri);
        }
        else
        {
            Toast.makeText(getApplicationContext(),"errror",Toast.LENGTH_SHORT).show();
        }
    }
    //ajouter l'image au storage
    private void uploadProfilePicture()
    {

       final  ProgressDialog diag=new ProgressDialog(this);
         diag.setTitle("Settings account information");
         diag.setMessage("please wait");
        diag.show();
        if(imageUri!=null)
        {

            //store the image in the firebase storage
            final StorageReference fileRef=storageProfilePickRef.child(mAuth.getUid() + ".jpg");
            uploadTask=fileRef.putFile(imageUri);
            //get the link of the image the store it in the firebasedatabase
            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
            {
                @Override
                public void onSuccess(Uri downloadUrl)
                {
                    myUri=downloadUrl.toString();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful())
                    {
                        HashMap<String,Object> userMap=new HashMap<>();
                        userMap.put("uid",mAuth.getUid());
                        userMap.put("name",nameText.getText().toString());
                        userMap.put("phone",phoneText.getText().toString());
                        userMap.put("image",myUri);
                        if(getString.equals("Drivers"))
                        {
                            userMap.put("car",driverCarName.getText().toString());
                        }
                        databaseReference.child(mAuth.getUid()).updateChildren(userMap);
                        diag.dismiss();

                        if(getString.equals("Drivers"))
                        {
                            intent=new Intent(getApplicationContext(),DriversMapsActivity.class);
                        }
                        else {
                            intent=new Intent(getApplicationContext(),CustomersMapsActivity.class);
                        }
                        startActivity(intent);

                    }
                }
            });


            /*
            uploadTask.continueWith(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful())
                    {
                        Toast.makeText(getApplicationContext(),"hana hna",Toast.LENGTH_SHORT).show();
                        throw task.getException();
                    }
                        return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(getApplicationContext(),"hana hna2",Toast.LENGTH_SHORT).show();
                        myUri=task.getResult().toString();
                        HashMap<String,Object> userMap=new HashMap<>();
                        userMap.put("uid",mAuth.getUid());
                        userMap.put("name",nameText.getText().toString());
                        userMap.put("phone",phoneText.getText().toString());
                        userMap.put("image",myUri);
                        if(getString.equals("Drivers"))
                        {
                            userMap.put("car",driverCarName.getText().toString());
                        }
                        databaseReference.child(mAuth.getUid()).updateChildren(userMap);
                        //diag.show();

                        if(getString.equals("Drivers"))
                        {
                            intent=new Intent(getApplicationContext(),DriversMapsActivity.class);
                        }
                        else {
                            intent=new Intent(getApplicationContext(),CustomersMapsActivity.class);
                        }
                        startActivity(intent);


                    }
                }
            });

            /*
            storageProfilePickRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                        }
                    });

            File localFile = File.createTempFile("profile picture", "jpg");
            riversRef.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // Successfully downloaded data to local file
                            // ...
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle failed download
                    // ...
                }
            });

             */
        }
    }
    private  void displayUserInformation()
    {
        databaseReference.child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0)
                {
                    String name=null;
                    String phone=null;
                    name=dataSnapshot.child("name").getValue().toString();
                    phone=dataSnapshot.child("phone").getValue().toString();
                    if(name!=null && phone!=null)
                    {
                        name=dataSnapshot.child("name").getValue().toString();
                        phone=dataSnapshot.child("phone").getValue().toString();
                    }

                    nameText.setText(name);
                    phoneText.setText(phone);
                    if(dataSnapshot.hasChild("image"))
                    {
                        String img=dataSnapshot.child("image").getValue().toString();
                        //display image in one line of code
                        Picasso.get().load(img).into(circleImageView);
                        Picasso.get().load(img).into(closeBtn);
                    }
                    if(getString.equals("Drivers"))
                    {
                        String car=dataSnapshot.child("car").getValue().toString();
                        driverCarName.setText(car);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
