package com.ranglerz.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.ranglerz.utils.DataUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android_serialport_api.PrinterAPI;
import android_serialport_api.SerialPortManager;

public class CustomPrinterActivity extends Activity implements AdapterView.OnItemSelectedListener {

    Button printPaper;
    EditText textToPrint;
    EditText phoneNumber;
    EditText presentMileage;
    EditText dueMileage;
    EditText desctription1, desctription2, desctription3, desctription4, desctription5, desctription6, desctription7, desctription8, desctription9;
    EditText price1,  price2,  price3,  price4,  price5,  price6,  price7,  price8,  price9;


    Spinner itemsNumber, productList;


    private PrinterAPI api;

    //alignment values for text to print
    int leftAlignment = 0;
    int centerAlignment = 1;
    int rightAlignment = 2;
    String invoiceNumber = "12345667";
    int totalPrice;
    int tPrice1, tPrice2, tPric3,tPrice4,tPrice5,tPrice6,tPrice7,tPrice8,tPrice9;
    String sr = "0";

    String currentDate = null;
    String descriptionText_1, descriptionText_2, descriptionText_3, descriptionText_4, descriptionText_5, descriptionText_6, descriptionText_7, descriptionText_8, descriptionText_9;
    int priceText_1, priceText_2, priceText_3, priceText_4, priceText_5, priceText_6, priceText_7, priceText_8, priceText_9;


    private ProgressDialog progressDialog;
    private Timer mtimer;
    private static final String headerText = "Guard AutoZone";
    private static final String customerSignature = "Signature _____________________";
    private static final String strightLinet = "----------------------------";
    private boolean boldCheck = true;

    String selectedProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_printer);

        intit();
        //openning serial port for printing paper
        SerialPortManager.getInstance().openSerialPortPrinter();

        showProgressDialog(R.string.isopenGpio);
        mtimer.schedule(new TimerTask() {

            @Override
            public void run() {
                cancleProgressDialog();
            }
        }, 1000);

        getCurrentDate();
        printPaper();





    }



    //intialilzation of views
    public void intit(){

        api = new PrinterAPI();

        printPaper = (Button)findViewById(R.id.print);
        textToPrint = (EditText)findViewById(R.id.name);
        phoneNumber = (EditText)findViewById(R.id.phone_number);
        presentMileage = (EditText)findViewById(R.id.presentMileage);
        dueMileage = (EditText)findViewById(R.id.dueMileage);
        //descriptions
        desctription1 = (EditText)findViewById(R.id.description1);
        desctription1.setVisibility(View.GONE);
        desctription2 = (EditText)findViewById(R.id.description2);
        desctription2.setVisibility(View.GONE);
        desctription3 = (EditText)findViewById(R.id.description3);
        desctription3.setVisibility(View.GONE);
        desctription4 = (EditText)findViewById(R.id.description4);
        desctription4.setVisibility(View.GONE);
        desctription5 = (EditText)findViewById(R.id.description5);
        desctription5.setVisibility(View.GONE);
        desctription6 = (EditText)findViewById(R.id.description6);
        desctription6.setVisibility(View.GONE);
        desctription7 = (EditText)findViewById(R.id.description7);
        desctription7.setVisibility(View.GONE);
        desctription8 = (EditText)findViewById(R.id.description8);
        desctription8.setVisibility(View.GONE);
        desctription9 = (EditText)findViewById(R.id.description9);
        desctription9.setVisibility(View.GONE);
        //price description
        price1 = (EditText)findViewById(R.id.itemPrice1);
        price1.setVisibility(View.GONE);
        price2 = (EditText)findViewById(R.id.itemPrice2);
        price2.setVisibility(View.GONE);
        price3 = (EditText)findViewById(R.id.itemPrice3);
        price3.setVisibility(View.GONE);
        price4 = (EditText)findViewById(R.id.itemPrice4);
        price4.setVisibility(View.GONE);
        price5 = (EditText)findViewById(R.id.itemPrice5);
        price5.setVisibility(View.GONE);
        price6 = (EditText)findViewById(R.id.itemPrice6);
        price6.setVisibility(View.GONE);
        price7 = (EditText)findViewById(R.id.itemPrice7);
        price7.setVisibility(View.GONE);
        price8 = (EditText)findViewById(R.id.itemPrice8);
        price8.setVisibility(View.GONE);
        price9 = (EditText)findViewById(R.id.itemPrice9);
        price9.setVisibility(View.GONE);

        //visibility of view

        price1.setVisibility(View.GONE);

        itemsNumber = (Spinner)findViewById(R.id.itemNumber);
        productList = (Spinner)findViewById(R.id.product_list);
        itemsNumber.setOnItemSelectedListener(this);
        productList.setOnItemSelectedListener(this);
        //api initializtion

        mtimer = new Timer();
    }
    //checking if text is null or not


    //printing text button listerber
    public void printPaper(){


        printPaper.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (textToPrint.getText().length() == 0
                        || phoneNumber.getText().length()==0
                        ||  presentMileage.getText().toString().length()==0
                        || dueMileage.getText().toString().length()==0) {
                    Toast.makeText(getApplicationContext(), "Text or Phone Field should not Empty ", Toast.LENGTH_SHORT).show();
                } else {
                    String str = textToPrint.getText().toString();
                    String phon = phoneNumber.getText().toString();
                    String pMileage = presentMileage.getText().toString();
                    String pDueMileage = dueMileage.getText().toString();
                    String nameAndPhone = str + "\n" + phon;


                    String complextData = "Date: " + currentDate + "\n" + "Name: " + str + "\n" + "Phone: " + phon + "\n" + "Present Mileage: " + pMileage + "\n" + "Due Mileage: " +  pDueMileage;
                    Log.e("My Test Log", "Name And Phone " + complextData);



                    if (DataUtils.str2Hexstr(headerText).length() / 2 >= 2000) {
                        Toast.makeText(getApplicationContext(),
                                "Current number of bytes:"
                                        + DataUtils.str2Hexstr(headerText).length() / 2
                                        + ",Can not be more than 1999",
                                Toast.LENGTH_SHORT).show();
                    } else {

                        //printing header
                        //api.setAlighType(centerAlignment);
                        //api.setKGCU(false, false, true, false);
                        api.printPaper(headerText);

                      //  api.setAlighType(leftAlignment);




                        //printing invoice number
                        //api.setAlighType(leftAlignment);
                        //api.setKGCU(false, false, false, false);
                        //api.setFont(6);


                       // api.printPaper("Invoice No:" + invoiceNumber);

                       if (desctription1.getText().length()!=0 && price1.getText().length()!=0){
                           String firstDescricption = desctription1.getText().toString();
                           String firstPrice = price1.getText().toString();
                           //converting String Price to number
                           int firstPriceInt = Integer.parseInt(firstPrice);




                           String longString = "\n" + "Product Name: "+ selectedProduct + "\n" + "Sr.# " + sr+1 + "\n" + "Description: " + firstDescricption + "\n" + "Quantity: 1 " + "\n" + "Price: " + firstPrice + "\n" + strightLinet + "\n" + "Total Price: " + "Rs." + firstPrice;




                           if (DataUtils.str2Hexstr(complextData).length() / 2 >= 2000 && DataUtils.str2Hexstr(longString).length() / 2 >= 2000) {
                               Toast.makeText(getApplicationContext(),
                                       "Current number of bytes:"
                                               + DataUtils.str2Hexstr(headerText).length() / 2
                                               + ",Can not be more than 1999",
                                       Toast.LENGTH_SHORT).show();
                           }else {



                               api.printPaper("Invoice No: " + invoiceNumber + "\n\n" + " " + "\n\n" + complextData + "\n\n" + longString + "\n" + customerSignature + "\n" + " " + "\n" + " " + "\n" + " ");

                               //printing flash Image


                               //api.printFlashImage(R.raw.example);
                              // api.printFlashImage(R.raw.guard_demo_image);

                           }


                       }



                        /*api.printPaper("Sr.# " + sr + 1 + "\n" + "Description: " + desctription1.getText().toString() + "\n" + " Quantity 1 " + "\n" + "Price: " + price1.getText().toString() + "\n" +
                                "___________" + "\n" + "Total Price :" + price1.getText().toString());
*/

                        //Customer signature
                        //api.doPrintPaper();
                        //api.printPaper(customerSignature);
                        Toast.makeText(getApplicationContext(), nameAndPhone.length() + "", Toast.LENGTH_SHORT)
                                .show();
                    }
                }


            }
        });
    }

    private void showProgressDialog(int resId) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(resId));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void cancleProgressDialog() {
        if (null != this.progressDialog && this.progressDialog.isShowing()) {
            this.progressDialog.cancel();
            this.progressDialog = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SerialPortManager.getInstance().openSerialPort();
    }

    @Override
    protected void onDestroy() {
        SerialPortManager.getInstance().closeSerialPort(2);
        super.onDestroy();
    }

    public void getCurrentDate(){
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        currentDate = df.format(c.getTime());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SerialPortManager.getInstance().closeSerialPort(2);
        Toast.makeText(getApplicationContext(), "Printer Command Off", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (parent.getId()){
            case R.id.product_list:
                String productName = (String)parent.getItemAtPosition(position);
                selectedProduct = productName;
                break;

            case R.id.itemNumber:

                Log.v("item", (String) parent.getItemAtPosition(position));
                String item = (String)parent.getItemAtPosition(position);



                if (item.equals("0")){
                    Toast.makeText(getApplicationContext(), "Item should be greater then 0", Toast.LENGTH_SHORT).show();
                    Log.d("Test Item", "Test Item Seclected 1");

                }else if (item.equals("1")){
                    desctription1.setVisibility(View.VISIBLE);
                    price1.setVisibility(View.VISIBLE);

                    //gon Visibility
                    desctription2.setVisibility(View.GONE);
                    price2.setVisibility(View.GONE);

                    desctription3.setVisibility(View.GONE);
                    price3.setVisibility(View.GONE);

                    desctription4.setVisibility(View.GONE);
                    price4.setVisibility(View.GONE);

                    desctription5.setVisibility(View.GONE);
                    price5.setVisibility(View.GONE);

                    desctription6.setVisibility(View.GONE);
                    price6.setVisibility(View.GONE);

                    desctription7.setVisibility(View.GONE);
                    price7.setVisibility(View.GONE);

                    desctription8.setVisibility(View.GONE);
                    price8.setVisibility(View.GONE);

                    desctription9.setVisibility(View.GONE);
                    price9.setVisibility(View.GONE);

                    if (desctription1.getText().length()==0 || price1.getText().length()==0){
                        Toast.makeText(getApplicationContext(), "Enter Description or Price", Toast.LENGTH_SHORT).show();
                    }else {
                        descriptionText_1 = desctription1.getText().toString();
                        String price11 = price1.getText().toString();
                        priceText_1 = Integer.parseInt(price11);
                        tPrice1 = priceText_1;
                        sr = sr+1;
                    }
                }/*else if (item.equals("2")){

            desctription1.setVisibility(View.VISIBLE);
            price1.setVisibility(View.VISIBLE);

            desctription2.setVisibility(View.VISIBLE);
            price2.setVisibility(View.VISIBLE);

            //gon Visibility
            desctription3.setVisibility(View.GONE);
            price3.setVisibility(View.GONE);

            desctription4.setVisibility(View.GONE);
            price4.setVisibility(View.GONE);

            desctription5.setVisibility(View.GONE);
            price5.setVisibility(View.GONE);

            desctription6.setVisibility(View.GONE);
            price6.setVisibility(View.GONE);

            desctription7.setVisibility(View.GONE);
            price7.setVisibility(View.GONE);

            desctription8.setVisibility(View.GONE);
            price8.setVisibility(View.GONE);

            desctription9.setVisibility(View.GONE);
            price9.setVisibility(View.GONE);


            if (desctription2.getText().length()==0 || price2.getText().length()==0){
                Toast.makeText(getApplicationContext(), "Enter Description or Price", Toast.LENGTH_SHORT).show();
            }else {
                descriptionText_2 = desctription2.getText().toString();
                String price22 = price2.getText().toString();
                priceText_2 = Integer.parseInt(price22);
                tPrice2 = tPrice1 + priceText_2;
            }


        }else if (item.equals("3")){

            desctription1.setVisibility(View.VISIBLE);
            price1.setVisibility(View.VISIBLE);

            desctription2.setVisibility(View.VISIBLE);
            price2.setVisibility(View.VISIBLE);

            desctription3.setVisibility(View.VISIBLE);
            price3.setVisibility(View.VISIBLE);

            //gon Visibility

            desctription4.setVisibility(View.GONE);
            price4.setVisibility(View.GONE);

            desctription5.setVisibility(View.GONE);
            price5.setVisibility(View.GONE);

            desctription6.setVisibility(View.GONE);
            price6.setVisibility(View.GONE);

            desctription7.setVisibility(View.GONE);
            price7.setVisibility(View.GONE);

            desctription8.setVisibility(View.GONE);
            price8.setVisibility(View.GONE);

            desctription9.setVisibility(View.GONE);
            price9.setVisibility(View.GONE);

            if (desctription3.getText().length()==0 || price3.getText().length()==0){
                Toast.makeText(getApplicationContext(), "Enter Description or Price", Toast.LENGTH_SHORT).show();
            }else {
                descriptionText_3 = desctription3.getText().toString();
                String price33 = price3.getText().toString();
                priceText_3 = Integer.parseInt(price33);
                tPric3 = tPrice1+tPrice2+priceText_3;
            }


        }else if (item.equals("4")){

            desctription1.setVisibility(View.VISIBLE);
            price1.setVisibility(View.VISIBLE);

            desctription2.setVisibility(View.VISIBLE);
            price2.setVisibility(View.VISIBLE);

            desctription3.setVisibility(View.VISIBLE);
            price3.setVisibility(View.VISIBLE);

            desctription4.setVisibility(View.VISIBLE);
            price4.setVisibility(View.VISIBLE);


            desctription5.setVisibility(View.GONE);
            price5.setVisibility(View.GONE);

            desctription6.setVisibility(View.GONE);
            price6.setVisibility(View.GONE);

            desctription7.setVisibility(View.GONE);
            price7.setVisibility(View.GONE);

            desctription8.setVisibility(View.GONE);
            price8.setVisibility(View.GONE);

            desctription9.setVisibility(View.GONE);
            price9.setVisibility(View.GONE);

            if (desctription4.getText().length()==0 || price4.getText().length()==0){
                Toast.makeText(getApplicationContext(), "Enter Description or Price", Toast.LENGTH_SHORT).show();
            }else {
                descriptionText_4 = desctription4.getText().toString();
                String price44 = price4.getText().toString();
                priceText_4 = Integer.parseInt(price44);

                tPrice4 = tPrice1+tPrice2+tPric3+priceText_4;
            }

        }else if (item.equals("5")){

            desctription1.setVisibility(View.VISIBLE);
            price1.setVisibility(View.VISIBLE);

            desctription2.setVisibility(View.VISIBLE);
            price2.setVisibility(View.VISIBLE);

            desctription3.setVisibility(View.VISIBLE);
            price3.setVisibility(View.VISIBLE);

            desctription4.setVisibility(View.VISIBLE);
            price4.setVisibility(View.VISIBLE);


            desctription5.setVisibility(View.VISIBLE);
            price5.setVisibility(View.VISIBLE);


            desctription6.setVisibility(View.GONE);
            price6.setVisibility(View.GONE);

            desctription7.setVisibility(View.GONE);
            price7.setVisibility(View.GONE);

            desctription8.setVisibility(View.GONE);
            price8.setVisibility(View.GONE);

            desctription9.setVisibility(View.GONE);
            price9.setVisibility(View.GONE);

            if (desctription5.getText().length()==0 || price5.getText().length()==0){
                Toast.makeText(getApplicationContext(), "Enter Description or Price", Toast.LENGTH_SHORT).show();
            }else {
                descriptionText_5 = desctription5.getText().toString();
                String price55 = price5.getText().toString();
                priceText_5 = Integer.parseInt(price55);

                tPrice5 = tPrice1+tPrice2+tPric3+tPrice4+priceText_5;
            }


        }else if (item.equals("6")){

            desctription1.setVisibility(View.VISIBLE);
            price1.setVisibility(View.VISIBLE);

            desctription2.setVisibility(View.VISIBLE);
            price2.setVisibility(View.VISIBLE);

            desctription3.setVisibility(View.VISIBLE);
            price3.setVisibility(View.VISIBLE);

            desctription4.setVisibility(View.VISIBLE);
            price4.setVisibility(View.VISIBLE);


            desctription5.setVisibility(View.VISIBLE);
            price5.setVisibility(View.VISIBLE);


            desctription6.setVisibility(View.VISIBLE);
            price6.setVisibility(View.VISIBLE);

            desctription7.setVisibility(View.GONE);
            price7.setVisibility(View.GONE);

            desctription8.setVisibility(View.GONE);
            price8.setVisibility(View.GONE);

            desctription9.setVisibility(View.GONE);
            price9.setVisibility(View.GONE);


            if (desctription6.getText().length()==0 || price6.getText().length()==0){
                Toast.makeText(getApplicationContext(), "Enter Description or Price", Toast.LENGTH_SHORT).show();
            }else {
                descriptionText_6 = desctription6.getText().toString();
                String price66 = price6.getText().toString();
                priceText_6 = Integer.parseInt(price66);

                tPrice6 = tPrice1+tPrice2+tPric3+tPrice4+tPrice5+priceText_6;
            }

        }else if (item.equals("7")){

            desctription1.setVisibility(View.VISIBLE);
            price1.setVisibility(View.VISIBLE);

            desctription2.setVisibility(View.VISIBLE);
            price2.setVisibility(View.VISIBLE);

            desctription3.setVisibility(View.VISIBLE);
            price3.setVisibility(View.VISIBLE);

            desctription4.setVisibility(View.VISIBLE);
            price4.setVisibility(View.VISIBLE);


            desctription5.setVisibility(View.VISIBLE);
            price5.setVisibility(View.VISIBLE);

            desctription6.setVisibility(View.VISIBLE);
            price6.setVisibility(View.VISIBLE);


            desctription7.setVisibility(View.VISIBLE);
            price7.setVisibility(View.VISIBLE);

            desctription8.setVisibility(View.GONE);
            price8.setVisibility(View.GONE);

            desctription9.setVisibility(View.GONE);
            price9.setVisibility(View.GONE);


            if (desctription7.getText().length()==0 || price7.getText().length()==0){
                Toast.makeText(getApplicationContext(), "Enter Description or Price", Toast.LENGTH_SHORT).show();
            }else {
                descriptionText_7 = desctription7.getText().toString();
                String price77 = price7.getText().toString();
                priceText_7 = Integer.parseInt(price77);

                tPrice7 = tPrice1+tPrice2+tPric3+tPrice4+tPrice5+tPrice6+priceText_7;

            }

        }else if (item.equals("8")){

            desctription1.setVisibility(View.VISIBLE);
            price1.setVisibility(View.VISIBLE);

            desctription2.setVisibility(View.VISIBLE);
            price2.setVisibility(View.VISIBLE);

            desctription3.setVisibility(View.VISIBLE);
            price3.setVisibility(View.VISIBLE);

            desctription4.setVisibility(View.VISIBLE);
            price4.setVisibility(View.VISIBLE);


            desctription5.setVisibility(View.VISIBLE);
            price5.setVisibility(View.VISIBLE);

            desctription6.setVisibility(View.VISIBLE);
            price6.setVisibility(View.VISIBLE);


            desctription7.setVisibility(View.VISIBLE);
            price7.setVisibility(View.VISIBLE);


            desctription8.setVisibility(View.VISIBLE);
            price8.setVisibility(View.VISIBLE);

            desctription9.setVisibility(View.GONE);
            price9.setVisibility(View.GONE);


            if (desctription8.getText().length()==0 || price8.getText().length()==0){
                Toast.makeText(getApplicationContext(), "Enter Description or Price", Toast.LENGTH_SHORT).show();
            }else {
                descriptionText_8 = desctription8.getText().toString();
                String price88 = price8.getText().toString();
                priceText_8 = Integer.parseInt(price88);

                tPrice8 = tPrice1+tPrice2+tPric3+tPrice4+tPrice5+tPrice6+tPrice7+priceText_8;
            }

        }else if (item.equals("9")){


            desctription1.setVisibility(View.VISIBLE);
            price1.setVisibility(View.VISIBLE);

            desctription2.setVisibility(View.VISIBLE);
            price2.setVisibility(View.VISIBLE);

            desctription3.setVisibility(View.VISIBLE);
            price3.setVisibility(View.VISIBLE);

            desctription4.setVisibility(View.VISIBLE);
            price4.setVisibility(View.VISIBLE);


            desctription5.setVisibility(View.VISIBLE);
            price5.setVisibility(View.VISIBLE);

            desctription6.setVisibility(View.VISIBLE);
            price6.setVisibility(View.VISIBLE);


            desctription7.setVisibility(View.VISIBLE);
            price7.setVisibility(View.VISIBLE);

            desctription8.setVisibility(View.VISIBLE);
            price8.setVisibility(View.VISIBLE);

            desctription9.setVisibility(View.VISIBLE);
            price9.setVisibility(View.VISIBLE);

            if (desctription9.getText().length()==0 || price9.getText().length()==0){
                Toast.makeText(getApplicationContext(), "Enter Description or Price", Toast.LENGTH_SHORT).show();
            }else {
                descriptionText_9 = desctription9.getText().toString();
                String price99 = price9.getText().toString();
                priceText_9 = Integer.parseInt(price99);

                tPrice9 = tPrice1+tPrice2+tPric3+tPrice4+tPrice5+tPrice6+tPrice7+tPrice9+priceText_9;
            }

        }
*/

        }



    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

