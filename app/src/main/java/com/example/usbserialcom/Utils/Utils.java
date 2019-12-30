package com.example.usbserialcom.Utils;

import android.content.Context;

import com.example.usbserialcom.helper.UsbMessageListner;

public class Utils {

    private static UsbMessageListner listner;
    private static Utils context=new Utils();

    public static Utils getContext(){
        return context;
    }

    public UsbMessageListner getListner() {
        return listner;
    }

    public void setListner(UsbMessageListner mlistner) {
        listner = mlistner;
    }
}
