package com.example.csv_usb;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectUsb();
//        sendFileOverWifiFTP();
    }

    private void sendFileOverWifiFTP() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        try {
            ftpSend();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void ftpSend() throws Exception{

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        FTPClient ftpClient = new FTPClient();
        FileInputStream fileInputStream = null;
        final String host = "192.168.1.104";
        final int port = 21;

        final String user = "user1";
        final String pass = "123456";

        try{

            ftpClient.connect(host, port);

            if(ftpClient.login(user, pass) == false){
                System.exit(2);
            }

            File myFile = new File(Environment.getExternalStorageDirectory(), "Pictures/Reddit/9465a9a.jpg");
            fileInputStream = new FileInputStream(myFile);
            ftpClient.storeFile("9465a9a.jpg", fileInputStream);
            fileInputStream.close();

        }catch(Exception e){
            e.printStackTrace();
        }finally {
            ftpClient.disconnect();
            fileInputStream.close();
        }

    }

    /** Send files over USB **/
    // https://stackoverflow.com/questions/21808223/send-data-through-usb-from-android-app-to-pc/24290009

    UsbInterface usbInterface;
    UsbEndpoint usbEndpointIN, usbEndpointOUT;
    UsbDeviceConnection usbDeviceConnection;
    UsbDevice deviceFound = null;
//    USB USB;

    ArrayList<String> listInterface;
    ArrayList<UsbInterface> listUsbInterface;
    ArrayList<String> listEndPoint;
    ArrayList<UsbEndpoint> listUsbEndpoint;

    private static final int targetVendorID = 8260;
    private static final int ProductID = 1000;

    private boolean connectUsb() {
        boolean result = false;
        switch(checkDeviceInfo())
        {
            case ProductID:
                result = StartUSB();
                break;
        }
        return result;
    }

    private int checkDeviceInfo() {

        deviceFound = null;

        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);

        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            int vendorId = device.getVendorId();

            if(vendorId == targetVendorID) {
                deviceFound = device;

                int productId = device.getProductId();
                switch(productId) {
                    case ProductID:
                        GetInterface(deviceFound);
                        GetEndpoint(deviceFound);
                        return ProductID;

                    default:
                        Toast.makeText(this, "Error unknown device", Toast.LENGTH_LONG).show();
                        break;
                }
                return -1;
            }
        }
        return -1;
    }

    private void GetInterface(UsbDevice d) {
        listInterface = new ArrayList<String>();
        listUsbInterface = new ArrayList<UsbInterface>();
        for(int i=0; i<d.getInterfaceCount(); i++){
            UsbInterface usbif = d.getInterface(i);
            listInterface.add(usbif.toString());
            listUsbInterface.add(usbif);
        }

        if(d.getInterfaceCount() > 0)
        {
            usbInterface = listUsbInterface.get(1);
        }
        else usbInterface = null;
    }

    private void GetEndpoint(UsbDevice d) {
        int EndpointCount = usbInterface.getEndpointCount();
        listEndPoint = new ArrayList<String>();
        listUsbEndpoint = new ArrayList<UsbEndpoint>();

        for(int i=0; i<usbInterface.getEndpointCount(); i++) {
            UsbEndpoint usbEP = usbInterface.getEndpoint(i);
            listEndPoint.add(usbEP.toString());
            listUsbEndpoint.add(usbEP);
        }

        // deixar fixo para TxBlock USB
        if(EndpointCount > 0) {
            usbEndpointIN = usbInterface.getEndpoint(0);
            usbEndpointOUT = usbInterface.getEndpoint(1);
        }
        else {
            usbEndpointIN = null;
            usbEndpointOUT = null;
        }
    }

    private boolean StartUSB() {
        boolean result = false;
        UsbDevice deviceToRead = deviceFound;
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        Boolean permitToRead = manager.hasPermission(deviceToRead);

        if(permitToRead) {
            result = OpenDevice(deviceToRead);
        }else {
            Toast.makeText(this,
                    "Error, No permission" + permitToRead,
                    Toast.LENGTH_LONG).show();
        }

        return result;
    }

    private boolean OpenDevice(UsbDevice device){

        boolean forceClaim = true;

        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        usbDeviceConnection = manager.openDevice(device);

        if(usbDeviceConnection != null){
            usbDeviceConnection.claimInterface(usbInterface, forceClaim);
            return true;
        }else{
            Toast.makeText(this,
                    "Error: No open device",
                    Toast.LENGTH_LONG).show();
        }

        return false;
    }

}