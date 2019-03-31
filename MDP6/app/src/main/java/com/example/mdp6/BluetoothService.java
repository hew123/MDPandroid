package com.example.mdp6;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.UUID;

import static com.example.mdp6.BluetoothService.State.NONE;
import static com.example.mdp6.BluetoothService.State.CONNECTED;
import static com.example.mdp6.BluetoothService.State.CONNECTING;
import static com.example.mdp6.BluetoothService.State.LISTEN;
import static com.example.mdp6.BluetoothService.MsgConstants.BT_ERROR_OCCURRED;
import static com.example.mdp6.BluetoothService.MsgConstants.BT_CONNECTED;
import static com.example.mdp6.BluetoothService.MsgConstants.BT_CONNECTION_LOST;
import static com.example.mdp6.BluetoothService.MsgConstants.BT_DISCONNECTING;
import static com.example.mdp6.BluetoothService.MsgConstants.MESSAGE_READ;
import static com.example.mdp6.BluetoothService.MsgConstants.MESSAGE_TOAST;
import static com.example.mdp6.BluetoothService.MsgConstants.MESSAGE_WRITE;



public class BluetoothService {

    private static final String TAG = "BT_DEBUG_TAG";
    private static volatile BluetoothService INSTANCE = null;

    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final String myUUID_string = "00001101-0000-1000-8000-00805f9b34fb";

    private static ConnectThread connectThread;
    private static ConnectedThread connectedThread;
    private static AcceptThread acceptThread;
    private Handler handler;
    private final BluetoothAdapter BTadapter = BluetoothAdapter.getDefaultAdapter();

    // BluetoothService state
    private State state;

    //  Available BT states
    public enum State {
        NONE, LISTEN, CONNECTING, CONNECTED
    }

    public interface MsgConstants{

        int MESSAGE_READ = 0;
        int MESSAGE_WRITE = 1;
        int MESSAGE_TOAST = 2;

        int BT_CONNECTED = 100;
        int BT_DISCONNECTING = 101;
        int BT_CONNECTION_LOST = 102;
        int BT_ERROR_OCCURRED = 404;
    }

    private BluetoothService() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                return false;
            }
        });
        state = State.NONE;
    }

    public static BluetoothService getInstance()  {
        if (INSTANCE == null) {
            synchronized (BluetoothService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BluetoothService();
                }
            }
        }
        return INSTANCE;
    }

    // Send messages/updates to Activities
    private synchronized void sendServiceMessage(int messageConstant) {
        handler.obtainMessage(messageConstant, -1, -1).sendToTarget();
    }

    private synchronized void sendServiceMessage(int messageConstant, Object object) {
        handler.obtainMessage(messageConstant, -1, -1, object).sendToTarget();
    }

    // Used by Activity to handle messages sent to it from this service
    public void registerNewHandlerCallback(Handler.Callback callback) {
        handler = new Handler(callback);
    }

    // Start listening for incoming Bluetooth connections
    synchronized void listen() {
        //  Start listening on socket
        if (acceptThread == null && state != LISTEN) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
    }

    // Connect to a specified BluetoothDevice
    synchronized void connect(BluetoothDevice device) {
        disconnect();
        if (connectThread == null) {
            connectThread = new ConnectThread(device);
            connectThread.start();
        }
    }

    // Disconnect from any existing connections
    public synchronized void disconnect() {
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
        state = State.NONE;

        //  Listen to incoming Bluetooth connections
        listen();
    }

    // Perform relevant actions on connected BluetoothSocket
    private synchronized void manageConnectedSocket(BluetoothSocket socket) {
        if (socket.isConnected()) {
            connectedThread = new ConnectedThread(socket);
            connectedThread.start();
        }
    }
    // Get name of connected remote device
    String getConnectedDeviceName() {
        if (state == CONNECTED) {
            return connectedThread.mmSocket.getRemoteDevice().getName();
        } else {
            return "";
        }
    }

    // return State of BluetoothService State
    public State getState() {
        return state;
    }

    public synchronized void sendMessageToRemoteDevice(String message) {
        //  Create temporary object
        synchronized (this) {
            if (state != CONNECTED) return;
        }
        connectedThread.write(message);
    }

    // Connection initiation
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice device;

        ConnectThread(BluetoothDevice mdevice) {
            BluetoothSocket tmp = null;
            device = mdevice;
            try {
                tmp = mdevice.createRfcommSocketToServiceRecord(myUUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
                sendServiceMessage(BT_ERROR_OCCURRED);
            }
            mmSocket = tmp;
            state = CONNECTING;
        }

        public void run() {
            BTadapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    Log.e(TAG, "Unable to connect, closing client socket", connectException);
                    sendServiceMessage(BT_DISCONNECTING, device.getName());
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                    sendServiceMessage(BT_ERROR_OCCURRED);
                }
                return;
            }

            synchronized (BluetoothService.this) {
                connectThread = null;
            }

            sendServiceMessage(BT_CONNECTED, device.getName());
            manageConnectedSocket(mmSocket);
        }

        // Closes the client socket and causes the thread to finish.
        void cancel() {
            try {
                sendServiceMessage(BT_DISCONNECTING, device.getName());
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
                sendServiceMessage(BT_ERROR_OCCURRED);
            }
        }
    }

    // connection handling
    private class ConnectedThread extends Thread {
        private BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer;

        ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
                sendServiceMessage(BT_ERROR_OCCURRED);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
                sendServiceMessage(BT_ERROR_OCCURRED);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            state = CONNECTED;
        }

        public void run() {

            mmBuffer = new byte[256];
            int numBytes;
            String message;

            // Keep listening to the InputStream until an exception occurs.
            // Keep listening to the InputStream until an exception occurs.
            while (state == CONNECTED) {
                try {
                    //  Read from the InputStream
                    numBytes = mmInStream.read(mmBuffer);
                    message = new String(mmBuffer).substring(0, numBytes);
                    Log.d(TAG, "(" + numBytes + "): " + message);
                    // Send the obtained bytes to the UI activity.
                    handler.obtainMessage(MESSAGE_READ, numBytes, -1, message).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "Input stream was disconnected", e);
                    sendServiceMessage(BT_CONNECTION_LOST);
                    break;
                }
            }
        }

        // Call this from the main activity to sendMessageToRemoteDevice data to the remote device.
        void write(String command) {
            try {
                // Allocate bytes for integer indicating size and message itself
                mmOutStream.write(command.getBytes());
                mmOutStream.flush();
                // Share the sent message with the UI activity.
                handler.obtainMessage(MESSAGE_WRITE, command.getBytes().length, -1, command).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
                sendServiceMessage(BT_ERROR_OCCURRED);
            }
        }

        // Call this method from the main activity to shut down the connection.
        void cancel() {
            try {
                sendServiceMessage(BT_DISCONNECTING);
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
                sendServiceMessage(BT_ERROR_OCCURRED);
            }
        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                tmp = BTadapter.listenUsingRfcommWithServiceRecord(myUUID_string, myUUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
            state = LISTEN;
        }

        public void run() {
            BluetoothSocket socket;
            // Keep listening until exception occurs or a socket is returned.
            while (state != CONNECTED) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (state) {
                            case LISTEN:
                            case CONNECTING:
                                // A connection was accepted. Perform work associated with
                                // the connection in a separate thread.
                                sendServiceMessage(BT_CONNECTED, socket.getRemoteDevice().getName());
                                manageConnectedSocket(socket);
                                cancel();
                                break;
                            case NONE:
                            case CONNECTED:
                                cancel();
                                break;
                        }
                    }
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}
