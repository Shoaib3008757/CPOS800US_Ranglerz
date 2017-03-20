package com.ranglerz.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.ranglerz.asynctask.AsyncFingerprint;
import com.ranglerz.utils.ToastUtil;

import java.util.Timer;
import java.util.TimerTask;

import android_serialport_api.FingerprintAPI;
import android_serialport_api.SerialPortManager;

public class EnterFingerprintActivity extends Activity {

    Button bValidate;

    private byte[] model;

    private MyApplication application;
    private ProgressDialog progressDialog;
    private AsyncFingerprint asyncFingerprint;
    private Timer mtimer;


    //handler
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            Log.i("cy",
                    "Enter function FingerprintActivity-Handler-handleMessage()");
            super.handleMessage(msg);

            switch (msg.what) {
                case AsyncFingerprint.SHOW_PROGRESSDIALOG:
                    cancleProgressDialog();
                    showProgressDialog((Integer) msg.obj);
                    break;

                /*case AsyncFingerprint.SHOW_FINGER_IMAGE:
                    showFingerImage(msg.arg1, (byte[]) msg.obj);
                    break;

                case AsyncFingerprint.SHOW_FINGER_MODEL:
                    FingerprintActivity.this.model = (byte[]) msg.obj;
                    if (null != FingerprintActivity.this.model) {
                        Log.i("cy", "The length of Finger model is "
                                + FingerprintActivity.this.model.length);
                    }
                    cancleProgressDialog();
                    ToastUtil.showToast(FingerprintActivity.this, "pageId = "
                            + msg.arg1 + "  store = " + msg.arg2);
                    break;
*/

               /* case AsyncFingerprint.REGISTER_SUCCESS:
                    cancleProgressDialog();
                    if (null != msg.obj) {
                        Integer id = (Integer) msg.obj;
                        ToastUtil.showToast(EnterFingerprintActivity.this,
                                getString(R.string.register_success)
                                        + "  pageId = " + id);
                    } else {
                        ToastUtil.showToast(EnterFingerprintActivity.this,
                                R.string.register_success);
                    }
                    break;

                case AsyncFingerprint.REGISTER_FAIL:
                    cancleProgressDialog();
                    ToastUtil.showToast(EnterFingerprintActivity.this,
                            R.string.register_fail);
                    break;*/


                case AsyncFingerprint.VALIDATE_RESULT1:
                    cancleProgressDialog();
                    showValidateResult((Boolean) msg.obj);
                    break;

    case AsyncFingerprint.VALIDATE_RESULT2:
                    cancleProgressDialog();
                    Integer ret = (Integer) msg.obj;

        //************************

        /*            if (-1 != ret) {
                        ToastUtil.showToast(EnterFingerprintActivity.this,
                                getString(R.string.verifying_through) + "  pageId="
                                        + ret);
                    } else {
                        showValidateResult(false);
                    }
        */

        //***************************

                        showValidateResult(true);
                        break;


                /*case AsyncFingerprint.UP_IMAGE_RESULT:
                    cancleProgressDialog();
                    ToastUtil
                            .showToast(EnterFingerprintActivity.this, (Integer) msg.obj);
                    break;*/

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_fingerprint);

        viewInitialization();
        dataInitialization();
        //opening serial port for

        SerialPortManager.getInstance().openSerialPortPrinter();

        openigDilaog();
        buttonValidateFingerprint();




    }//end of onCreate


    //intialization views
    public void viewInitialization(){
        bValidate = (Button)findViewById(R.id.bValidate);
        mtimer = new Timer();

    }//end of viewInitialization

    //data initialiation
    public void dataInitialization(){

        application = (MyApplication) this.getApplicationContext();
        asyncFingerprint = new AsyncFingerprint(application.getHandlerThread()
                .getLooper(), handler);

        asyncFingerprint
                .setFingerprintType(FingerprintAPI.SMALL_FINGERPRINT_SIZE);

    }//end of dataInitialization

    //click lister for button
    public void buttonValidateFingerprint(){
        bValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showing progress dialog

                asyncFingerprint.validate2();

            }
        });
    }//end of buttonValidateFingerprint

    public void openigDilaog(){
        showProgressDialog(R.string.isopenGpio);
        mtimer.schedule(new TimerTask() {

            @Override
            public void run() {
                cancleProgressDialog();
            }
        }, 1500);
    }//end of openingDilaog

    //showPrograssDialog
    private void showProgressDialog(int resId) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(resId)); // 通过ID获取对应的值
        progressDialog.setCanceledOnTouchOutside(false);

        progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                if (KeyEvent.KEYCODE_BACK == keyCode) {
                    asyncFingerprint.setStop(true);
                }
                return false;
            }
        });
        progressDialog.show();
    }//end of showPrograssdDialog

    //cancel prograss Diloag
    private void cancleProgressDialog() {
        if (null != progressDialog && progressDialog.isShowing()) {
            progressDialog.cancel();
            progressDialog = null;
        }
    }//end of cancelPrograssDilaog




    //show Validdate Result
    private void showValidateResult(boolean matchResult) {


        if (matchResult) {

            Intent customePrintActivity = new Intent(EnterFingerprintActivity.this, CustomPrinterActivity.class);
            startActivity(customePrintActivity);
            finish();
        }

    }//end of showValidateResult




    @Override
    protected void onDestroy() {
        cancleProgressDialog();
        handler.removeCallbacksAndMessages(null);
        //SerialPortManager.getInstance().closeSerialPort(2);
        super.onDestroy();
    }//end of onDestroy



    }


