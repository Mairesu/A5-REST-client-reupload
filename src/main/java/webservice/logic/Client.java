package webservice.logic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import webservice.data.Get;
import webservice.data.Post;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Client {

    private String url = "http://";

    private int sessionId;
    private int userId;
    private boolean authorized = false;

    public Client(String host, int port)    {
        url += host + ":" + port + "/";
    }

    public void authorize(String email, int phoneNumber) {
        System.out.println("####################### AUTHORIZATION START #######################");

        System.out.println("=== Creating authentication JSON object with" +
                "\n\tEmail: \""+ email +"\"" +
                "\n\tPhone number: \""+ phoneNumber +"\"");

        String jsonObjString = "{ \"email\": \"" + email + "\", \"phone\": \"" + phoneNumber + "\"}";
        System.out.println("=== JSON string: " + jsonObjString);
        JSONObject authJSON = generateJSONObject(jsonObjString);

        Post authPost = new Post(url, "dkrest/auth", authJSON);
        authPost.sendPost();

        try {
            if (null != authPost.getResponseString()) {

                JSONObject responseObj = generateJSONObject(authPost.getResponseString());

                System.out.println("=== " + responseObj.getString("comment"));

                if (responseObj.getBoolean("success")) {
                    authorized = true;
                    sessionId = responseObj.getInt("sessionId");
                    userId = responseObj.getInt("userId");
                    System.out.println("=== User ID: \"" + userId + "\", " +
                            "\n\tSession ID: \"" + sessionId + "\"");
                }
            }
            else {
                System.out.println("!!! Response string does not exist");
            }
        }
        catch (JSONException je)    {
            System.out.println("!!! Something happened while processing the JSON authorization response" +
                    "\n\t" + je.getMessage());
        }
        System.out.println("####################### AUTHORIZATION END #######################\n\n");
    }

    public void solveTask(int taskNumber)    {
        System.out.println("####################### TASK " + taskNumber + " START #######################");

        if(authorized)  {
            String jsonAnswString = null;
            Get get = new Get(url, "dkrest/gettask", taskNumber, sessionId);
            get.sendGet();
            JSONObject responseJSON = generateJSONObject(get.getResponseString());
            switch(taskNumber) {
                case 1:
                    jsonAnswString = "{ \"sessionId\": \"" + sessionId + "\", \"msg\": \"Hello\"}";
                    break;

                case 2:
                    jsonAnswString = task2(responseJSON);
                    break;

                case 3:
                    jsonAnswString = task3(responseJSON);
                    break;

                case 4:
                    jsonAnswString = task4(responseJSON);
                    break;

                case 2016:
                    //jsonAnswString = secretTask(responseJSON);
                    break;

                default:
                    System.out.println("!!! Unknown task number occurred");
                    break;
            }
            if(null != jsonAnswString) {
                Post answerPost = new Post(url, "dkrest/solve", generateJSONObject(jsonAnswString));
                answerPost.sendPost();
            }
            else {
                System.out.println("!!! JSON answer content was empty");
            }
        }
        else    {
            System.out.println("!!! Authorization step was unsuccessful" +
                    "\n\tUnable to solve tasks");
        }
        System.out.println("####################### TASK " + taskNumber + " END #######################\n\n");
    }

    public String task2(JSONObject responseJSON)   {

        JSONArray argArray = responseJSON.getJSONArray("arguments");
        return "{ \"sessionId\": \"" + sessionId + "\", \"msg\": \"" + argArray.get(0) + "\"}";

    }

    public String task3(JSONObject responseJSON)    {
        int product = 1; //All numbers in the arguments array are supposed to be multiplied. multiplying by 0 gives 0

        JSONArray argArray = responseJSON.getJSONArray("arguments");

        for(int i = 0; i < argArray.length(); i++)  {
            product = product * Integer.parseInt(argArray.getString(i));
        }

        return "{ \"sessionId\": \"" + sessionId + "\", \"result\": \"" + product + "\"}";
    }

    public String task4(JSONObject responseJSON)   {
        String pin = null;
        String hash = responseJSON.getJSONArray("arguments").get(0).toString();
        try {
            int i = 0;
            boolean foundMatch = false;
            while (!foundMatch && i < 10000) {
                String numberAsString = String.valueOf(i);
                pin = "0000".substring(numberAsString.length()) + numberAsString;

                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] hashInBytes = md.digest(pin.getBytes());

                StringBuilder sb = new StringBuilder();
                for (byte b : hashInBytes)  {
                    sb.append(String.format("%02x", b));
                }
                if(hash.equals(sb.toString()))  {
                    foundMatch = true;
                }
                else {
                    i++;
                }
            }
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "{ \"sessionId\": \"" + sessionId + "\", \"pin\": \"" + pin + "\"}";
    }

    /*
    private String secretTask(JSONObject responseJSON)   {
        JSONArray argArray = responseJSON.getJSONArray("arguments");

        String networkIP = argArray.get(0).toString();
        String subnetMask = argArray.get(1).toString();
        int[] networkParts = Arrays.stream(networkIP.split("\\.")).mapToInt(Integer::parseInt).toArray();
        int[] subnetParts = Arrays.stream(subnetMask.split("\\.")).mapToInt(Integer::parseInt).toArray();
        String[] networkBitString = {"", "", "", ""};
        String[] subnetBitString = {"", "", "", ""};

        for(int i = 0; i < networkParts.length; i++)    {
            networkBitString[i] = Integer.toBinaryString(0x100 | networkParts[i]).substring(1);
        }

        for(int i = 0; i < subnetParts.length; i++)    {
            subnetBitString[i] = Integer.toBinaryString(0x100 | subnetParts[i]).substring(1);
        }

        return "{ \"sessionId\": \"" + sessionId + "\", \"ip\": \"" + networkBitString.toString() + "\"}";
    }
    */

    public void getFeedback()   {
        System.out.println("####################### FEEDBACK #######################");
        if(authorized)  {
            Get get = new Get(url, "dkrest/results/" + sessionId);
            get.sendGet();
        }
        else    {
            System.out.println("!!! Authorization step was unsuccessful" +
                    "\n\tUnable to solve task");
        }
        System.out.println("####################### FEEDBACK #######################\n\n");
    }

    public JSONObject generateJSONObject(String JSONString)  {

        JSONObject jsonObject = null;

        if(null != JSONString) {
            try {
                jsonObject = new JSONObject(JSONString);
                System.out.println("=== JSON object created" +
                        "\n\t" + jsonObject.toString());

            } catch (JSONException je) {
                System.out.println("!!! JSON object construction failure" +
                        "\n\t" + je.getMessage());
            }
        }
        else    {
            System.out.println("!!! JSON string is null. Something bad happened");
        }

        return jsonObject;
    }
}
