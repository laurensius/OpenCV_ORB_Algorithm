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
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.ORB;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.Utils;
import android.graphics.Bitmap;
import android.widget.ImageView;

import org.opencv.features2d.*;


public class MatchingORBActivity extends AppCompatActivity {


    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_matching_orb);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageView = findViewById(R.id.imageView);

        if (OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "OpenCV initialized successfully");
            matchORBFeatures();
        } else {
            Log.e("OpenCV", "OpenCV initialization failed");
        }
    }

    private void matchORBFeatures() {
        // Load the two images
        Mat img1 = new Mat();
        Mat img2 = new Mat();
        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.image_1);
        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.image_2);
        Utils.bitmapToMat(bitmap1, img1);
        Utils.bitmapToMat(bitmap2, img2);

        // Convert to grayscale
        Imgproc.cvtColor(img1, img1, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(img2, img2, Imgproc.COLOR_BGR2GRAY);

        // Initialize ORB detector
        ORB orb = ORB.create();
        MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
        MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
        Mat descriptors1 = new Mat();
        Mat descriptors2 = new Mat();

        // Detect keypoints and compute descriptors
        orb.detectAndCompute(img1, new Mat(), keypoints1, descriptors1);
        orb.detectAndCompute(img2, new Mat(), keypoints2, descriptors2);

        // Match descriptors using BFMatcher
        BFMatcher bfMatcher = BFMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING, true);
        MatOfDMatch matches = new MatOfDMatch();
        bfMatcher.match(descriptors1, descriptors2, matches);

        // Draw matches
        Mat outputImg = new Mat();
        Features2d.drawMatches(img1, keypoints1, img2, keypoints2, matches, outputImg);

        // Display result
        Bitmap outputBitmap = Bitmap.createBitmap(outputImg.cols(), outputImg.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(outputImg, outputBitmap);
        imageView.setImageBitmap(outputBitmap);
    }
}