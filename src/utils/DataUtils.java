package utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class DataUtils {
    private static byte[] readBytes(DataInputStream inputStream) throws IOException {
        int bytes_length = inputStream.readInt();
        byte[] bytes = new byte[bytes_length];
        inputStream.readFully(bytes, 0, bytes_length);
        return bytes;
    }

    public static boolean saveFileFromInput(DataInputStream inputStream, File file) throws IOException {
        byte[] bytes = readBytes(inputStream);
        if (file.exists()) {
            return false;
        }
        Files.write(file.toPath(), bytes);
        return true;
    }

    public static String readMessage(DataInputStream inputStream) throws IOException {
        byte[] bytes = readBytes(inputStream);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static void writeBytes(DataOutputStream outputStream, byte[] bytes) throws IOException {
        outputStream.writeInt(bytes.length);
        outputStream.write(bytes);
        outputStream.flush();
    }

    public static void sendFileToOutput(DataOutputStream outputStream, File file) throws IOException {
        writeBytes(outputStream, Files.readAllBytes(file.toPath()));
    }

    public static void writeMessage(DataOutputStream outputStream, String message) throws IOException {
        writeBytes(outputStream, message.getBytes());
    }
}
