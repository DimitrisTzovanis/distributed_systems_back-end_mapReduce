import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Master{
    static int userPort;
    static int workerPort;
    static int numWorkers;
    static Socket[] socketArray = new Socket[numWorkers];
    DataInputStream[] dataInputStream = new DataInputStream[numWorkers];
    static DataOutputStream[] dataOutputStream = new DataOutputStream[numWorkers];
    static private HashMap<Integer, ArrayList<Double>> reduce = new HashMap<>();
    static private HashMap<Integer, Integer> totalchunks = new HashMap<>();


    public Master(int userPort, int workerPort, int numWorkers) throws IOException {

        this.userPort = userPort;
        this.workerPort= workerPort;
        this.numWorkers = numWorkers;
        listenForWorkers();
        listenForUsers();
    }
    //tcp server
    private static void listenForUsers() throws IOException{

        ServerSocket serverSocket = new ServerSocket(userPort);
        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("user connected");
            //thread pou epeksergazetai to input ths efarmoghs
            Random r = new Random();
            int userID = r.nextInt();
            System.out.println(userID);
            new Thread(new MasterThread(socket, dataOutputStream,numWorkers,socketArray, r.nextInt(), totalchunks, reduce)).start();

        }
    }
    private void listenForWorkers() throws IOException{
        socketArray = new Socket[numWorkers];
        dataInputStream = new DataInputStream[numWorkers];
        dataOutputStream = new DataOutputStream[numWorkers];

        ServerSocket serverSocket = new ServerSocket(workerPort);
        for(int i=0;i<numWorkers; i++ ){


            Socket socket = serverSocket.accept();
            System.out.println("worker connected");
            socketArray[i] = socket;
            dataInputStream[i] = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            dataOutputStream[i] = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            //lamvanei map arxeia apo workerthread
            new Thread(new MasterReduce(socket,dataInputStream[i], reduce, totalchunks)).start();

        }

    }
    public static void main(String[] args) throws IOException {

        int port1 = 10090;
        int port2 = 9090;
        int numWorkers = 3;
        Master master = new Master(port1, port2, numWorkers);
    }
}