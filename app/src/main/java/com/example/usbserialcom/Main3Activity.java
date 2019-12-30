package com.example.usbserialcom;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.usbserialcom.Utils.Utils;
import com.example.usbserialcom.helper.DataAvailableListner;
import com.example.usbserialcom.helper.UsbMessageListner;

public class Main3Activity extends AppCompatActivity {

    private Utils util;
    private UsbMessageListner messageListener;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        util = Utils.getContext();
        messageListener = util.getListner();
        tv=findViewById(R.id.tv);
    }

    public void send1(View view) {
        if (messageListener!=null){
            messageListener.sendData(1, "HEALTH\n", new DataAvailableListner() {
                @Override
                public void onDataAvailable(String data,int port) {
                    tv.setText(data);
                }

                @Override
                public void timeOutError() {

                }
            });
        }
    }

    public void send2(View view) {
        if (messageListener!=null){
            messageListener.sendData(2, "HEALTH\n", new DataAvailableListner() {
                @Override
                public void onDataAvailable(String data,int port) {

                }

                @Override
                public void timeOutError() {

                }
            });
        }
    }
}
