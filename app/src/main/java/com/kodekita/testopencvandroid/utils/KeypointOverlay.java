package com.kodekita.testopencvandroid.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

public class KeypointOverlay extends View {
    private List<Point> keypoints = new ArrayList<>();

    public KeypointOverlay(Context context) {
        super(context);
    }

    public KeypointOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setKeypoints(List<Point> keypoints) {
        this.keypoints = keypoints;
        invalidate(); // Meminta untuk menggambar ulang
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        @SuppressLint("DrawAllocation") Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(5);
        paint.setAntiAlias(true);

        // Menggambar titik kunci
        for (Point point : keypoints) {
            canvas.drawCircle((float) point.x, (float) point.y, 5, paint); // Menggambar lingkaran kecil pada setiap titik kunci
        }
    }
}