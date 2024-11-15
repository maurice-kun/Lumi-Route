package hs.fl.lumiroute.connection;

import java.io.*;
import java.net.Socket;

public class SocketClient {
    private Socket socket;
    private BufferedReader input;
    private String serverIp = "192.168.0.100";  // IP-Adresse des PCs, auf dem Unity läuft
    private int serverPort = 8888;

    public void start() {
        new Thread(() -> {
            try {
                socket = new Socket(serverIp, serverPort);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                while (true) {
                    String message = input.readLine();
                    if (message != null) {
                        System.out.println("Received from Unity: " + message);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void stop() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}