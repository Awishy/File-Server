package client;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Main {
    private static final String IP_ADDRESS = "127.0.0.1";

    private static final int PORT = 25555;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void initialize() {
        new File(Client.getClientFilesPath()).mkdirs();
    }

    public static void main(String[] args) {
        try {
            Thread.sleep(60);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        initialize();
        while (true) {
            try (
                    Socket socket = new Socket(InetAddress.getByName(IP_ADDRESS), PORT)
            ) {
                Client client = new Client(socket);
                if (client.process()) {
                    client.close();
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
