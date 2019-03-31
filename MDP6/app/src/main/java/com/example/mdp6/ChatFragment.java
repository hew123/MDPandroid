package com.example.mdp6;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.mdp6.BluetoothService.MsgConstants;

import java.util.HashSet;

public class ChatFragment extends Fragment {

    private static final String TAG = "CHAT_DEBUG_TAG";

    private TextView mConnectedDeviceText;
    private Button mDisconnectButton;
    private EditText mSendBluetoothMessage;
    private Button mSendBluetoothMessageButton;
    private ListView mBluetoothMessages;
    private ArrayAdapter<String> mBluetoothMessagesListAdapter;
    public static BluetoothService bluetoothService;
    String deviceName;

    public ChatFragment() {

        bluetoothService = BluetoothService.getInstance();
        if (bluetoothService.getState() == BluetoothService.State.CONNECTED) {
            deviceName = bluetoothService.getConnectedDeviceName();
        }
        //BTadapter = BluetoothAdapter.getDefaultAdapter();

        //  Register handler callback to handle BluetoothService messages
        bluetoothService.registerNewHandlerCallback(bluetoothServiceMessageHandler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.chat_fragment, container, false);

        mDisconnectButton = view.findViewById(R.id.bluetooth_disconnect_button);
        mConnectedDeviceText = view.findViewById(R.id.bluetooth_connected_device);
        mBluetoothMessages = view.findViewById(R.id.bluetooth_messages);
        mBluetoothMessagesListAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        mBluetoothMessages.setAdapter(mBluetoothMessagesListAdapter);
        mSendBluetoothMessage = view.findViewById(R.id.bluetooth_message);
        mSendBluetoothMessageButton = view.findViewById(R.id.send_bluetooth_message_button);

        //  If already connected to a device, restore connected view
        if (bluetoothService.getState() == BluetoothService.State.CONNECTED) {
            String deviceName = bluetoothService.getConnectedDeviceName();
            mConnectedDeviceText.setText(deviceName);
            mDisconnectButton.setEnabled(true);
        }

        //  Disconnect button action
        mDisconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothService.disconnect();
                bluetoothService.listen();
                mConnectedDeviceText.setText("");
                mBluetoothMessagesListAdapter.clear();
                deviceName ="";
                mDisconnectButton.setEnabled(false);
            }
        });

        //  Send Message button action
        mSendBluetoothMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = mSendBluetoothMessage.getText().toString().trim();
                if (message.length() != 0) {
                    mSendBluetoothMessage.setText("");
                    bluetoothService.sendMessageToRemoteDevice(message);
                }
            }
        });

        return view;
    }

    // Handle messages from BluetoothService
    private final Handler.Callback bluetoothServiceMessageHandler = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            try {
                switch (message.what) {
                    case MsgConstants.MESSAGE_READ:
                        //  Reading message from remote device
                        String receivedMessage = message.obj.toString();
                        mBluetoothMessagesListAdapter.add(deviceName + ": " + receivedMessage);
                        return false;
                    case MsgConstants.MESSAGE_WRITE:
                        //  Writing message to remote device
                        String sendingMessage = message.obj.toString();
                        mBluetoothMessagesListAdapter.add("ARC: " + sendingMessage);
                        return false;
                    case MsgConstants.BT_CONNECTED:
                        //  Successfully connected to remote device
                        String deviceName2 = message.obj.toString();
                        deviceName = deviceName2;
                        Toast.makeText(getContext(), "Connected to remote device: " + deviceName, Toast.LENGTH_SHORT).show();
                        mConnectedDeviceText.setText(deviceName);

                        return false;
                    case MsgConstants.BT_DISCONNECTING:
                    case MsgConstants.BT_CONNECTION_LOST:
                        //  Connection to remote device lost
                        Toast.makeText(getContext(), "Connection to remote device lost", Toast.LENGTH_SHORT).show();
                        bluetoothService.disconnect();
                        bluetoothService.listen();
                        deviceName = "";
                        mConnectedDeviceText.setText("");
                        mDisconnectButton.setEnabled(false);
                        return false;
                    case MsgConstants.BT_ERROR_OCCURRED:
                        //  An error occured during connection
                        Log.e(TAG, "BT_ERROR_OCCURRED: A Bluetooth error occurred");
                        Toast.makeText(getContext(), "A Bluetooth error occurred", Toast.LENGTH_SHORT).show();
                        bluetoothService.disconnect();
                        bluetoothService.listen();
                        deviceName = "";
                        mConnectedDeviceText.setText("");
                        mDisconnectButton.setEnabled(false);
                        return false;
                }
            } catch (Throwable t) {
                Log.e(TAG,null, t);
            }

            return false;
        }
    };
}

