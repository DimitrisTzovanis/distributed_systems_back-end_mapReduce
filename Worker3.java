import org.w3c.dom.Node;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Worker3 implements Runnable {

    int port;
    static DataInputStream dataInputStream;
    static DataOutputStream dataOutputStream;
    ArrayList<Node> listOfNodes;

    Worker3(int port){
        this.port = port;
    }
    public void run(){
        //files workers receive from Master
        try(Socket socket = new Socket("localhost",port)){

            System.out.println("connected to master");
            while(true){
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                ObjectInputStream obj = new ObjectInputStream(dataInputStream);
                ArrayList<Node> listOfNodes = (ArrayList<Node>) obj.readObject();
                System.out.println("chunk recieved");
                int userID = dataInputStream.readInt();

                new Thread(new WorkerThread(socket, listOfNodes, userID)).start();


                //TODO
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
    public static void main(String[] args) throws IOException{
        int numWorkers = 3;
        int workerPort = 9090;
        Worker worker3 = new Worker(workerPort);
        worker3.run();
    }



}

