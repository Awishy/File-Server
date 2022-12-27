package server;

import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

public class Request {
    private final String type;
    private final String accessType;
    private final String fileIdentifier;
    private final String fileName;
    private final ConcurrentHashMap<Integer, String> fileNameById;

    private static final String DEFAULT_ACCESS_TYPE = "BY_NAME";

    public Request (final String request, ConcurrentHashMap<Integer, String> fileNameById) {
        StringTokenizer stringTokenizer = new StringTokenizer(request);
        System.out.println("request: " + request);
        this.type = stringTokenizer.nextToken();
        this.accessType = stringTokenizer.countTokens() > 1 ? stringTokenizer.nextToken() : DEFAULT_ACCESS_TYPE;
        this.fileIdentifier = stringTokenizer.nextToken();
        this.fileNameById = fileNameById;
        this.fileName = initializeFileName();
    }

    private String initializeFileName() {
        switch (getAccessType()) {
            case "BY_ID":
                return fileNameById.get(Integer.parseInt(getFileIdentifier()));
            case "BY_NAME":
                return getFileIdentifier();
        }
        return null;
    }

    public String getType() {
        return type;
    }

    public String getAccessType() {
        return accessType;
    }

    public String getFileIdentifier() {
        return fileIdentifier;
    }

    public String getFileName() {
        return fileName;
    }
}
