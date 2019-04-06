package com.example.mdp7;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;


public class Robot {

    private Point position;
    private int height;
    private int width;
    private float rotation;

    private Paint boundColor = new Paint();
    private Rect bound;
    private Paint triangleColor = new Paint();
    private Path triangle;

    public Robot(int x, int y, int height, int width, int rotation){

        this.position = new Point(x, y);
        this.height = height;
        this.width = width;
        this.rotation = rotation;

        boundColor.setColor(Color.BLUE);
        triangleColor.setColor(Color.CYAN);
        this.position = new Point(x, y);
        this.bound = new Rect(position.x - (width / 3), position.y - (height / 3) * 2, position.x + (width / 3) * 2, position.y + (height / 3));

        drawTriangle();

    }

    void draw(Canvas canvas) {

        canvas.save();
        canvas.rotate(this.rotation, position.x + (width / 6), position.y - (height / 6));
        canvas.drawRect(bound, boundColor);
        canvas.drawPath(triangle, triangleColor);
        canvas.restore();
    }

    void drawTriangle(){
        triangle = new Path();
        //  Move to top middle
        triangle.moveTo((bound.left + width / 2), (bound.top + height / 6));
        //  Draw line to bottom left
        triangle.lineTo((bound.left + width / 6), (bound.bottom - height / 6));
        //  Draw line to bottom right
        triangle.lineTo((bound.right - width / 6), (bound.bottom - height / 6));
        //  Draw line to top middle
        triangle.lineTo((bound.left + width / 2), (bound.top + height / 6));
        //  Complete
        triangle.close();
    }

    void update() {
        bound.set(position.x - (width / 3), position.y - (height / 3) * 2, position.x + (width / 3) * 2, position.y + (height / 3));
        drawTriangle();
        setRotation(rotation);
    }

    Point getPosition() {
        return position;
    }

    void setPosition(int x, int y) {
        this.position = new Point(x, y);
    }

    float getRotation() {
        return rotation;
    }

    void setRotation(float rotation) {
        this.rotation = rotation;
    }
}

