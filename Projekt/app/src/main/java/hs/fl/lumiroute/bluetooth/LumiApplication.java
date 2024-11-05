package hs.fl.lumiroute.bluetooth;

import android.app.Application;

public class LumiApplication extends Application {
    private static LumiApplication sInstance;
    private ConnectedThread connectedThread = null;

    public static LumiApplication getApplication() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public synchronized void setupConnectedThread(ConnectedThread thread) {
        if (connectedThread != null) {
            connectedThread.cancel(); // Vorherigen Thread schließen
        }
        connectedThread = thread;
        connectedThread.start(); // Starte den neuen Thread
    }

    public synchronized ConnectedThread getCurrentConnectedThread() {
        return connectedThread;
    }
}