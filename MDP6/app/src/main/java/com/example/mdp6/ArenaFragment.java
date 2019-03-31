package com.example.mdp6;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mdp6.BluetoothService.MsgConstants;
import com.example.mdp6.BluetoothService;
import com.example.mdp6.Cell;
import com.example.mdp6.MapDescriptorDialogFragment;
import com.example.mdp6.Preferences;
import com.example.mdp6.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//public class ArenaFragment extends Fragment{
public class ArenaFragment extends Fragment implements SensorEventListener {


    private final String TAG = "ARENA_FRAG_DEBUG_TAG";

    private static final BluetoothService bluetooth = BluetoothService.getInstance();

    private ArenaView arenaView;
    private RelativeLayout arenaFrame;

    private TextView robotStatusView;
    private Button loadMapButton;
    private Button saveMapButton;
    private Button mapRefreshButton;
    private Button exploreButton;
    private Button fastestPathButton;
    private Button placeStartpointButton;
    private Button placeWaypointButton;
    private Button stopButton;
    private Button mapDescriptorButton;
    private Switch autoSwitch;
    private Switch tiltSwitch;
    private ImageButton rotateLeftButton;
    private ImageButton rotateRightButton;
    private ImageButton forwardButton;

    private Button calibrateFrontButton;
    private Button calibrateRightButton;

    private static boolean isAuto;
    private static boolean isTilt;

    private static String paddedMap1;
    private static String paddedMap2;
    private static String robotMidX;
    private static String robotMidY;
    private static String robotDir;

    String arrowCellString[];

    private int ARENA_COLUMN_COUNT = 15;
    private int ARENA_ROW_COUNT = 20;

    private SensorManager sensorManager;
    private Sensor sensor;
    private Handler tHandler;

    enum Status {
        NONE, EXPLORING, FASTEST
    }

    private Status status;

    String ROBOT_COMMAND_ALGO_PREFIX = "al_";
    String ROBOT_COMMAND_ARDUI_PREFIX = "ar_";
    String ROBOT_COMMAND_FORWARD = "W";
    String ROBOT_COMMAND_ROTATE_LEFT = "A";
    String ROBOT_COMMAND_ROTATE_RIGHT = "D";
    String ROBOT_COMMAND_BEGIN_EXPLORATION = "explore";
    String ROBOT_COMMAND_BEGIN_FASTEST = "fastest";

    public ArenaFragment() {
        status = Status.NONE;
        isAuto = true;
        isTilt = false;
        paddedMap1 = "";
        paddedMap2 = "";
        robotMidX = "";
        robotMidY = "";
        robotDir = "";
        setArguments(new Bundle());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  Register handler callback to handle BluetoothService messages
        bluetooth.registerNewHandlerCallback(bluetoothServiceMessageHandler);

        /*if(getArguments() != null){
            String msg = getArguments().getString("msg");
            Toast.makeText(getContext(),msg,Toast.LENGTH_SHORT).show();
        }*/

        arrowCellString = new String[5];

        if (getArguments().getString("saved_arrow_cell1") != null) {
            arrowCellString[0] = getArguments().getString("saved_arrow_cell1");
        }

        if(getArguments().getString("saved_arrow_cell2") != null) {
            arrowCellString[1] = getArguments().getString("saved_arrow_cell2");
        }

        if(getArguments().getString("saved_arrow_cell3") != null) {
            arrowCellString[2] = getArguments().getString("saved_arrow_cell3");
        }

        if(getArguments().getString("saved_arrow_cell4") != null) {
            arrowCellString[3] = getArguments().getString("saved_arrow_cell4");
        }

        if(getArguments().getString("saved_arrow_cell5") != null) {
            arrowCellString[4] = getArguments().getString("saved_arrow_cell5");
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.arena_fragment, container, false);

        sensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        robotStatusView = view.findViewById(R.id.robot_status_text);
        loadMapButton = view.findViewById(R.id.load_map_button);
        saveMapButton = view.findViewById(R.id.save_map_button);
        mapRefreshButton = view.findViewById(R.id.map_refresh_button);
        exploreButton = view.findViewById(R.id.explore_button);
        fastestPathButton = view.findViewById(R.id.fastest_path_button);
        placeStartpointButton = view.findViewById(R.id.place_startpoint_button);
        placeWaypointButton = view.findViewById(R.id.place_waypoint_button);
        stopButton = view.findViewById(R.id.stop_button);
        mapDescriptorButton = view.findViewById(R.id.mapdescriptor_button);
        autoSwitch = view.findViewById(R.id.auto_switch);
        tiltSwitch = view.findViewById(R.id.tilt_switch);
        rotateLeftButton = view.findViewById(R.id.rotate_left_button);
        rotateRightButton = view.findViewById(R.id.rotate_right_button);
        forwardButton = view.findViewById(R.id.forward_button);
        calibrateFrontButton = view.findViewById(R.id.calibrate_front_button);
        calibrateRightButton = view.findViewById(R.id.calibrate_right_button);

        calibrateRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetooth.getState() == BluetoothService.State.CONNECTED) {
                    arenaView.setArenaAction(ArenaView.ArenaAction.NONE);
                    bluetooth.sendMessageToRemoteDevice(ROBOT_COMMAND_ARDUI_PREFIX + "C");
                }
            }
        });

        calibrateFrontButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetooth.getState() == BluetoothService.State.CONNECTED) {
                    arenaView.setArenaAction(ArenaView.ArenaAction.NONE);
                    bluetooth.sendMessageToRemoteDevice(ROBOT_COMMAND_ARDUI_PREFIX + "c");
                }
            }
        });

        loadMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSavedArena();
            }
        });

        saveMapButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                if(paddedMap1.length()!=0 && paddedMap2.length()!=0) {
                    Preferences.savePreference(getContext(), R.string.saved_map1, paddedMap1);
                    Preferences.savePreference(getContext(), R.string.saved_map2, paddedMap2);
                }
            }
        });

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetooth.getState() == BluetoothService.State.CONNECTED) {
                    arenaView.setArenaAction(ArenaView.ArenaAction.NONE);
                    bluetooth.sendMessageToRemoteDevice(ROBOT_COMMAND_ARDUI_PREFIX + ROBOT_COMMAND_FORWARD);
                }
            }
        });

        rotateRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetooth.getState() == BluetoothService.State.CONNECTED) {
                    arenaView.setArenaAction(ArenaView.ArenaAction.NONE);
                    bluetooth.sendMessageToRemoteDevice(ROBOT_COMMAND_ARDUI_PREFIX + ROBOT_COMMAND_ROTATE_RIGHT);
                }
            }
        });

        rotateLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetooth.getState() == BluetoothService.State.CONNECTED) {
                    arenaView.setArenaAction(ArenaView.ArenaAction.NONE);
                    bluetooth.sendMessageToRemoteDevice(ROBOT_COMMAND_ARDUI_PREFIX + ROBOT_COMMAND_ROTATE_LEFT);
                }
            }
        });

        tiltSwitch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(tiltSwitch.isChecked()){
                    isTilt = true;
                }else{
                    isTilt = false;
                }
            }
        });

        autoSwitch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(autoSwitch.isChecked()){
                    isAuto = true;
                    mapRefreshButton.setEnabled(false);
                }else{
                    isAuto = false;
                    mapRefreshButton.setEnabled(true);
                }
            }
        });

        mapRefreshButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                updateMap();
            }

        });

        placeStartpointButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                arenaView.setArenaAction(ArenaView.ArenaAction.PLACING_STARTPOINT);
            }
        });

        placeWaypointButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                arenaView.setArenaAction(ArenaView.ArenaAction.PLACING_WAYPOINT);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                status = Status.NONE;
                robotStatusView.setText("NONE");
                Preferences.savePreference(getContext(),R.string.saved_status,"NONE");
            }
        });

        mapDescriptorButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                MapDescriptorDialogFragment dialog = new MapDescriptorDialogFragment();

                Bundle args1 = new Bundle();
                args1.putString("map1", Preferences.readPreference(getContext(), R.string.saved_map1));
                args1.putString("map2", Preferences.readPreference(getContext(), R.string.saved_map2));

                //Cell[] arrowCellArray = arenaView.getArrowCell();

                for(int i=0; i<5; i++){
                    if(arrowCellString[i] != null){

                        String a = "cell" + (i+1);
                        String b = "(" + arrowCellString[i] + ")";
                        args1.putString(a,b);
                    }else{
                        String k = "cell" + (i+1);
                        args1.putString(k,"");
                    }
                }
                dialog.setArguments(args1);
                dialog.show(getFragmentManager(),"MAP");
            }
        });

        exploreButton.setOnClickListener(exploreOnClickListener);

        fastestPathButton.setOnClickListener(fastestPathOnClickListener);

        arenaFrame = view.findViewById(R.id.arena_frame);

        //  Resize Arena frame to display arena properly
        ViewGroup.LayoutParams layoutParams = arenaFrame.getLayoutParams();

        //  Adjust arena width and height to display cells
        layoutParams.width = layoutParams.width - (layoutParams.width % ARENA_COLUMN_COUNT) + 1;
        layoutParams.height = layoutParams.height - (layoutParams.height % ARENA_ROW_COUNT) + 1;
        arenaFrame.setLayoutParams(layoutParams);

        //  Add ArenaView to frame
        arenaView = new ArenaView(getContext());
        arenaView.setFragment(this);
        arenaFrame.addView(arenaView);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        arenaFrame.removeAllViews();
    }

    public void loadSavedArena() {

        //String savedRobotPosition = Preferences.readPreference(getContext(), R.string.saved_robot_position);
        //String savedWaypoint = Preferences.readPreference(getContext(), R.string.saved_waypoint);

        //  Load saved robot position and direction
        /*if (savedRobotPosition.trim().length() != 0) {
            String[] robotPos = savedRobotPosition.split(",");
            int x = Integer.parseInt(robotPos[0]);
            int y = Integer.parseInt(robotPos[1]);
            float rot = Float.parseFloat(robotPos[2]);
            arenaView.moveRobot(x, y, rot);
        }*/

        //  Load way point and update map
        /*if (savedWaypoint.trim().length() != 0) {
            String[] wpPos = savedWaypoint.split(",");
            int wpX = Integer.parseInt(wpPos[0]);
            int wpY = Integer.parseInt(wpPos[1]);
            arenaView.setWaypoint(wpX, wpY);
        }*/

        paddedMap1 = Preferences.readPreference(getContext(), R.string.saved_map1);
        paddedMap2 = Preferences.readPreference(getContext(), R.string.saved_map2);

        updateMap();
    }

    public void restoreStatus(){
        String prev_status = Preferences.readPreference(getContext(),R.string.saved_status);
        robotStatusView.setText(prev_status);
        switch(prev_status){
            case "NONE":
            case "":
                status = Status.NONE;
                break;
            case "EXPLORING":
                status = Status.EXPLORING;
                break;
            case "FASTEST":
                status = Status.FASTEST;
                break;
        }
    }

    public void setStatus(Status stat){
        status = stat;
    }

    private void moveRobot(String robotMidX, String robotMidY, String robotDir) {
        if (robotMidX.trim().length() != 0 && robotMidY.trim().length() != 0) {

            int x = Integer.parseInt(robotMidX);
            int y = Integer.parseInt(robotMidY);

            int rot = 0;
            switch (robotDir) {
                case "n":
                    rot = 0;
                    break;
                case "s":
                    rot = 180;
                    break;
                case "e":
                    rot = 90;
                    break;
                case "w":
                    rot = 270;
                    break;
            }
            arenaView.moveRobot(x, y, rot);

        }
    }

    private void processMessage(String message) {

        Pattern pattern;
        Matcher matcher;
        String[] contents;

        if (status == Status.EXPLORING) {

            //message arrives in 1,3,N, fff.....000, fff....000
            pattern = Pattern.compile("[0-9]+,[0-9]+,(?:N|S|E|W)+,[0-9a-fA-F]+,[0-9a-fA-F]+,", Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(message);

            //for arrowCell message
            Pattern pattern2 = Pattern.compile("~[0-9]+~[0-9]+~(?:u|d|l|r)+~", Pattern.CASE_INSENSITIVE);
            Matcher matcher2 = pattern2.matcher(message);

            while (matcher.find()) {
                message = matcher.group();

                //  Get contents of message
                contents = message.split(",");
                robotMidX = contents[0].trim();
                robotMidY = contents[1].trim();
                robotDir = contents[2].trim().toLowerCase();
                paddedMap1 = contents[3].trim();
                paddedMap2 = contents[4].trim();

                Preferences.savePreference(getContext(), R.string.saved_map1, paddedMap1);
                Preferences.savePreference(getContext(), R.string.saved_map2, paddedMap2);

                Log.d("maprefreshprocessmsg:",paddedMap1+"\n"+paddedMap2);

                if(isAuto){
                    updateMap();
                }
            }

            while(matcher2.find()){
                message = matcher2.group();
                contents = message.split("~");
                int arrowCellX  = Integer.parseInt(contents[1].trim());
                int arrowCellY  = Integer.parseInt(contents[2].trim());
                String arrowCellDir = contents[3].trim().toLowerCase();

                String temp = arrowCellX + "," + arrowCellY + ","+ arrowCellDir;

                Toast.makeText(getContext(), "received image coordinate: " + temp, Toast.LENGTH_SHORT).show();

                for(int i=0; i<5; i++){
                    if(arrowCellString[i] != null){
                        String[] content = arrowCellString[i].split(",");
                        int x  = Integer.parseInt(content[0].trim());
                        int y  = Integer.parseInt(content[1].trim());
                        String dir = contents[2].trim().toLowerCase();

                        Log.e("arrowcell"+i,arrowCellString[i]);
                        Log.e("arrowcell"+i, Integer.toString(x));
                        Log.e("arrowcell"+i, Integer.toString(y));
                        Log.e("arrowcell"+i, dir);

                        Log.e("arrowcell_temp"+i, Integer.toString(arrowCellX));
                        Log.e("arrowcell_temp"+i, Integer.toString(arrowCellY));
                        Log.e("arrowcell_temp"+i, arrowCellDir);

                        if(arrowCellX == x && arrowCellY == y) {
                            Log.e("arrowcell" + i , "same arrow");
                            break;
                        }
                    }
                    else if(arrowCellString[i] == null){
                        //arrowCellString[i] = temp;
                        //arenaView.setArrowCell(arrowCellX,arrowCellY,arrowCellDir);
                        Cell tempCell = arenaView.getCell(arrowCellX,arrowCellY);
                        if(tempCell.getState() == Cell.State.OBSTACLE){
                            arenaView.setArrowCell(arrowCellX, arrowCellY);
                            arrowCellString[i] = temp;
                        }else {
                            switch(arrowCellDir){
                                case "u": arrowCellY = arrowCellY - 1;
                                            break;
                                case "d": arrowCellY = arrowCellY + 1;
                                            break;
                                case "l": arrowCellX = arrowCellX + 1;
                                            break;
                                case "r": arrowCellX = arrowCellX - 1;
                                            break;
                            }
                            if(arenaView.getCell(arrowCellX, arrowCellY).getState() == Cell.State.OBSTACLE){
                                arenaView.setArrowCell(arrowCellX,arrowCellY);
                                String temp2 = arrowCellX + "," + arrowCellY + ","+ arrowCellDir;
                                arrowCellString[i] = temp2;
                            }
                        }
                        break;
                    }
                }
            }

        } else if (status == Status.FASTEST) {

            pattern = Pattern.compile("[0-9]+,[0-9]+,(?:n|s|e|w)", Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(message);

            while (matcher.find()) {
                message = matcher.group();
                contents = message.split(",");
                robotMidX = contents[0].trim();
                robotMidY = contents[1].trim();
                robotDir = contents[2].trim().toLowerCase();

                if(isAuto) {
                    //moveRobot(robotMidX, robotMidY, robotDir);
                    updateMap();
                }
            }
        }
    }

    private void updateMap(){

        if(paddedMap1.length()!= 0 && paddedMap2.length()!=0) {
            arenaView.updateMap1(paddedMap1);
            arenaView.updateMap2(paddedMap2);
        }

        if(robotMidX.length()!=0 && robotMidY.length()!=0 && robotDir.length()!=0 ){
            moveRobot(robotMidX, robotMidY, robotDir);
        }

        Cell waypointCell = arenaView.getWaypointCell();
        if(waypointCell != null){
            waypointCell.setState(Cell.State.WAYPOINT);
        }

        for(int i=0; i<5; i++){
            if(arrowCellString[i] != null){
                String[] contents = arrowCellString[i].split(",");
                int arrowCellX  = Integer.parseInt(contents[0].trim());
                int arrowCellY  = Integer.parseInt(contents[1].trim());
                //String arrowCellDir = contents[2].trim().toLowerCase();
                //Toast.makeText(getContext(),"arrow to display is: " + arrowCellX + "," + arrowCellY, Toast.LENGTH_SHORT).show();
                arenaView.setArrowCell(arrowCellX, arrowCellY);
            }
        }
    }

    private View.OnClickListener exploreOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            arenaView.setArenaAction(ArenaView.ArenaAction.NONE);
            final String waypointMsg = arenaView.getWaypointInfo();

            if(waypointMsg.trim().length()!= 0) {
                String[] wp = waypointMsg.split(",");

                if (arenaView.getRobot() == null) {
                    Toast.makeText(getContext(), "Please select a starting point", Toast.LENGTH_SHORT).show();
                } else {
                    if (bluetooth.getState() == BluetoothService.State.CONNECTED) {

                        // al_explore:10,10,N|3,3
                        bluetooth.sendMessageToRemoteDevice(ROBOT_COMMAND_ALGO_PREFIX + ROBOT_COMMAND_BEGIN_EXPLORATION + ":" + arenaView.getRobotStartingPosition() + "|" + waypointMsg);
                        // ar_Ew
                        bluetooth.sendMessageToRemoteDevice(ROBOT_COMMAND_ARDUI_PREFIX + "E" + arenaView.getRobotDir());

                        status = Status.EXPLORING;
                        robotStatusView.setText("EXPLORING");
                        Preferences.savePreference(getContext(),R.string.saved_status,"EXPLORING");
                        Toast.makeText(getContext(),"Robot exploring",Toast.LENGTH_SHORT).show();

                        for(int i=0; i<5; i++){
                            arrowCellString[i] = null;
                        }

                    } else {
                        Toast.makeText(getContext(), "Bluetooth is not connected", Toast.LENGTH_SHORT).show();
                    }

                }
            }else{
                Toast.makeText(getContext(), "Waypoint is not placed", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private View.OnClickListener fastestPathOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            arenaView.setArenaAction(ArenaView.ArenaAction.NONE);
            final String waypointMsg = arenaView.getWaypointInfo();
            if(waypointMsg.trim().length()!= 0){
                //String[] wp = waypointMsg.split(",");

                if (bluetooth.getState() == BluetoothService.State.CONNECTED) {

                    if(arenaView.getRobot() == null) {
                        Toast.makeText(getContext(),"Please select a starting point",Toast.LENGTH_SHORT).show();
                    }else {

                        //arenaView.sendRobotStartingPosition();
                        //al_fastest:10,10,N
                        //bluetooth.sendMessageToRemoteDevice(ROBOT_COMMAND_BEGIN_FASTEST);
                        bluetooth.sendMessageToRemoteDevice(ROBOT_COMMAND_ALGO_PREFIX + ROBOT_COMMAND_BEGIN_FASTEST + ":" + arenaView.getRobotStartingPosition());
                        bluetooth.sendMessageToRemoteDevice(ROBOT_COMMAND_ARDUI_PREFIX + "F");
                        status = Status.FASTEST;
                        robotStatusView.setText("FASTEST");
                        Preferences.savePreference(getContext(),R.string.saved_status,"FASTEST");
                        Toast.makeText(getContext(),"Robot on fastest path",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getContext(), "Bluetooth is not connected", Toast.LENGTH_SHORT).show();
                }

            }else{
                Toast.makeText(getContext(), "Waypoint is not placed", Toast.LENGTH_SHORT).show();

            }
        }
    };

    private final Handler.Callback bluetoothServiceMessageHandler = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            try {
                switch (message.what) {
                    case MsgConstants.MESSAGE_READ:
                        //  Reading message from remote device
                        String receivedMessage = message.obj.toString();
                        //  Handle message from Pi
                        processMessage(receivedMessage);
                        break;
                    case MsgConstants.MESSAGE_WRITE:
                        //  Writing message to remote device
                        break;
                    case MsgConstants.BT_CONNECTED:
                        //  Successfully connected to remote device
                        String deviceName = message.obj.toString();
                        Toast.makeText(getContext(), "Connected to remote device: " + deviceName, Toast.LENGTH_SHORT).show();
                        //bluetoothStatusSwitch.setChecked(true);
                        break;
                    case MsgConstants.BT_DISCONNECTING:
                    case MsgConstants.BT_CONNECTION_LOST:
                        //  Connection to remote device lost
                        bluetooth.disconnect();
                        Toast.makeText(getContext(), "Connection to remote device lost", Toast.LENGTH_SHORT).show();
                        //  Switch back to Bluetooth Fragment
                        //MainActivity.addFragment(MainActivity.BLUETOOTH_TAG);
                        //bluetoothStatusSwitch.setChecked(false);
                        break;
                    case MsgConstants.BT_ERROR_OCCURRED:
                        //  An error occured during connection
                        bluetooth.disconnect();
                        Toast.makeText(getContext(), "A Bluetooth error occurred", Toast.LENGTH_SHORT).show();
                        break;
                }
            } catch (Throwable t) {
                Log.e(TAG,null, t);
            }
            return false;
        }
    };

    //while switching fragments, save arrow images coordinates
    @Override
    public void onPause(){
        super.onPause();
        //getArguments().putString("msg","YESSSSS");

        for(int i=0; i<5; i++){
            if(arrowCellString[i] != null){
                switch (i) {
                    case 0:
                        getArguments().putString("saved_arrow_cell1", arrowCellString[i]);
                        break;
                    case 1:
                        getArguments().putString("saved_arrow_cell2", arrowCellString[i]);
                        break;
                    case 2:
                        getArguments().putString("saved_arrow_cell3", arrowCellString[i]);
                        break;
                    case 3:
                        getArguments().putString("saved_arrow_cell4", arrowCellString[i]);
                        break;
                    case 4:
                        getArguments().putString("saved_arrow_cell5", arrowCellString[i]);
                        break;
                }
        }
    }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        tHandler = new Handler();
        float x = event.values[0];
        float y = event.values[1];
        if (Math.abs(x) > Math.abs(y)) {
                //turn right
                if (x < -5) {
                    {
                        if (isTilt) {
                            robotStatusView.setText("Turn right");
                            tHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    bluetooth.sendMessageToRemoteDevice(ROBOT_COMMAND_ARDUI_PREFIX + ROBOT_COMMAND_ROTATE_RIGHT);

                                }
                            }, 1000);
                        }
                    }
                }

                //turn left
                else if (x > 5) {
                    {
                        if (isTilt) {
                            robotStatusView.setText("Turn Left");
                            tHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    bluetooth.sendMessageToRemoteDevice(ROBOT_COMMAND_ARDUI_PREFIX + ROBOT_COMMAND_ROTATE_LEFT);
                                }
                            }, 1000);
                        }
                    }
                }
            }

            else {
                //forward
                if (y < -3) {
                    {
                        if (isTilt) {
                            robotStatusView.setText("Forward");
                            tHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    bluetooth.sendMessageToRemoteDevice(ROBOT_COMMAND_ARDUI_PREFIX + ROBOT_COMMAND_FORWARD);

                                }
                            }, 1000);
                        }
                    }
                }
            }
        }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

