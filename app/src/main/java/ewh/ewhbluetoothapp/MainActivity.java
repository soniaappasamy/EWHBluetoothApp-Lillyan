package ewh.ewhbluetoothapp;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.UUID;

import java.util.Set;

/**
 * created based on http://stackoverflow.com/questions/10795424/how-to-get-the-bluetooth-devices-as-a-list tutorial
 */

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    //For bluetooth device list
    ArrayList<String> deviceList = new ArrayList<String>();

    ArrayList<String> frontEndListItems=new ArrayList<String>();
    ArrayAdapter<String> frontEndAdapter;

    private StringBuilder dataString = new StringBuilder();
    private BluetoothAdapter adapter;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestWindowFeature(getWindow().FEATURE_ACTION_BAR);


        listView = (ListView) findViewById(R.id.listView);

        frontEndListItems.add("Well Monitor");
        frontEndListItems.add("Other Device 1");
        frontEndListItems.add("Other Device 2");

        frontEndAdapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                frontEndListItems);

        listView.setAdapter(frontEndAdapter);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String itemSelected = frontEndAdapter.getItem(position);
                System.out.println("Item selected: " + itemSelected);

                if (position == 0) {
                    Intent intent = new Intent(MainActivity.this, MetricListActivity.class);
                    startActivity(intent);
                }
            }
        });
        //adapter = BluetoothAdapter.getDefaultAdapter(); UNCOMMENT THIS WHEN ACTUALLY USING
        //adapter.startDiscovery(); UNCOMMENT THIS WHEN ACTUALLY USING

//      Facilitate communication between components
        //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND); UNCOMMENT THIS WHEN ACTUALLY USING
        //registerReceiver(mReceiver, filter);   UNCOMMENT THIS WHEN ACTUALLY USING                                 // mReceiver will be called with any broadcast Intent that matches filter
                                                                                // (bluetooth device found)
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

//  Component that responds to system-wide broadcast announcements (bluetooth device found)
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {                 // BR only valid for duration of onReceive call; modifies current result
            String action = intent.getAction();                                 // Describe general way info in intent should be handled
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {                  // Broadcast Action: Remote device discovered
                BluetoothDevice device = intent                                 // contains the BluetoothDevice that the intent applies to
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                deviceList.add(device.getName() + "\n" + device.getAddress());
                Log.i("BT", device.getName() + "\n" + device.getAddress());     // Report device found success; posts information to the log
                listView.setAdapter(new ArrayAdapter<String>(context,           // Sets the data behind listView
                        android.R.layout.simple_list_item_1, deviceList));      // android.R.layout.simple_list_item_1: built-in XML layout document


                //Opening a bluetooth socket
                BluetoothSocket socket = null;
                try {
                    socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                } catch (IOException e) {
                    System.out.println("UNABLE TO CREATE BLUETOOTH SOCKET FROM DEVICE: " + device.getName());
                    e.printStackTrace();
                }
                try {
                    socket.connect();

                    InputStream in = null;
                    OutputStream out = null;

                    try{
                        in = socket.getInputStream();
                        out = socket.getOutputStream();

                        //Trying to use the input stream
                        byte[] buffer = new byte[1024];
                        int bytes;

                        while(true)
                        {
                            /*
                            Note to self: current state is that the message is being recieved succesfully from
                            the server socket, but is not printing as a string message

                            May be related to the MESSAGE_READ parameter

                            Next todo: figure out how to convert the message recieved into a readable string
                             */
                            try {
                                // Read from the InputStream
                                bytes = in.read(buffer);
                                // Send the obtained bytes to the UI activity
                                Handler mHandler = new Handler();
                                int MESSAGE_READ = 1; //NEED TO FIGURE OUT WHAT THIS VALUE SHOULD BE
                                Message newMessage = mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer);

                                /*
                                Bundle temp = newMessage.getData();
                                StringBuilder sb = new StringBuilder();
                                Set<String> keySet = temp.keySet();
                                for (final String key: keySet) {
                                    sb.append('\"');
                                    sb.append(key);
                                    sb.append("\"=\"");
                                    sb.append(temp.get(key));
                                    sb.append("\", ");
                                }
                                System.out.println("DATA RECIEVEDDDDDDD" + sb.toString());
**/
                               // System.out.println("DATA RECIEVEDDDD" + newMessage.obj);

                                byte[] tempArray = (byte[]) newMessage.obj;

                              //  System.out.println("temp array: ");
                                String realMessage = " ";

                                for(int i = 0; i < tempArray.length; i++)
                                {
                                    //if(tempArray[i]==0)
                                      //  break;
                                    byte[] tempArray3 = new byte[]{tempArray[i]};
                                    String temp = new String(tempArray3, "UTF8");
                                    realMessage = realMessage + temp;
                                }

                                System.out.println(realMessage);

                                //byte[] tempArray2 = new byte[]{tempArray[1]};

                               // String test1 = bytesToStringUTFNIO(tempArray);
                             //   System.out.println("TESTTTT" + test1);
                            //    String test = new String(tempArray2, "UTF8");
                             //   System.out.println("VALUE RECIEVED" + test);
/*
                                String readMessage = (String) newMessage.obj;                                                                // msg.arg1 = bytes from connect thread
                                dataString.append(readMessage);                             //keep appending to string until ~
                                int endOfLineIndex = dataString.indexOf("~");               // determine the end-of-line
                                if (endOfLineIndex > 0) {                                   // make sure there data before ~
                                    if (dataString.charAt(0) == '#')                        //if it starts with # we know it is what we are looking for
                                    {
                                        String sensor = dataString.substring(1, 5);         //get sensor value from string between indices 1-5
                                    }
                                    dataString.delete(0, dataString.length());               //clear all string data
                                }
                                **/

                            } catch (IOException e) {
                                break;
                            }
                        }

                        socket.close();

                    } catch (IOException e) {
                        System.out.println("UNABLE TO CREATE STREAMS FROM SOCKET");
                    }


                } catch (IOException e) {
                    System.out.println("UNABLE TO CONNECT BLUETOOTH SOCKET FROM DEVICE: " + device.getName());
                    e.printStackTrace();
                }

            }
        }
    };

    public void deviceSelected()
    {
        System.out.println("DEVICE SELECTED!");
    }

    public static String bytesToStringUTFNIO(byte[] bytes) {
        CharBuffer cBuffer = ByteBuffer.wrap(bytes).asCharBuffer();
        return cBuffer.toString();
    }
}