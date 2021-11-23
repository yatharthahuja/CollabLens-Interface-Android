package com.example.collablensinterface;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice = null;

    final byte delimiter = 33;
    int readBufferPosition = 0;

    private final Mat imageMatrix = new Mat();

    public void sendBtMsg(String msg){

        UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee"); //Standard SerialPortService ID
        try {
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            if (!mmSocket.isConnected()){
                mmSocket.connect();
            }
            //msg += "\n";
            OutputStream mmOutputStream = mmSocket.getOutputStream();
            mmOutputStream.write(msg.getBytes());
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static String TAG = "MainActivity";
    static {
        if (OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV Loaded Successfully");
        }
        else{
            Log.d(TAG, "OpenCV Not Loaded");
        }
    }


    @Override
    protected void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Handler handler = new Handler();

        Switch record_control = (Switch)findViewById(R.id.record_control);
        Switch stream_control = (Switch)findViewById(R.id.stream_control);
        Button upload = (Button)findViewById(R.id.upload);
        Button delete = (Button)findViewById(R.id.delete);
        TextView instream = (TextView) findViewById(R.id.instream);
        TextView instream_raw = (TextView) findViewById(R.id.instream_raw);
        ImageView input_image = (ImageView) findViewById(R.id.image_stream);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        final int[] newFrameLength = new int[1];
        final byte[][] inputFrame = {null};
        final int maxPacketSize = 300;

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available!", Toast.LENGTH_SHORT).show();
            finish(); //automatic close app if Bluetooth service is not available!
        }

         final class messageThread implements Runnable {

            private String btMsg;

            public messageThread(String msg) {
                btMsg = msg;
            }

            public void run(){

                sendBtMsg(btMsg);
                while(!Thread.currentThread().isInterrupted()){

                    int bytesAvailable;
                    boolean workDone = false;

                    try {

                        final InputStream mmInputStream;
                        mmInputStream = mmSocket.getInputStream();
                        bytesAvailable = mmInputStream.available();

                        if(bytesAvailable > 0)
                        {

                            byte[] inputBytes = new byte[bytesAvailable];
                            Log.e("RPi receive bt","bytes available");
                            mmInputStream.read(inputBytes, 0, bytesAvailable);
                            Log.e("TAG", Arrays.toString(inputBytes));
                            byte[] packetBytes = new byte[inputBytes.length];
                            for (int i=0; i < inputBytes.length; ++i){
                                packetBytes[i] = (byte)(((int)inputBytes[i]+Integer.valueOf(0x80)));
                            }
                            Log.e("TAG", Arrays.toString(packetBytes));

                            for(int i=0;i<bytesAvailable;i++) {
                                byte b = inputBytes[i];
                                if (b == delimiter) {

                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(inputBytes, 0, encodedBytes, 0, encodedBytes.length);
                                    //converting obtained bytes stream to respective coherent data:
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    //The variable data now contains our full command
                                    handler.post(new Runnable() {
                                        public void run() {
                                            instream.setText(data);
                                        }
                                    });

                                    workDone = true;
                                    break;

                                } else if (btMsg == "start_stream!") {
                                    instream.setText(btMsg);
                                    //byte[] decodedImageBytes = Base64.decode(inputBytes, Base64.DEFAULT);
                                    //Bitmap bmp = BitmapFactory.decodeByteArray(decodedImageBytes, 0, decodedImageBytes.length);
                                    //Mat mat = Imgcodecs.imdecode(new MatOfByte(packetBytes), Imgcodecs.IMREAD_UNCHANGED);
                                    //Mat mat = new Mat (8,1,CvType.CV_8UC1,new Scalar(4));
                                    //mat = Imgcodecs.imdecode(new MatOfByte(packetBytes), Imgcodecs.IMREAD_UNCHANGED);
                                    //System.out.println(mat);
                                    //try {
                                        //Imgproc.cvtColor(seedsImage, tmp, Imgproc.COLOR_RGB2BGRA);
                                    //    Imgproc.cvtColor(seedsImage, tmp, Imgproc.COLOR_GRAY2RGBA, 4);
                                    //    bmp = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);
                                    //    Utils.matToBitmap(tmp, bmp);
                                    //}
                                    //catch (CvException e){Log.d("Exception",e.getMessage());}
                                    //Bitmap bmp = (ResourcesCompat.getDrawable(packetBytes, R.drawable.ic_home_black_24dp, null)as VectorDrawable).toBitmap();
                                    //BitmapFactory.Options options = new BitmapFactory.Options();
                                    Bitmap bmp = BitmapFactory.decodeByteArray(packetBytes, 0, packetBytes.length);
                                    //BitmapFactory.Options options = new BitmapFactory.Options();
                                    //options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                    //Bitmap bmp = BitmapFactory.decodeStream(mmInputStream, null, options);
                                    System.out.println(packetBytes.length);
                                    System.out.println(bmp);
                                    input_image.setImageBitmap(bmp);

                                    workDone = true;
                                    break;

                                } else {
                                    packetBytes[readBufferPosition++] = b;
                                }
                            }
                            if (workDone == true){
                                mmSocket.close();
                                break;
                            }

                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }
        };

        record_control.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if(record_control.isChecked()){
                    //Button is ON
                    Toast.makeText(getApplicationContext(),
                            "Recording Input video from RPi",
                            Toast.LENGTH_SHORT)
                            .show();
                    //call to instigate raspivid shell command
                    (new Thread(new messageThread("start_record!"))).start();
                    System.out.println(newFrameLength[0]);
                }
                else {
                    //Button is OFF
                    Toast.makeText(getApplicationContext(),
                            "Discarding Input video from RPi",
                            Toast.LENGTH_SHORT)
                            .show();
                    (new Thread(new messageThread("stop_record!"))).start();
                }
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Toast.makeText(getApplicationContext(),
                        "Uploading Input video from RPi",
                        Toast.LENGTH_SHORT)
                        .show();
                (new Thread(new messageThread("upload!"))).start();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Toast.makeText(getApplicationContext(),
                        "Deleting Input video from RPi",
                        Toast.LENGTH_SHORT)
                        .show();
                (new Thread(new messageThread("delete!"))).start();
            }
        });

        stream_control.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if(stream_control.isChecked()){
                    //Button is ON
                    Toast.makeText(getApplicationContext(),
                            "Streaming Input video from RPi",
                            Toast.LENGTH_SHORT)
                            .show();
                    //call to instigate stream input

                    //while(true){//iterate for each input video frame while video stream is inbound
                        //input length of incoming byte array, store into global variable newFrameLength
                    (new Thread(new messageThread("start_stream!"))).start();
                        //create buffer of enough bytes = number of int array elements: global variable inputFrame
                    //inputFrame[0] = new byte[newFrameLength[0]];
                        //fill the buffer with multiple packets(of maximum receiving size) of input array to form the single frame array
                    //int i = 0;
                    //while(i<newFrameLength[0]/maxPacketSize){//filling buffer array from subsequent packets from socket
                    //    byte[] encodedBytes = null;//byte array input array obtained from socket in each iteration
                    //    System.arraycopy(inputFrame[0], 0, encodedBytes, i*maxPacketSize, maxPacketSize);
                    //    i+=1;
                    //}
                        //Add remainder of bytes to buffer
                    //byte[] encodedBytes = null;//byte array input array obtained from socket in each iteration
                    //System.arraycopy(inputFrame[0], 0, encodedBytes, i*maxPacketSize, newFrameLength[0]%maxPacketSize);
                        //Convert the byte array into numpy array
                        //Decode the numpy array into image
                    //Mat mat = Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_UNCHANGED);
                        //Set image view as input frame
                    //}//iterate over multiple frames to generate video stream
                }
                else {
                    //Button is OFF
                    Toast.makeText(getApplicationContext(),
                            "Stopping Input stream from RPi",
                            Toast.LENGTH_SHORT)
                            .show();
                    (new Thread(new messageThread("stop_stream!"))).start();
                }
            }
        });

        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("raspberrypi")) //Note, you will need to change this to match the name of your device
                {
                    Log.e("CL",device.getName());
                    mmDevice = device;
                    break;
                }
            }
        }

    }

}