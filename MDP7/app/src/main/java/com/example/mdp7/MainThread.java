package com.example.mdp7;

import android.graphics.Canvas;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;

public class MainThread extends Thread {

    private static final int MAX_FPS = 30;
    private final SurfaceHolder surfaceholder;
    private GridView gridView;
    private boolean running;

    MainThread(SurfaceHolder sf, GridView gv){
        super();
        this.surfaceholder = sf;
        this.gridView = gv;
    }

    void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {
        long startTime;
        long waitTime;
        int frameCount = 0;
        long totalTime = 0;
        long targetTime = 1000/MAX_FPS;

        Canvas canvas;

        while (running) {

            startTime = System.nanoTime();
            canvas = null;
            canvas = this.surfaceholder.lockCanvas();

            synchronized (surfaceholder) {
                    // Update objects in GamePanel
                    this.gridView.update();
                    // Draw updated objects in Canvas
                    this.gridView.draw(canvas);
                }

                if (canvas != null) {
                        surfaceholder.unlockCanvasAndPost(canvas);
                }

            long timeMillis = (System.nanoTime() - startTime)/1000000;
            waitTime = targetTime - timeMillis;

            try {
                if (waitTime > 0) {
                    //  If frame finished earlier than target time, then sleep to cap framerate
                    sleep(waitTime);
                }
            } catch (Exception e) {
                Log.e("main thread:", "Exception occured in run() frame timing: " + e);
            }

            totalTime += System.nanoTime() - startTime;
            frameCount++;

            if (frameCount == MAX_FPS) {
                long averageFPS = 1000/((totalTime/frameCount)/1000000);
                // Reset values to resample frameCount
                frameCount = 0;
                totalTime = 0;
            }
        }
    }
}

