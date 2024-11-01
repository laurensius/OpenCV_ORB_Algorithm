package com.kodekita.testopencvandroid;

import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.Log;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.ORB;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.Utils;
import android.graphics.Bitmap;
import android.widget.ImageView;



public class SimpleORBActivity extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_simple_orb);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageView = findViewById(R.id.imageView);

        if (OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "OpenCV initialized successfully");
            detectORBFeatures();
        } else {
            Log.e("OpenCV", "OpenCV initialization failed");
        }
    }

    private void detectORBFeatures() {
        Mat img = new Mat();
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.top_right);
        Utils.bitmapToMat(bitmap, img);
        Mat grayImg = new Mat();
        Imgproc.cvtColor(img, grayImg, Imgproc.COLOR_BGR2GRAY);
        ORB orb = ORB.create();
        MatOfKeyPoint keyPoints = new MatOfKeyPoint();
        orb.detect(grayImg, keyPoints);
        Mat outputImg = new Mat();
        Features2d.drawKeypoints(img, keyPoints, outputImg, new Scalar(0, 255, 0), Features2d.DrawMatchesFlags_DRAW_RICH_KEYPOINTS);
        Bitmap outputBitmap = Bitmap.createBitmap(outputImg.cols(), outputImg.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(outputImg, outputBitmap);
        imageView.setImageBitmap(outputBitmap);
    }
}