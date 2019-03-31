package com.example.mdp6;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import com.example.mdp6.BluetoothService.MsgConstants;

public class BluetoothSettingFragment extends Fragment{

    private static final String TAG = "BT_setting_DEBUG_TAG";
    private BluetoothAdapter BTadapter;
    private Set<BluetoothDevice> mDiscoveredDevices;

    private Button mScanButton;
    private ListView mPairedDeviceList;
    private ListView mDiscoveredDeviceList;
    private ArrayAdapter<String> mPairedDeviceListAdapter;
    private ArrayAdapter<String> mDiscoveredDeviceListAdapter;

    public static BluetoothService bluetoothService;

    public BluetoothSettingFragment() {

        BTadapter = BluetoothAdapter.getDefaultAdapter();

        mDiscoveredDevices = new HashSet<>();

        bluetoothService = BluetoothService.getInstance();

        //  Register handler callback to handle BluetoothService messages
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
        View view = inflater.inflate(R.layout.bluetooth_setting_fragment, container, false);

        //  Instantiate unconnected layout
        mScanButton = view.findViewById(R.id.bluetooth_connect_button);
        ListView mPairedDeviceList = view.findViewById(R.id.bluetooth_paired_device_list);
        mPairedDeviceListAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        mPairedDeviceList.setAdapter(mPairedDeviceListAdapter);
        mDiscoveredDeviceList = view.findViewById(R.id.bluetooth_discovered_device_list);
        mDiscoveredDeviceListAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        mDiscoveredDeviceList.setAdapter(mDiscoveredDeviceListAdapter);

        //  Get paired devices and display on list
        final Set<BluetoothDevice> pairedDevices = BTadapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice pairedDevice : pairedDevices) {
                mPairedDeviceListAdapter.add(pairedDevice.getName());
            }
        }

        //  If paired device is clicked, connect
        mPairedDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDiscoveredDeviceListAdapter.clear();
                if (BTadapter.isDiscovering()) {
                    BTadapter.cancelDiscovery();
                }
                BTadapter.startDiscovery();
                Toast.makeText(getContext(),"Scanning..",Toast.LENGTH_SHORT).show();
            }
        });

        //  If discovered device is clicked, connect
        mDiscoveredDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String chosenDeviceName = ((TextView) view).getText().toString();
                for (BluetoothDevice device : mDiscoveredDevices) {
                    if (device.getName().equalsIgnoreCase(chosenDeviceName)) {
                        mDiscoveredDeviceListAdapter.clear();
                        mDiscoveredDevices.clear();
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
                        mScanButton.setText(R.string.bluetooth_discovering);
                        mScanButton.setEnabled(false);
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        //  Bluetooth device discovery completed
                        mScanButton.setText(R.string.bluetooth_scan);
                        mScanButton.setEnabled(true);
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        //  Bluetooth device discovered, get information from Intent
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        String deviceName = device.getName();
                        mDiscoveredDeviceListAdapter.add(deviceName);
                        mDiscoveredDeviceListAdapter.notifyDataSetChanged();
                        mDiscoveredDevices.add(device);
                        break;
                }
            }
        }
    };

    // Handle messages from BluetoothService
    private final Handler.Callback bluetoothServiceMessageHandler = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            try {
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
                        Log.e(TAG, "BT_ERROR_OCCURRED: A Bluetooth error occurred");
                        Toast.makeText(getContext(), "A Bluetooth error occurred", Toast.LENGTH_SHORT).show();
                        bluetoothService.disconnect();
                        bluetoothService.listen();
                        return false;
                }
            } catch (Throwable t) {
                Log.e(TAG,null, t);
            }

            return false;
        }
    };

}
