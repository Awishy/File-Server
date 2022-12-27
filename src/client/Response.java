package client;

public class Response {
    private final int statusCode;
    private final String data;

    Response(String response) {
        String[] tokens = response.split(" ", 2);
        this.statusCode = Integer.parseInt(tokens[0]);
        this.data = tokens.length > 1 ? tokens[1] : "";
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getData() {
        return data;
    }
}
