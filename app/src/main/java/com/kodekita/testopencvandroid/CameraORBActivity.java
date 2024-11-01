package com.kodekita.testopencvandroid;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.*;
import org.opencv.features2d.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.Utils;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
public class CameraORBActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ImageView imageView;
    private ORB orb;
    private BFMatcher matcher;

    private static final int CAMERA_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cek izin kamera
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            startCamera();  // Memulai kamera jika izin sudah diberikan
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera_orb);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        previewView = findViewById(R.id.previewView);
        imageView = findViewById(R.id.imageView);

        if (!OpenCVLoader.initLocal()) {
            throw new RuntimeException("Error initializing OpenCV");
        }

        orb = ORB.create();
        matcher = BFMatcher.create(BFMatcher.BRUTEFORCE_HAMMINGLUT, true);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ORBAnalyzer());

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis, preview);
    }

    private class ORBAnalyzer implements ImageAnalysis.Analyzer {

        @Override
        public void analyze(@NonNull ImageProxy imageProxy) {
            Mat cameraMat = imageToMat(imageProxy);
            Imgproc.cvtColor(cameraMat, cameraMat, Imgproc.COLOR_YUV2GRAY_420);

            MatOfKeyPoint keyPoints = new MatOfKeyPoint();
            Mat descriptors = new Mat();
            orb.detectAndCompute(cameraMat, new Mat(), keyPoints, descriptors);

            Mat outputImage = new Mat();
            Features2d.drawKeypoints(cameraMat, keyPoints, outputImage);

            Bitmap bitmap = Bitmap.createBitmap(outputImage.cols(), outputImage.rows(), Bitmap.Config.ARGB_8888);

            Utils.matToBitmap(outputImage, bitmap);
            runOnUiThread(() -> {
                displayBitmap(bitmap);
            });

            imageProxy.close();
        }

        private Mat imageToMat(ImageProxy image) {
            byte[] nv21 = convertYUV420888ToNV21(image);
            Mat mat = new Mat(image.getHeight() + image.getHeight() / 2, image.getWidth(), CvType.CV_8UC1);
            mat.put(0, 0, nv21);
            return mat;
        }

        private byte[] convertYUV420888ToNV21(ImageProxy image) {
            ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
            ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
            ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

            int ySize = yBuffer.remaining();
            int uSize = uBuffer.remaining();
            int vSize = vBuffer.remaining();

            byte[] nv21 = new byte[ySize + uSize + vSize];

            yBuffer.get(nv21, 0, ySize);
            vBuffer.get(nv21, ySize, vSize);
            uBuffer.get(nv21, ySize + vSize, uSize);

            return nv21;
        }

        public Bitmap rotateBitmap(Bitmap source, float angle) {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, false);
        }

        private void displayBitmap(Bitmap bitmap) {
            imageView.setImageDrawable(null);
            Bitmap rotatedBitmap = rotateBitmap(bitmap, 90); // Putar 90 derajat
            int desiredWidth = imageView.getWidth();
            int desiredHeight = imageView.getHeight();
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(rotatedBitmap, desiredWidth, desiredHeight, false);
            imageView.setImageBitmap(scaledBitmap);
        }
    }




}