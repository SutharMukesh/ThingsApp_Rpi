package com.example.mukesh.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
public String TAG="Main";
    private static final String GPIO_NAME ="BCM4";

    private Gpio mGpio;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("light");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        /*PeripheralManagerService manager = new PeripheralManagerService();
        List<String> portList = manager.getGpioList();
        if (portList.isEmpty()) {
            Log.i(TAG, "No GPIO port available on this device.");
        } else {
            Log.i(TAG, "List of available ports: " + portList);

        }*/


        // Attempt to access the GPIO
        try {
            PeripheralManagerService manager = new PeripheralManagerService();
            mGpio = manager.openGpio(GPIO_NAME);
        } catch (IOException e) {
            Log.w(TAG, "Unable to access GPIO", e);
        }





        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Boolean value = dataSnapshot.getValue(Boolean.class);
                Log.d(TAG, "Value is: " + value);
                if(value==true)
                {
                    Log.i(TAG, "value is true");
                    try {
                        configureOutput(mGpio,true);


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                else{
                    Log.i(TAG, "value is false");
                    try {
                        configureOutput(mGpio,false);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }
    public void configureOutput(Gpio gpio,Boolean b) throws IOException {
        // Initialize the pin as a high output
        gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
        // Low voltage is considered active
        gpio.setActiveType(Gpio.ACTIVE_LOW);

        // Toggle the value to be LOW
        gpio.setValue(b);
        Log.d(TAG,"Switched to "+b);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mGpio != null) {
            try {
                mGpio.close();
                mGpio = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close GPIO", e);
            }
        }
    }
}
