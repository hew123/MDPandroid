package com.example.mdp7;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.mdp7.BluetoothService.MsgConstants;


public class ChatFragment extends Fragment {

    public static BluetoothService bluetoothService;
    private TextView connectedDevice;
    private ArrayAdapter<String> messagesListAdapter;
    private Button disconnectButton;
    String deviceName;

    public ChatFragment() {

        bluetoothService = BluetoothService.getInstance();
        if (bluetoothService.getState() == BluetoothService.State.CONNECTED) {
            deviceName = bluetoothService.getConnectedDeviceName();
        }
        bluetoothService.registerNewHandlerCallback(bluetoothServiceMessageHandler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.chat_fragment, container, false);

        disconnectButton = view.findViewById(R.id.disconnect_button);
        connectedDevice = view.findViewById(R.id.connected_device);
        ListView messages = view.findViewById(R.id.messages);
        messagesListAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        messages.setAdapter(messagesListAdapter);
        final EditText typeMessage = view.findViewById(R.id.type_message);
        Button sendButton = view.findViewById(R.id.send_button);

        if (bluetoothService.getState() == BluetoothService.State.CONNECTED) {
            String deviceName = bluetoothService.getConnectedDeviceName();
            connectedDevice.setText(deviceName);
            disconnectButton.setEnabled(true);
        }

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothService.disconnect();
                bluetoothService.listen();
                connectedDevice.setText("");
                messagesListAdapter.clear();
                deviceName ="";
                disconnectButton.setEnabled(false);
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = typeMessage.getText().toString().trim();
                if (message.length() != 0) {
                    typeMessage.setText("");
                    bluetoothService.sendMsg(message);
                }
            }
        });

        return view;
    }

    private final Handler.Callback bluetoothServiceMessageHandler = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {

                switch (message.what) {
                    case MsgConstants.MESSAGE_READ:
                        //  Reading message from remote device
                        String receivedMessage = message.obj.toString();
                        messagesListAdapter.add(deviceName + ": " + receivedMessage);
                        return false;
                    case MsgConstants.MESSAGE_WRITE:
                        //  Writing message to remote device
                        String sendingMessage = message.obj.toString();
                        messagesListAdapter.add("robot: " + sendingMessage);
                        return false;
                    case MsgConstants.BT_CONNECTED:
                        //  Successfully connected to remote device
                        String deviceName2 = message.obj.toString();
                        deviceName = deviceName2;
                        Toast.makeText(getContext(), "Connected to remote device: " + deviceName, Toast.LENGTH_SHORT).show();
                        connectedDevice.setText(deviceName);

                        return false;
                    case MsgConstants.BT_DISCONNECTING:
                    case MsgConstants.BT_CONNECTION_LOST:
                        //  Connection to remote device lost
                        Toast.makeText(getContext(), "Connection to remote device lost", Toast.LENGTH_SHORT).show();
                        bluetoothService.disconnect();
                        bluetoothService.listen();
                        deviceName = "";
                        connectedDevice.setText("");
                        disconnectButton.setEnabled(false);
                        return false;
                    case MsgConstants.BT_ERROR_OCCURRED:
                        //  An error occured during connection
                        Toast.makeText(getContext(), "A Bluetooth error occurred", Toast.LENGTH_SHORT).show();
                        bluetoothService.disconnect();
                        bluetoothService.listen();
                        deviceName = "";
                        connectedDevice.setText("");
                        disconnectButton.setEnabled(false);
                        return false;
                }
            return false;
        }
    };
}

