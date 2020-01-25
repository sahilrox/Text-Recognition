package com.example.textrecognition;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button uploadBtn, detectBtn;
    private ImageButton cameraBtn;
    private static final int Image_Capture_Code = 1;
    private static final int RESULT_LOAD_IMAGE = 2;
    private static final int SELECT_PICTURE = 100;
    private ImageView image;
    private String resultText;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraBtn = findViewById(R.id.cameraBtn);
        uploadBtn = findViewById(R.id.uploadBtn);
        detectBtn = findViewById(R.id.detectBtn);
        image = findViewById(R.id.capturedImage);
        detectBtn.setEnabled(false);

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, Image_Capture_Code);
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"),SELECT_PICTURE);
            }
        });

        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RecognizerActivity.class);
                intent.putExtra("text",resultText);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        boolean flag = false;
        if (resultCode == RESULT_OK) {
            if (requestCode == Image_Capture_Code) {
                bitmap = (Bitmap) data.getExtras().get("data");
                image.setImageBitmap(bitmap);
                flag = true;
                detectBtn.setEnabled(true);
            } else if (requestCode == SELECT_PICTURE) {
                Uri uri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    image.setImageBitmap(bitmap);
                    detectBtn.setEnabled(true);
                    flag = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == RESULT_CANCELED) {
                Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_LONG).show();
            }
        }

        if(flag) {
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

            FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
                    .getOnDeviceTextRecognizer();

            textRecognizer.processImage(image)
                    .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                        @Override
                        public void onSuccess(FirebaseVisionText result) {
                            // Task completed successfully
                            // ...
                            resultText = result.getText();
                            for (FirebaseVisionText.TextBlock block: result.getTextBlocks()) {
                                String blockText = block.getText();
                                Float blockConfidence = block.getConfidence();
                                List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
                                Point[] blockCornerPoints = block.getCornerPoints();
                                Rect blockFrame = block.getBoundingBox();
                                for (FirebaseVisionText.Line line: block.getLines()) {
                                    String lineText = line.getText();
                                    Float lineConfidence = line.getConfidence();
                                    List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
                                    Point[] lineCornerPoints = line.getCornerPoints();
                                    Rect lineFrame = line.getBoundingBox();
                                    for (FirebaseVisionText.Element element: line.getElements()) {
                                        String elementText = element.getText();
                                        Float elementConfidence = element.getConfidence();
                                        List<RecognizedLanguage> elementLanguages = element.getRecognizedLanguages();
                                        Point[] elementCornerPoints = element.getCornerPoints();
                                        Rect elementFrame = element.getBoundingBox();
                                    }
                                }
                            }
                        }
                    })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Task failed with an exception
                                    // ...
                                    Toast.makeText(MainActivity.this, "Couldn't recognize text", Toast.LENGTH_SHORT).show();
                                }
                            });


        }
    }


}
