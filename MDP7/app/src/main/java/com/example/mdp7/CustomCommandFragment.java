package com.example.mdp7;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CustomCommandFragment extends Fragment{

    private static final BluetoothService BTservice = BluetoothService.getInstance();

    Button savebutton1, savebutton2, sendbutton1, sendbutton2;

    public CustomCommandFragment(){
        //empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.custom_command_fragment, container, false);

        savebutton1 = view.findViewById(R.id.save1);
        savebutton2 = view.findViewById(R.id.save2);
        sendbutton1 = view.findViewById(R.id.send1);
        sendbutton2 = view.findViewById(R.id.send2);

        String Value1 = Preferences.readPreference(getContext(), R.string.config1_key, R.string.config1_default);
        String Value2 = Preferences.readPreference(getContext(), R.string.config2_key, R.string.config2_default);
        TextView new_value1 = view.findViewById(R.id.old_value1);
        TextView new_value2 = view.findViewById(R.id.old_value2);
        new_value1.setText(Value1);
        new_value2.setText(Value2);


        sendbutton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String string = Preferences.readPreference(getContext(), R.string.config1_key, R.string.config1_default);
                if (BTservice.getState() == BluetoothService.State.CONNECTED) {
                    Toast.makeText(getContext(), "Sending command " + string, Toast.LENGTH_SHORT).show();
                    BTservice.sendMsg(string);
                } else {
                    Toast.makeText(getContext(), "Not connected to any remote device, unable to send command", Toast.LENGTH_SHORT).show();
                }
            }
        });

        sendbutton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String string = Preferences.readPreference(getContext(), R.string.config2_key, R.string.config2_default);
                if (BTservice.getState() == BluetoothService.State.CONNECTED) {
                    Toast.makeText(getContext(), "Sending command " + string, Toast.LENGTH_SHORT).show();
                    BTservice.sendMsg(string);
                } else {
                    Toast.makeText(getContext(), "Not connected to any remote device, unable to send command", Toast.LENGTH_SHORT).show();
                }
            }
        });

        savebutton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText newValue = getView().findViewById(R.id.newstring1);
                Preferences.savePreference(getContext(), R.string.config1_key, newValue.getText().toString());
                String string = Preferences.readPreference(getContext(), R.string.config1_key);
                Toast.makeText(getContext(), "command has been reconfigured to " + string, Toast.LENGTH_SHORT).show();
                TextView new_value1 = getView().findViewById(R.id.old_value1);
                new_value1.setText(string);
            }
        });

        savebutton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText newValue = getView().findViewById(R.id.newstring2);
                Preferences.savePreference(getContext(), R.string.config2_key, newValue.getText().toString());
                String string = Preferences.readPreference(getContext(), R.string.config2_key);
                Toast.makeText(getContext(), "command has been reconfigured to " + string, Toast.LENGTH_SHORT).show();
                TextView new_value2 = getView().findViewById(R.id.old_value2);
                new_value2.setText(string);
            }
        });

        return view;
    }
}
