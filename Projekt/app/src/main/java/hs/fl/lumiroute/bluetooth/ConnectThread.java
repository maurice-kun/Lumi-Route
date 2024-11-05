package hs.fl.lumiroute.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private static final String TAG = "FrugalLogs";
    public static Handler handler;
    private static final int ERROR_READ = 0;
    private static final int SUCCESS_CONNECT = 1; // Hinzugefügt für erfolgreichen Verbindungsstatus

    @SuppressLint("MissingPermission")
    public ConnectThread(BluetoothDevice device, UUID MY_UUID, Handler handler) {
        BluetoothSocket tmp = null;
        this.handler = handler;

        try {
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    @SuppressLint("MissingPermission")
    public void run() {
        // Verwende Looper, um sicherzustellen, dass der Handler auf dem Haupt-Thread läuft
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }

        try {
            mmSocket.connect();
            // Verbindung erfolgreich - sende Nachricht zurück
            handler.obtainMessage(SUCCESS_CONNECT, "Connected to the BT device").sendToTarget();
            Log.i(TAG, "Connection established");

            // Starte den ConnectedThread
            LumiApplication.getApplication().setupConnectedThread(new ConnectedThread(mmSocket));

        } catch (IOException connectException) {
            handler.obtainMessage(ERROR_READ, "Unable to connect to the BT device").sendToTarget();
            Log.e(TAG, "Connection failed", connectException);
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
        }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }

    public BluetoothSocket getMmSocket() {
        return mmSocket;
    }
}
