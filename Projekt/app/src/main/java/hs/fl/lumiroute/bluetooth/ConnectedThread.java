package hs.fl.lumiroute.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {

    private static final String TAG = "FrugalLogs";
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private String valueRead;
    private volatile boolean isRunning = true; // Hinzugefügt, um den Thread sicher zu stoppen
    private Handler handler;

    public InputStream getMmInStream() {
        return mmInStream;
    }

    public OutputStream getMmOutStream() {
        return mmOutStream;
    }

    public ConnectedThread(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public String getValueRead() {
        return valueRead;
    }

    public void run() {
        byte[] buffer = new byte[1024];
        int bytes = 0;

        while (isRunning) {
            try {
                buffer[bytes] = (byte) mmInStream.read();
                String readMessage;
                if (buffer[bytes] == '\n') {
                    readMessage = new String(buffer, 0, bytes);
                    Log.i(TAG, "Read message: " + readMessage);
                    valueRead = readMessage;
                    bytes = 0;
                } else {
                    bytes++;
                }
            } catch (IOException e) {
                Log.e(TAG, "Input stream was disconnected", e);
                break;
            }
        }
    }

    public void write(String input) {
        byte[] bytes = input.getBytes();
        try {
            mmOutStream.write(bytes);
            Log.i(TAG, "Sent message: " + input);
        } catch (IOException e) {
            Log.e(TAG, "Unable to send message", e);
        }
    }

    public void cancel() {
        isRunning = false; // Setzt die Laufbedingung auf false, um den Thread zu beenden
        try {
            mmSocket.close();
            Log.i(TAG, "Socket closed");
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
}
