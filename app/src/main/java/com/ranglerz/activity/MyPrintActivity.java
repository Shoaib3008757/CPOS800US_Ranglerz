package com.ranglerz.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;

public class MyPrintActivity extends Activity {

    Button startPrintAcitity;
    Boolean firstTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_print);

        startPrintAcitity = (Button) findViewById(R.id.start_print_activity);

        startPrintActivity();

    }

    //starting new printActivity
    public void startPrintActivity() {
        //click listener for button startPrintActivity
        startPrintAcitity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                checkIsFirstTime();

                /*Intent i = new Intent(MyPrintActivity.this, EnterFingerprintActivity.class);
                startActivity(i);
*/
            }
        });
    }

    //check for first time
    public void checkIsFirstTime(){
        SharedPreferences sharedPreferences = this.getSharedPreferences("mPreferences", Context.MODE_APPEND);
        firstTime = sharedPreferences.getBoolean("firstTime", true);
        if (firstTime){

            Intent i = new Intent(MyPrintActivity.this, EnterFingerprintActivity.class);
            startActivity(i);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("firstTime", false);
            editor.commit();

        }else {
            Intent i = new Intent(MyPrintActivity.this, CustomPrinterActivity.class);
            startActivity(i);
        }
}

    @Override
    public void onBackPressed() {



        //  super.onBackPressed();

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title

        //alertDialog.setTitle("Exit alert");
        //alertDialog.setMessage("Do You want Exit??");
        //alertDialog .setIcon(R.drawable.ic_action_alert);

/*
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {

                dialog.cancel();
            }
        });


        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                MyPrintActivity.super.onBackPressed();


            }
        });*/


        alertDialog.show();



    }//end of onBackPress function
    }

