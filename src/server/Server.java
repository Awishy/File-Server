package server;

import utils.DataUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private final String requestString;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    private final ConcurrentHashMap<Integer, String> fileNameById;
    private static int availableId;

    private static final String SERVER_FILES_PATH = "./src/server/data/";

    public Server(
            String request,
            Socket socket,
            ConcurrentHashMap<Integer, String> fileNameById
    ) throws IOException {
        this.requestString = request;
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
        this.fileNameById = fileNameById;

        synchronized (Server.class) {
            availableId = fileNameById.keySet().stream().mapToInt(v -> v).max().orElse(0) + 1;
        }
    }

    public static String getServerFilesPath() {
        return SERVER_FILES_PATH;
    }

    public void process() {
        try {
            processRequest();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeStatusCode(int statusCode) throws IOException {
        DataUtils.writeMessage(outputStream, Integer.toString(statusCode));
    }

    private void processRequest() throws IOException {
        Request request = new Request(requestString, fileNameById);
        boolean processed = false;
        try {
            if (request.getFileName() != null) {
                File file = new File(SERVER_FILES_PATH, request.getFileName());
                switch (request.getType()) {
                    case "PUT":
                        processed = processPut(file);
                        if (!processed) {
                            writeStatusCode(HttpURLConnection.HTTP_FORBIDDEN);
                            processed = true;
                        }
                        break;
                    case "GET":
                        processed = processGet(file);
                        break;
                    case "DELETE":
                        processed = processDelete(file);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            writeStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
            return;
        }
        if (!processed) {
            writeStatusCode(HttpURLConnection.HTTP_NOT_FOUND);
        }
    }

    private boolean processGet(File file) throws IOException {
        if (file.exists()) {
            writeStatusCode(HttpURLConnection.HTTP_OK);
            DataUtils.sendFileToOutput(outputStream, file);
            return true;
        }
        return false;
    }

    private static synchronized int getAvailableId() {
        return availableId++;
    }

    private boolean processPut(File file) throws IOException {
        if (!DataUtils.saveFileFromInput(inputStream, file)) {
            return false;
        }
        int id = getAvailableId();
        fileNameById.put(id, file.getName());
        DataUtils.writeMessage(outputStream, HttpURLConnection.HTTP_OK + " " + id);
        return true;
    }

    private boolean processDelete(File file) throws IOException {
        if (file.delete()) {
            fileNameById.values().remove(file.getName());
            writeStatusCode(HttpURLConnection.HTTP_OK);
            return true;
        }
        return false;
    }
}
