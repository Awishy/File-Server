package server;

import utils.DataUtils;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    private static final String IP_ADDRESS = "127.0.0.1";
    private static final int PORT = 25555;
    private static final String MAP_FILE_PATH = "./src/server/storage/";
    private static final File MAP_FILE = new File(MAP_FILE_PATH, "map");

    private static final AtomicBoolean isRunning = new AtomicBoolean(true);

    private static ConcurrentHashMap<Integer, String> fileNameById = new ConcurrentHashMap<>();

    private static void saveMap() {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(MAP_FILE);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream);
            objectOutputStream.writeObject(fileNameById);
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadMap() {
        try {
            if (MAP_FILE.exists()) {
                FileInputStream fileInputStream = new FileInputStream(MAP_FILE);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);
                //noinspection unchecked
                fileNameById = (ConcurrentHashMap<Integer, String>) objectInputStream.readObject();
                objectInputStream.close();
            }
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void initialize() {
        new File(Server.getServerFilesPath()).mkdirs();
        new File(MAP_FILE_PATH).mkdirs();
        loadMap();
    }

    public static void main(String[] args) {
        initialize();
        try (
                ServerSocket server = new ServerSocket(PORT, 50, InetAddress.getByName(IP_ADDRESS))
        ) {
            while (isRunning.get()) {
                Session session = new Session(server, server.accept());
                session.start();
            }
        } catch (SocketException e) {
            // EXIT
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            saveMap();
        }
    }


    private static class Session extends Thread {
        private final ServerSocket serverSocket;
        private final Socket socket;

        public Session(ServerSocket serverSocket, Socket socket) {
            this.serverSocket = serverSocket;
            this.socket = socket;
        }

        public void run() {
            try (
                    DataInputStream inputStream = new DataInputStream(socket.getInputStream())
            ) {
                String request = DataUtils.readMessage(inputStream);
                if ("EXIT".equals(request)) {
                    Main.isRunning.set(false);
                    socket.close();
                    serverSocket.close();
                    return;
                }
                Server server = new Server(request, socket, Main.fileNameById);
                server.process();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}