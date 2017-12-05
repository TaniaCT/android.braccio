package com.upcstudios.tania.braccioapp3;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    GlobalClasses globalClasses;
    ListView listDevices;
    Button searchPairedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        globalClasses = (GlobalClasses) getApplication();
        setContentView(R.layout.activity_main);

        listDevices = findViewById(R.id.listDevices);
        searchPairedButton = findViewById(R.id.searchPairedButton);

        searchPairedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listDevices.setAdapter(globalClasses.MyBluetooth.GetPairedDevices());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!globalClasses.MyBluetooth.GetBTAdapter().isEnabled())
            globalClasses.MyBluetooth.VerifyBT();
        else {
            listDevices.setAdapter(globalClasses.MyBluetooth.GetPairedDevices());

            listDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String info = ((TextView) view).getText().toString();
                    String address = info.substring(info.length() - 17);
                    try {
                        globalClasses.MyBluetooth.SetAddress(address);

                        if (globalClasses.MyBluetooth.BTConnected) {
                            Toast.makeText(MainActivity.this, "Connected.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, ControlActivity.class);
                            startActivity(intent);
                        }
                    } catch (IOException e) {
                    }
                }
            });
        }
    }
}
