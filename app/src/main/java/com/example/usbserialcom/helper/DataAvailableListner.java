package com.example.usbserialcom.helper;

public interface DataAvailableListner {
    void onDataAvailable(String data,int port);
    void timeOutError();
}
