package com.example.mdp6;

import android.graphics.Canvas;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

public class MainThread extends Thread {

    private final static String TAG = "MAIN_THREAD_DEBUG_TAG";
    private static final int MAX_FPS = 30;
    private final SurfaceHolder surfaceholder;
    private ArenaView arenaview;
    private boolean running;

    MainThread(SurfaceHolder sf, ArenaView av){
        super();
        this.surfaceholder = sf;
        this.arenaview = av;
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

            try {
                canvas = this.surfaceholder.lockCanvas();
                synchronized (surfaceholder) {
                    // Update objects in GamePanel
                    this.arenaview.update();
                    // Draw updated objects in Canvas
                    this.arenaview.draw(canvas);
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception occured in run(): " + e);
            } finally {
                if (canvas != null) {
                    try {
                        surfaceholder.unlockCanvasAndPost(canvas);
                    } catch (Exception e) {
                        Log.e(TAG, "Exception occured in run() finally block: " + e);
                    }
                }
            }

            long timeMillis = (System.nanoTime() - startTime)/1000000;
            waitTime = targetTime - timeMillis;

            try {
                if (waitTime > 0) {
                    //  If frame finished earlier than target time, then sleep to cap framerate
                    sleep(waitTime);
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception occured in run() frame timing: " + e);
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

