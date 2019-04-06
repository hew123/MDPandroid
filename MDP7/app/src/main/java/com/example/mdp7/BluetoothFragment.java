package com.example.mdp7;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;

import java.util.HashSet;
import java.util.Set;
import com.example.mdp7.BluetoothService.MsgConstants;

public class BluetoothFragment extends Fragment{

    private BluetoothAdapter BTadapter;
    private Set<BluetoothDevice> discoveredDevices;

    private Button scanButton;
    private ListView pairedDeviceList;
    private ListView discoveredDeviceList;
    private ArrayAdapter<String> pairedDeviceListAdapter;
    private ArrayAdapter<String> discoveredDeviceListAdapter;

    public static BluetoothService bluetoothService;

    public BluetoothFragment() {

        BTadapter = BluetoothAdapter.getDefaultAdapter();

        discoveredDevices = new HashSet<>();

        bluetoothService = BluetoothService.getInstance();

        bluetoothService.registerNewHandlerCallback(bluetoothServiceMessageHandler);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //  Create intent filter and register broadcast receiver
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //  Stop Bluetooth discovery
        BTadapter.cancelDiscovery();

        //  Unregister receivers
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bluetooth_fragment, container, false);

        //  Instantiate unconnected layout
        scanButton = view.findViewById(R.id.connect_button);
        ListView pairedDeviceList = view.findViewById(R.id.paired_devices);
        pairedDeviceListAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        pairedDeviceList.setAdapter(pairedDeviceListAdapter);
        discoveredDeviceList = view.findViewById(R.id.discovered_devices);
        discoveredDeviceListAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        discoveredDeviceList.setAdapter(discoveredDeviceListAdapter);

        //  Get paired devices and display on list
        final Set<BluetoothDevice> pairedDevices = BTadapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice pairedDevice : pairedDevices) {
                pairedDeviceListAdapter.add(pairedDevice.getName());
            }
        }

        //  If paired device is clicked, connect
        pairedDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String chosenDeviceName = ((TextView) view).getText().toString();
                for (BluetoothDevice pairedDevice : pairedDevices) {
                    if (pairedDevice.getName().equalsIgnoreCase(chosenDeviceName)) {
                        bluetoothService.connect(pairedDevice);
                        Toast.makeText(getContext(),"Connecting bluetooth..",Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        });

        //  Scan button action
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discoveredDeviceListAdapter.clear();
                if (BTadapter.isDiscovering()) {
                    BTadapter.cancelDiscovery();
                }
                BTadapter.startDiscovery();
                Toast.makeText(getContext(),"Scanning..",Toast.LENGTH_SHORT).show();
            }
        });

        //  If discovered device is clicked, connect
        discoveredDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String chosenDeviceName = ((TextView) view).getText().toString();
                for (BluetoothDevice device : discoveredDevices) {
                    if (device.getName().equalsIgnoreCase(chosenDeviceName)) {
                        discoveredDeviceListAdapter.clear();
                        discoveredDevices.clear();
                        bluetoothService.connect(device);
                        Toast.makeText(getContext(),"Connecting bluetooth..",Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        });

        return view;
    }
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        //  Bluetooth device discovery started
                        scanButton.setText("discovering nearby devices");
                        scanButton.setEnabled(false);
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        //  Bluetooth device discovery completed
                        scanButton.setText("scanning nearby devices");
                        scanButton.setEnabled(true);
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        //  Bluetooth device discovered, get information from Intent
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        String deviceName = device.getName();
                        discoveredDeviceListAdapter.add(deviceName);
                        discoveredDeviceListAdapter.notifyDataSetChanged();
                        discoveredDevices.add(device);
                        break;
                }
            }
        }
    };

    private final Handler.Callback bluetoothServiceMessageHandler = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
                switch (message.what) {
                    case MsgConstants.BT_CONNECTED:
                        String deviceName = message.obj.toString();
                        Toast.makeText(getContext(), "Connected to remote device: " + deviceName, Toast.LENGTH_SHORT).show();
                        return false;
                    case MsgConstants.BT_DISCONNECTING:
                    case MsgConstants.BT_CONNECTION_LOST:
                        Toast.makeText(getContext(), "Connection to remote device lost", Toast.LENGTH_SHORT).show();
                        bluetoothService.disconnect();
                        bluetoothService.listen();
                        return false;
                    case MsgConstants.BT_ERROR_OCCURRED:
                        Toast.makeText(getContext(), "A Bluetooth error occurred", Toast.LENGTH_SHORT).show();
                        bluetoothService.disconnect();
                        bluetoothService.listen();
                        return false;
                }
            return false;
        }
    };

}
