package com.upcstudios.tania.braccioapp3;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class LinkActivity extends AppCompatActivity {

    // Variable definition
    GlobalClasses globalClasses;    // Variable that contains global variables
    ListView listDevices;
    Button buttonSearchPaired;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // In order to use global variables, is necessary to get a reference to the application class
        globalClasses = (GlobalClasses) getApplication();

        setContentView(R.layout.activity_link);

        // Link of local variables to the objects in LinkActivity screen
        listDevices = findViewById(R.id.listDevices);
        buttonSearchPaired = findViewById(R.id.buttonSearchPaired);

        // When this button is pressed, the method GetPairedDevices() of the bluetooth class is called
        // and the data of the list is refreshed. Also, it checks if the Bluetooth adapter is enabled,
        // and if it is not, it will ask for enabling it.
        buttonSearchPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listDevices.setAdapter(globalClasses.MyBluetooth.GetPairedDevices());
            }
        });

        listDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);
                String name = info.substring(0, info.length() - 18);

                // When an item of the list is selected, the name of the item is verified in order
                // to work only with de HC-06 module. If it's not the case, a message ask for
                // choosing HC-06. If it's correct, the address of the bluetooth is set into the
                // bluetooth class, which will try to establish the connection.
                if (name.equals("HC-06")){
                    try {
                        globalClasses.MyBluetooth.SetAddress(address);

                        // If the connection is established, a message of connected will be shown
                        // and the MainActivity will be opened.
                        if (globalClasses.MyBluetooth.BTConnected) {
                            Toast.makeText(LinkActivity.this, "Connected.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LinkActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    } catch (IOException e) {
                    }
                }
                else Toast.makeText(LinkActivity.this, "Select the robot device.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Checks if the bluetooth adapter of the mobile is turned on.
        // If it's not, the LinkApplication will ask for enabling it.
        if (!globalClasses.MyBluetooth.GetBTAdapter().isEnabled())
            globalClasses.MyBluetooth.VerifyBT();

        // If it's enabled, the list of paired devices will be loaded.
        else {
            listDevices.setAdapter(globalClasses.MyBluetooth.GetPairedDevices());
        }
    }
}
