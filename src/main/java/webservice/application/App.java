package webservice.application;

import webservice.logic.Client;

public class App {

    public static void main(String[] args) {
        //Connection variables
        String host = "104.248.47.74";
        int port = 80;

        //Create a client and call to all tasks
        Client client = new Client(host, port);
        client.authorize("email@goes.here", 69696969);
        for(int i=1; i <= 4; i++)   {
            client.solveTask(i);
        }
        //client.solveTask(2016);
        client.getFeedback();
    }
}
