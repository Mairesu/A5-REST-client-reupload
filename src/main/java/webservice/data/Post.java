package webservice.data;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

@SuppressWarnings("Duplicates")

public class Post{

    //fields for the post object to keep track of
    private String urlString;
    private JSONObject jsonObject;
    private String response;

    /**
     * Create a post object responsible for posting JSON object using HTTP POST request
     * @param url String representing the url (+port) for the post to go
     * @param path String representing the path in the url for the post to go
     * @param jsonObject a JSONObject to be sent by the post object
     */
    public Post(String url, String path, JSONObject jsonObject)   {
        //create Post object with url from client, selected path, and a constructed json object
        this.urlString = url + path;
        this.jsonObject = jsonObject;
    }

    /**
     *
     */
    public void sendPost() {
        try {
            //Create url object
            URL urlObject = new URL(urlString);
            System.out.println(">>> Sending POST to url " + urlString);

            //Create and handle a connection
            HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            OutputStream os = connection.getOutputStream();
            os.write(jsonObject.toString().getBytes());
            os.flush();

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

