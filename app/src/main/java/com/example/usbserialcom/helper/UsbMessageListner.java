package com.example.usbserialcom.helper;

public interface UsbMessageListner {
    void sendData(int port, String message, DataAvailableListner dataAvailableListner);
}
