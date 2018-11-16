package webservice.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

@SuppressWarnings("Duplicates")

public class Get {

    private String urlString;
    private String response;

    public Get(String url, String path)   {
        this.urlString = url + path;
    }

    public Get(String url, String path, int taskNumber, int sessionID)   {
        this.urlString = url + path + "/" + taskNumber + "?sessionId=" + sessionID;
    }

    public void sendGet()   {
        try {
            //Create url object
            URL urlObject = new URL(urlString);
            System.out.println(">>> Sending GET to url " + urlString);

            //Create and handle a connection
            HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
            connection.setRequestMethod("GET");

            //Handle server response
            int responseCode = connection.getResponseCode();
            if(200 == responseCode) {
                //Handle "OK" server response
                System.out.println("<<< Server response \"OK\"");

                InputStream stream = connection.getInputStream();
                response = convertInputStreamToString(stream);
                stream.close();
                System.out.println("<<< Response from the server: \n" + response);
            }
            else    {
                //Handle other server responses
                String responseMsg = connection.getResponseMessage();
                System.out.println("!!! Request error. " +
                        "\n\tResponse code \"" + responseCode + "\"" +
                        "\n\tResponse message \"" + responseMsg + "\"");
            }
        }
        //Catch possible exceptions
        catch (ProtocolException pe) {
            System.out.println("!!! Protocol not supported by the server");
        }
        catch (IOException ioe) {
            System.out.println("!!! Connection error" +
                    "\n\t" + ioe.getMessage());
            ioe.printStackTrace();
        }
    }

    private String convertInputStreamToString(InputStream stream)  {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        StringBuilder serverResponse = new StringBuilder();
        try {
            String inputLine;
            while(null != (inputLine = br.readLine())) {
                serverResponse.append(inputLine);
                serverResponse.append("\n");
            }
        }
        catch (IOException ioe) {
            System.out.println("!!! Could not read server response" +
                    "\n\t" + ioe.getMessage());
            ioe.printStackTrace();
        }
        return serverResponse.toString();
    }

    public String getResponseString() {
        return response;
    }
}
