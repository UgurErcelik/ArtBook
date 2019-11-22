package com.ugurercelik.artbook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.ugurercelik.artbook.MainActivity.imageArray;

public class Main2Activity extends AppCompatActivity {

    ImageView imageView2;
    EditText nameText;
    EditText artistText;
    Button saveButton;
    SQLiteDatabase database;
    Bitmap selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        imageView2=findViewById(R.id.imageView2);
        nameText = findViewById(R.id.nameEditText);
        artistText = findViewById(R.id.artistEditText);
        saveButton = findViewById(R.id.saveButton);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if(info.matches("newArt")){
            nameText.setText("");
            artistText.setText("");
           // Bitmap selectBitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.select);
            //imageView2.setImageBitmap(selectBitmap);
            saveButton.setVisibility(View.VISIBLE);

        }else{
            saveButton.setVisibility(View.INVISIBLE);
            String name = intent.getStringExtra("name");
            String artist = intent.getStringExtra("artist");
            Bitmap image = imageArray.get(intent.getIntExtra("position",0));
            nameText.setText(name);
            artistText.setText(artist);
            imageView2.setImageBitmap(image);


        }

    }

    public void save(View view) {
        String name = nameText.getText().toString();
        String artist = artistText.getText().toString();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        selectedImage.compress(Bitmap.CompressFormat.PNG, 25, outputStream);
        byte[] byteArray = outputStream.toByteArray();

        try{

        database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE IF NOT EXISTS arts (name VARCHAR,artist VARCHAR,image BLOB)");

        String sqlString = "INSERT INTO arts (name,artist,image) VALUES (?,?,?)";
        SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
        sqLiteStatement.bindString(1, name);
        sqLiteStatement.bindString(2, artist);
        sqLiteStatement.bindBlob(3, byteArray);
        sqLiteStatement.execute();

        }catch(Exception e){
            e.printStackTrace();

        }
        /*
        nameArray.add(name);
        artistArray.add(artist);
        imageArray.add(selectedImage);
        arrayAdapter.notifyDataSetChanged();
        */

        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        //finish();


    }

    public void selectImage(View view){

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},2);

        }else{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 1 && resultCode == RESULT_OK && data != null){


           Uri imageData  = data.getData();

            try {
                if(Build.VERSION.SDK_INT >= 28){
                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(),imageData);
                    selectedImage = ImageDecoder.decodeBitmap(source);
                    imageView2.setImageBitmap(selectedImage);

                }else{
                    selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageData);
                    imageView2.setImageBitmap(selectedImage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == 2){

            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,1);

            }

        }



        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
