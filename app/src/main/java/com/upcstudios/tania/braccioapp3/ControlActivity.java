package com.upcstudios.tania.braccioapp3;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ControlActivity extends AppCompatActivity {
    private boolean close;
    GlobalClasses globalClasses;
    TextView receivedText;
    Button sendSmthButton, joggingButton, programButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        globalClasses = (GlobalClasses)getApplication();
        setContentView(R.layout.activity_control);

        globalClasses.MyBluetooth.VerifyBT();

        receivedText = findViewById(R.id.receivedText);
        sendSmthButton = findViewById(R.id.sendSmthButton);
        joggingButton = findViewById(R.id.joggingButton);
        programButton = findViewById(R.id.programButton);

        sendSmthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                globalClasses.MyBluetooth.writeMessage("Hello");
            }
        });

        joggingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (globalClasses.MyBluetooth.Messages.size() > 0) {
                    receivedText.setText("Dato: " + globalClasses.MyBluetooth.Messages.get(globalClasses.MyBluetooth.Messages.size() - 1));
                    globalClasses.MyBluetooth.Messages.remove(globalClasses.MyBluetooth.Messages.size() - 1);
                }
                else {
                    Toast.makeText(ControlActivity.this, "No hay mensajes", Toast.LENGTH_SHORT).show();
                    receivedText.setText("Dato: ");
                }
            }
        });

        programButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close = false;
                Intent intent = new Intent(ControlActivity.this, JoggingActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        close = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (close) System.exit(0);
    }
}
