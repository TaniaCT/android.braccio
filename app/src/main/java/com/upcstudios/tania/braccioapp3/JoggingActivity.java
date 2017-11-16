package com.upcstudios.tania.braccioapp3;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class JoggingActivity extends AppCompatActivity {
    GlobalClasses globalClasses;
    Button sendButton2, dataButton;
    TextView receivedText2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        globalClasses = (GlobalClasses)getApplication();
        setContentView(R.layout.activity_jogging);

        sendButton2 = findViewById(R.id.sendButton2);
        dataButton = findViewById(R.id.dataButton);
        receivedText2 = findViewById(R.id.receivedText2);

        sendButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                globalClasses.MyBluetooth.writeMessage("Nice");
            }
        });

        dataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (globalClasses.MyBluetooth.Messages.size() > 0) {
                    receivedText2.setText("Dato: " + globalClasses.MyBluetooth.Messages.get(globalClasses.MyBluetooth.Messages.size() - 1));
                    globalClasses.MyBluetooth.Messages.remove(globalClasses.MyBluetooth.Messages.size() - 1);
                }
                else {
                    Toast.makeText(JoggingActivity.this, "No hay mensajes", Toast.LENGTH_SHORT).show();
                    receivedText2.setText("Dato: ");
                }
            }
        });
    }
}
