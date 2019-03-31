package com.example.mdp6;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

public class Cell{

    private Paint paint;
    private State state;
    private int x;
    private int y;
    private int height;
    private int width;
    private int row;
    private int col;

    enum State{
        NONE,UNKNOWN, UNEXPLORED, EXPLORED, EMPTY, OBSTACLE, WAYPOINT, EX_WAYPOINT, ARROW_UP, ARROW_DOWN, ARROW_LEFT, ARROW_RIGHT, ARROW, ARROW_BLOCK
    }

    Cell(int x, int y, int height, int width, int rowIndex, int colIndex) {
        paint = new Paint();
        this.state = State.UNKNOWN;
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        this.row = rowIndex;
        this.col = colIndex;
    }

    boolean isClicked(float a, float b) {
        return a < (this.x + width/2) && a > (this.x - width/2) && b < (this.y + height/2) && b > (this.y - height/2);
    }

    void draw(Canvas canvas, Context context){
        Rect cellRect = new Rect(x - width / 2, y - height / 2, x + width / 2, y + height / 2);
        Path triangle = new Path();
        triangle = drawTriangle(triangle);
        Paint triangleColor = new Paint();
        triangleColor.setColor(Color.BLACK);

        switch(state) {
            case UNKNOWN:
            case UNEXPLORED:
                paint.setColor(Color.GRAY);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(cellRect, paint);
                break;
            case EXPLORED:
            case EMPTY:
                paint.setColor(Color.WHITE);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(cellRect, paint);
                break;
            case OBSTACLE:
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(cellRect, paint);
                break;
            case WAYPOINT:
                paint.setColor(Color.YELLOW);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(cellRect, paint);
                break;
            case EX_WAYPOINT:
                paint.setColor(Color.MAGENTA);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(cellRect, paint);
                break;
            case ARROW_UP:
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(cellRect, paint);
                //Bitmap b= BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_arrow_upward_black);
                //canvas.drawBitmap(b,null,cellRect,paint);
                canvas.drawPath(triangle, triangleColor);
                break;
            case ARROW_DOWN:
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(cellRect, paint);
                //b= BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_arrow_downward_black);
                //canvas.drawBitmap(b,null,cellRect,paint);
                canvas.drawPath(triangle, triangleColor);
                break;
            case ARROW_LEFT:
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(cellRect, paint);
                //b= BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_arrow_left_black);
                //canvas.drawBitmap(b,null,cellRect,paint);
                canvas.drawPath(triangle, triangleColor);
                break;
            case ARROW_RIGHT:
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(cellRect, paint);
                //b= BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_arrow_right_black);
                //canvas.drawBitmap(b,null,cellRect,paint);
                canvas.drawPath(triangle, triangleColor);
                break;
            case ARROW:
                //paint.setColor(Color.RED);
                //paint.setColor(Color.TRANSPARENT);
                paint.setColor(Color.WHITE);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(cellRect, paint);
                //b= BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_arrow_right_black);
                //canvas.drawBitmap(b,null,cellRect,paint);
                canvas.drawPath(triangle, triangleColor);
                break;
            case ARROW_BLOCK:
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(cellRect, paint);
                //b= BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_arrow_right_black);
                //canvas.drawBitmap(b,null,cellRect,paint);
                canvas.drawPath(triangle, triangleColor);
                break;
        }
        //paint.setStyle(Paint.Style.FILL);
        //canvas.drawRect(cellRect, paint);


        //  Color destination
        if (row == 0 || row == 1 || row == 2) {
            if (col == 12 || col == 13 || col == 14) {
                paint.setColor(Color.GREEN);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(cellRect, paint);
            }
        }

        // Draw grid
        paint.setColor(Color.DKGRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        canvas.drawRect(cellRect,paint);
    }

    Path drawTriangle(Path triangle){

        Rect cellRect = new Rect(x - width / 2, y - height / 2, x + width / 2, y + height / 2);

        //triangle = new Path();

        //  Move to top middle
        triangle.moveTo((cellRect.left + width / 2), (cellRect.top + height / 6));
        //  Draw line to bottom left
        triangle.lineTo((cellRect.left + width / 6), (cellRect.bottom - height / 6));
        //  Draw line to bottom right
        triangle.lineTo((cellRect.right - width / 6), (cellRect.bottom - height / 6));
        //  Draw line to top middle
        triangle.lineTo((cellRect.left + width / 2), (cellRect.top + height / 6));
        //  Complete
        triangle.close();

        return triangle;
    }

    State getState(){
        return state;
    }

    void setState(State state){
        this.state = state;
    }

    int getX(){
        return x;
    }

    int getY(){
        return y;
    }

    int getRowIndex(){
        return row;
    }

    int getColIndex(){
        return col;
    }

}

