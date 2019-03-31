package com.example.mdp6;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.mdp6.BluetoothSettingFragment;
import com.example.mdp6.ChatFragment;
import com.example.mdp6.CustomCommandFragment;
import com.example.mdp6.R;

public class MainActivity extends AppCompatActivity {

    ArenaFragment Afragment;
    ChatFragment Cfragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentManager fragmentManager1 = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction1 = fragmentManager1.beginTransaction();
        BluetoothSettingFragment BTfragment = new BluetoothSettingFragment();
        fragmentTransaction1.replace(R.id.fragment_container, BTfragment);
        fragmentTransaction1.commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //  Inflate the menu, adding items to the action bar if present
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch (item.getItemId()) {
            case R.id.bluetooth:
                BluetoothSettingFragment BTfragment = new BluetoothSettingFragment();
                fragmentTransaction.replace(R.id.fragment_container, BTfragment);
                fragmentTransaction.commit();
                return true;
            case R.id.arena:

                //only create the fragment once, so that it is easy to retreive saved bundle
                if(Afragment == null) {
                    Afragment = new ArenaFragment();
                }
                //ArenaFragment Afragment = new ArenaFragment();
                fragmentTransaction.replace(R.id.fragment_container, Afragment);
                fragmentTransaction.commit();
                return true;
            case R.id.chat:

                ChatFragment Cfragment = new ChatFragment();
                fragmentTransaction.replace(R.id.fragment_container, Cfragment);
                fragmentTransaction.commit();
                return true;
            case R.id.custom_command:
                CustomCommandFragment CCfragment = new CustomCommandFragment();
                fragmentTransaction.replace(R.id.fragment_container, CCfragment);
                fragmentTransaction.commit();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
