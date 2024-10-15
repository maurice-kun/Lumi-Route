package hs.fl.lumiroute.bluetooth;

import android.app.Application;

public class LumiApplication extends Application {
    private static LumiApplication sInstance;
    ConnectedThread connectedThread = null;

    public static LumiApplication getApplication() {
        return sInstance;
    }

    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public void setupConnectedThread(ConnectedThread connectedThread) {
        this.connectedThread = connectedThread;
    }

    public ConnectedThread getCurrentConnectedThread() {
        return connectedThread;
    }
}
