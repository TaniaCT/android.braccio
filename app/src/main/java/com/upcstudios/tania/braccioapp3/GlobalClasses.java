package com.upcstudios.tania.braccioapp3;

import android.app.Application;

import java.util.ArrayList;
import java.util.Arrays;

public class GlobalClasses extends Application {

    public BluetoothClass MyBluetooth;
    public ArrayList<String> CommandArray = new ArrayList<>();
    public String Code = "";
    //public boolean arduinoFree = true;  //TODO: borrar¿?
    public  int maxPositions = 20;
    public int positions[] = new int[6*maxPositions];


    @Override
    public void onCreate() {
        super.onCreate();
        MyBluetooth = new BluetoothClass(getApplicationContext());

        Arrays.fill(positions,-1);

    }
}
