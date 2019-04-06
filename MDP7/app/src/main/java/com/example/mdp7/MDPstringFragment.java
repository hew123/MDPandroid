package com.example.mdp7;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class MDPstringFragment extends DialogFragment {

    String map1;
    String map2;
    String cell1;
    String cell2;
    String cell3;
    String cell4;
    String cell5;

    public MDPstringFragment() {
        //
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        map1 = args.getString("map1");
        map2 = args.getString("map2");
        cell1 = args.getString("cell1");
        cell2 = args.getString("cell2");
        cell3 = args.getString("cell3");
        cell4 = args.getString("cell4");
        cell5 = args.getString("cell5");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.mdp_string_fragment, null);

        TextView map1Text = view.findViewById(R.id.map1);
        TextView map2Text = view.findViewById(R.id.map2);
        TextView cell1Text = view.findViewById(R.id.cell1);
        TextView cell2Text = view.findViewById(R.id.cell2);
        TextView cell3Text = view.findViewById(R.id.cell3);
        TextView cell4Text = view.findViewById(R.id.cell4);
        TextView cell5Text = view.findViewById(R.id.cell5);

        if(map1.length() != 0){
            map1Text.setText(map1);
        }else{
            map1Text.setText("NONE");
        }

        if(map2.length() !=0){
            map2Text.setText(map2);
        }else{
            map2Text.setText("NONE");
        }

        if(cell1.length()!=0){
            cell1Text.setText(cell1);
        }

        if(cell2.length()!=0){
            cell2Text.setText(cell2);
        }

        if(cell3.length()!=0){
            cell3Text.setText(cell3);
        }

        if(cell4.length()!=0){
            cell4Text.setText(cell4);
        }

        if(cell5.length()!=0){
            cell5Text.setText(cell5);
        }

        builder.setView(view)
                .setNegativeButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //  Cancel dialog
                        MDPstringFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

}

