package com.example.mdp7;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class GridView extends SurfaceView implements SurfaceHolder.Callback{

    ArenaFragment arenaFragment;
    private static Robot robot;
    private MainThread thread;

    public static int ROBOT_WIDTH;
    public static int ROBOT_HEIGHT;

    int MIN_HEIGHT = 0;
    int MAX_HEIGHT;
    int MIN_WIDTH = 0;
    int MAX_WIDTH;
    int ARENA_ROW_COUNT = 20;
    int ARENA_COLUMN_COUNT = 15;
    int ARENA_ROBOT_SIZE_COLUMN = 3;
    int ARENA_ROBOT_SIZE_ROW = 3;

    public static int ROBOT_WIDTH_OFFSET;
    public static int ROBOT_HEIGHT_OFFSET;

    private static Cell waypointCell;

    private int CELL_HEIGHT;
    private int CELL_WIDTH;
    private Cell[][] cells;

    private ArenaAction arenaAction = ArenaAction.NONE;

    enum ArenaAction{
        NONE, PLACING_STARTPOINT, PLACING_WAYPOINT
    }

    private StringBuilder stringbuilder;

    public GridView(Context context) {
        super(context);

        getHolder().addCallback(this);
        thread = new MainThread(getHolder(), this);
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceholder){

        thread = new MainThread(getHolder(), this);

        //  Get dimensions of Arena
        MIN_HEIGHT = 0;
        MAX_HEIGHT = getHolder().getSurfaceFrame().height();
        MIN_WIDTH = 0;
        MAX_WIDTH = getHolder().getSurfaceFrame().width();

        //  Cell dimensions
        CELL_HEIGHT = (MAX_HEIGHT - MIN_HEIGHT) / ARENA_ROW_COUNT;
        CELL_WIDTH = (MAX_WIDTH - MIN_WIDTH) / ARENA_COLUMN_COUNT;

        //  Grid cells
        cells = new Cell[ARENA_ROW_COUNT][ARENA_COLUMN_COUNT];
        for (int i = 0; i < ARENA_ROW_COUNT; i++) {
            for (int j = 0; j < ARENA_COLUMN_COUNT; j++) {
                int x = (CELL_WIDTH / 2) + (j * CELL_WIDTH);
                int y = (CELL_HEIGHT / 2) + (i * CELL_HEIGHT);
                cells[i][j] = new Cell(x, y, CELL_HEIGHT, CELL_WIDTH, i, j);
            }
        }
        //  Get robot dimensions based on Cells
        ROBOT_WIDTH = CELL_WIDTH * ARENA_ROBOT_SIZE_COLUMN;
        ROBOT_HEIGHT = CELL_HEIGHT * ARENA_ROBOT_SIZE_ROW;
        ROBOT_WIDTH_OFFSET = ROBOT_WIDTH / 2;
        ROBOT_HEIGHT_OFFSET = ROBOT_HEIGHT / 2;

        //  Start render thread
        thread.setRunning(true);
        thread.start();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceholder){
        boolean retry = true;
        while (retry) {
            try {
                clearArena();
                thread.setRunning(false);
                //  Finish running thread, and terminates it
                thread.join();
                retry = false;
            } catch (Exception e) {
                Log.e("GridView", "Error occurred on surfaceDestroyed(): " + e);
            }
        }
        thread = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){

        arenaFragment.setStatus(ArenaFragment.Status.NONE);

        float x = event.getX();
        float y = event.getY();

        Cell cell = getTouchedCell(x,y);
        int a = getTouchedCellIndex1(x,y);
        int b = getTouchedCellIndex2(x,y);

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(cell!=null){
                    switch (arenaAction){
                        case PLACING_STARTPOINT:
                            moveRobot(a,b,0);
                            Toast.makeText(getContext(), "startpoint: "+Integer.toString(a)+","+Integer.toString(b), Toast.LENGTH_SHORT).show();
                            break;
                        case PLACING_WAYPOINT:
                            //remove current way point
                            if(waypointCell != null){
                                waypointCell.setState(Cell.State.NONE);
                            }
                            if(waypointCell == cell){
                                waypointCell.setState(Cell.State.NONE);
                                waypointCell = null;
                            }else{
                                waypointCell = cell;
                                cell.setState(Cell.State.WAYPOINT);
                                Toast.makeText(getContext(), "waypoint: "+Integer.toString(a)+","+Integer.toString(b), Toast.LENGTH_SHORT).show();
                            }
                            break;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (cell != null) {
                    switch (arenaAction) {
                        case PLACING_STARTPOINT:
                            //  Rotate robot in Arena
                            if (x > (robot.getPosition().x + ROBOT_WIDTH)) {
                                robot.setRotation(90);
                            } else if (y > (robot.getPosition().y + ROBOT_HEIGHT)) {
                                robot.setRotation(180);
                            } else if (x < robot.getPosition().x) {
                                robot.setRotation(270);
                            } else {
                                robot.setRotation(0);
                            }
                            break;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                switch (arenaAction) {
                    case PLACING_STARTPOINT:
                        if (robot != null) {
                            setArenaAction(ArenaAction.NONE);
                        }
                        break;
                    case PLACING_WAYPOINT:
                        if (waypointCell != null) {
                            setArenaAction(ArenaAction.NONE);
                        }
                        break;
                }
                break;
        }
        return true;
    }

    public void update(){
        if (robot != null){
            robot.update();
        }
    }

    //refresh map
    @Override
    public void draw(Canvas canvas){
        super.draw(canvas);

        canvas.drawColor(Color.GRAY);

        if (cells != null) {
            for (int i = 0; i < ARENA_ROW_COUNT; i++) {
                for (int j = 0; j < ARENA_COLUMN_COUNT; j++) {
                    cells[i][j].draw(canvas, getContext());
                }
            }
        }

        if (robot != null){
            robot.draw(canvas);
        }
    }

    private Cell getTouchedCell(float x, float y) {

        for (int i = 0; i < ARENA_ROW_COUNT; i++) {
            for (int j = 0; j < ARENA_COLUMN_COUNT; j++) {
                if (cells[i][j].isClicked(x, y)) {
                    return cells[i][j];
                }
            }
        }
        //  If touched outside of Grid, return to (0, 0)
        return null;
    }

    private int getTouchedCellIndex1(float x, float y){
        for (int i = 0; i < ARENA_ROW_COUNT; i++) {
            for (int j = 0; j < ARENA_COLUMN_COUNT; j++) {
                if (cells[i][j].isClicked(x, y)) {
                    return j;
                }
            }
        }
        return 0;
    }

    private int getTouchedCellIndex2(float x, float y){
        for (int i = 0; i < ARENA_ROW_COUNT; i++) {
            for (int j = 0; j < ARENA_COLUMN_COUNT; j++) {
                if (cells[i][j].isClicked(x, y)) {
                    return ARENA_ROW_COUNT - i -1;
                    //return i;
                }
            }
        }
        return 0;
    }

    public Robot getRobot(){
        return robot;
    }



    private void convertBinaryToHexadecimalString(StringBuilder sb) {
        int index = 0;
        String tmp;
        while (index < sb.length()) {
            //  Get every four characters
            tmp = sb.substring(index, index + 4);
            //  Replace every four characters with its equivalent hexadecimal value
            sb.replace(index, index + 4, Integer.toString(Integer.parseInt(tmp, 2), 16));
            index += 1;
        }
    }

    public void moveRobot(int x, int y, float rotation){
        if (robot == null) {
            robot = new Robot(x, y, ROBOT_HEIGHT, ROBOT_WIDTH, (int) rotation);
        }
        robot.setPosition(x * CELL_WIDTH, MAX_HEIGHT - y * CELL_HEIGHT);

        robot.setRotation(rotation);
        this.update();
    }

    public void convertPaddedMapDescriptorToBinary(StringBuilder sb, String paddedMapDescriptor){

        String character;
        Integer value;
        String binaryString;

        for (int i = 0; i < paddedMapDescriptor.length(); i++) {
            character = String.valueOf(paddedMapDescriptor.charAt(i));
            value = Integer.parseInt(character, 16);
            binaryString = Integer.toBinaryString(value);
            if (binaryString.length() < 4) {
                for (int j = 0; j < (4 - binaryString.length()); j++) {
                    sb.append("0");
                }
            }
            sb.append(binaryString);
        }
    }

    public void updateMap1(String paddedMap1Descriptor){

        stringbuilder = new StringBuilder();
        convertPaddedMapDescriptorToBinary(stringbuilder, paddedMap1Descriptor);

        //  Remove P1 padding (first 2, last 2)
        stringbuilder = stringbuilder.delete(0, 2);
        stringbuilder = stringbuilder.delete(stringbuilder.length() - 1, stringbuilder.length() + 1);

        String character;
        int k = 0;

        for (int i = ARENA_ROW_COUNT - 1; i > - 1; i--) {
            for (int j = 0; j < ARENA_COLUMN_COUNT; j++) {
                character = String.valueOf(stringbuilder.charAt(k));
                if (Integer.parseInt(character) == 0) {
                    cells[i][j].setState(Cell.State.UNEXPLORED);
                } else {
                    cells[i][j].setState(Cell.State.EXPLORED);
                }
                k++;
            }
        }
    }

    public void updateMap2(String paddedMap2Descriptor) {

        stringbuilder = new StringBuilder();
        convertPaddedMapDescriptorToBinary(stringbuilder, paddedMap2Descriptor);

        String character;
        int k = 0;

        for (int i = ARENA_ROW_COUNT - 1; i > -1; i--) {
            for (int j = 0; j < ARENA_COLUMN_COUNT; j++) {
                if (cells[i][j].getState() == Cell.State.EXPLORED) {
                    character = String.valueOf(stringbuilder.charAt(k));
                    if (Integer.parseInt(character) == 0) {
                        cells[i][j].setState(Cell.State.EMPTY);
                    } else {
                        cells[i][j].setState(Cell.State.OBSTACLE);
                    }
                    k++;
                }
            }
        }
    }

    public ArenaAction getArenaAction(){
        return arenaAction;
    }

    public void setArenaAction(ArenaAction action){
        this.arenaAction = action;
    }

    public String getRobotStartingPosition(){

        int cellIndexI = robot.getPosition().x/CELL_WIDTH;
        int cellIndexJ = ARENA_ROW_COUNT - (robot.getPosition().y / CELL_HEIGHT);
        //long rot = (long)rotation;
        int rot = (int)robot.getRotation();
        String dir ="";
        switch (rot) {
            case 0:
                dir = "N";
                break;
            case 180:
                dir = "S";
                break;
            case 90:
                dir = "E";
                break;
            case 270:
                dir = "W";
                break;
        }
        return cellIndexI + "," + cellIndexJ + "," + dir;
    }

    public String getRobotDir(){
        int rot = (int)robot.getRotation();
        String dir ="";
        switch (rot) {
            case 0:
                dir = "n";
                break;
            case 180:
                dir = "s";
                break;
            case 90:
                dir = "e";
                break;
            case 270:
                dir = "w";
                break;
        }
        return dir;
    }

    public String getWaypointInfo(){
        if (waypointCell != null){
            return (waypointCell.getX() / CELL_WIDTH) + "," + (ARENA_ROW_COUNT - (waypointCell.getY() / CELL_HEIGHT) - 1);
        }else{
            return "";
        }
    }

    public Cell getWaypointCell(){
        return waypointCell;
    }

    public Cell getCell(int x, int y){
        return cells[ARENA_ROW_COUNT - y - 1][x];
    }

    public void setArrowCell(int x, int y){

        cells[ARENA_ROW_COUNT - y - 1][x].setState(Cell.State.ARROW_BLOCK);
    }

    // Sets parent fragment of this SurfaceView, to obtain a
    // reference for manipulation
    public void setFragment(ArenaFragment parent){
        arenaFragment = parent;
    }

    public void clearArena() {

        robot = null;

        for (int i = 0; i < ARENA_ROW_COUNT; i++) {
            for (int j = 0; j < ARENA_COLUMN_COUNT; j++) {
                cells[i][j].setState(Cell.State.UNEXPLORED);
                cells[i][j].setState(Cell.State.EMPTY);
            }
        }
    }

}
