package client;

import utils.DataUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;

public class Client {
    private static final String CLIENT_FILES_PATH = "./src/client/data/";

    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    private final BufferedReader bufferedReader;

    public Client(final Socket socket) throws IOException {
        this.socket = socket;
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
        this.bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    }

    public void close() throws IOException {
        socket.close();
    }

    public static String getClientFilesPath() {
        return CLIENT_FILES_PATH;
    }

    private static boolean containsWhitespace(String string) {
        for (char ch : string.toCharArray()) {
            if (Character.isWhitespace(ch)) {
                return true;
            }
        }
        return false;
    }

    private String getFileName(String message) throws IOException {
        System.out.println(message);
        while (true) {
            String fileName = bufferedReader.readLine();
            if (containsWhitespace(fileName)) {
                System.out.println("File name shouldn't contain whitespaces.");
                continue;
            }
            return fileName;
        }
    }

    private Response getResponseTokens() throws IOException {
        return new Response(DataUtils.readMessage(inputStream));
    }

    private String getFileNameOrIdRequest() throws IOException {
        System.out.println("Do you want to get the file by name or by id (1 - name, 2 - id):");
        while (true) {
            switch (bufferedReader.readLine()) {
                case "1":
                    String filename = getFileName("Enter name of the file:");
                    if (filename != null) {
                        return "BY_NAME " + filename;
                    }
                    break;
                case "2":
                    System.out.println("Enter id:");
                    String id = bufferedReader.readLine();
                    try {
                        Integer.parseInt(id);
                        return "BY_ID " + id;
                    } catch (NumberFormatException e) {
                        // ignored
                    }
                    break;
            }
            System.out.println("Wrong input.");
        }
    }

    private void sendRequest(String request) throws IOException {
        DataUtils.writeMessage(outputStream, request);
        System.out.println("The request was sent.");
    }

    // Returns false and prints error message, otherwise returns true without any output.
    private static boolean checkStatus(int statusCode) {
        switch (statusCode) {
            case HttpURLConnection.HTTP_OK:
                return true;
            case HttpURLConnection.HTTP_FORBIDDEN:
                System.out.println("The response says that creating the file was forbidden!");
                break;
            case HttpURLConnection.HTTP_NOT_FOUND:
                System.out.println("The response says that this file is not found!");
                break;
            case HttpURLConnection.HTTP_INTERNAL_ERROR:
                System.out.println("The response says that an internal server error happened.");
                break;
        }
        return false;
    }

    public boolean process() throws IOException {
        System.out.println("Enter action (1 - get a file, 2 - save a file, 3 - delete a file):");
        String action = bufferedReader.readLine();
        if ("exit".equals(action)) {
            sendRequest("EXIT");
            return true;
        }

        switch (action) {
            case "1": // GET
                String fileNameOrIdRequest = getFileNameOrIdRequest();
                sendRequest("GET " + fileNameOrIdRequest);

                Response response = getResponseTokens();
                if (checkStatus(response.getStatusCode())) {
                    String savingFileName = getFileName("The file was downloaded! Specify a name for it:");
                    DataUtils.saveFileFromInput(inputStream, new File(CLIENT_FILES_PATH, savingFileName));
                }
                break;
            case "2": // PUT
                String fileName = getFileName("Enter name of the file:");
                File file = new File(CLIENT_FILES_PATH, fileName);
                if (fileName.isEmpty() || !file.exists()) {
                    System.out.println("File '" + fileName + "' doesn't exist.");
                    return false;
                }

                String serverFileName = getFileName("Enter name of the file to be saved on server:");
                if (serverFileName.isBlank()) {
                    serverFileName = fileName;
                }
                sendRequest("PUT " + serverFileName);
                DataUtils.sendFileToOutput(outputStream, file);

                response = getResponseTokens();
                if (checkStatus(response.getStatusCode())) {
                    System.out.println("Response says that file is saved! ID = " + response.getData());
                }
                break;
            case "3": // DELETE
                fileNameOrIdRequest = getFileNameOrIdRequest();
                sendRequest("DELETE " + fileNameOrIdRequest);

                response = getResponseTokens();
                if (checkStatus(response.getStatusCode())) {
                    System.out.println("The response says that this file was deleted successfully!");
                }
                break;
        }
        return false;
    }
}
