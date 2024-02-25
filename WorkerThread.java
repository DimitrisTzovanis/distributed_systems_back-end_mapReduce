import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.math.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WorkerThread implements Runnable{

    Socket socket;
    ArrayList<Node> listOfNodes; //one chunk
    int userID;
    private static final Lock socketLock = new ReentrantLock();

    WorkerThread(Socket socket, ArrayList<Node> listOfNodes, int userID){

        this.socket = socket;
        this.listOfNodes = listOfNodes;
        this.userID = userID;
    }
    public void run(){
        String lat[]= new String[2];
        String lon[] = new String[2];
        String[] ele = new String[2];
        String[] time = new String[2];

        for(int i=0; i<=1; i++){
            Node nNode = listOfNodes.get(i);

            // Get the latitude and longitude attributes of the wpt element
            NamedNodeMap attributes = nNode.getAttributes();

            lat[i] = attributes.getNamedItem("lat").getNodeValue();

            lon[i] = attributes.getNamedItem("lon").getNodeValue();

            // Get the child elements of the wpt element
            NodeList children = nNode.getChildNodes();

            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    String nodeName = child.getNodeName();
                    String nodeValue = child.getTextContent();
                    if (nodeName.equals("ele")) {
                        ele[i] = nodeValue;
                    } else if (nodeName.equals("time")) {
                        time[i] = nodeValue;
                    }
                }
            }
        }

        try {
            map(lat, lon, ele, time);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private double distance(double x1, double x2, double y1, double y2){
        return Math.sqrt(Math.pow(x2 - x1,2)+ Math.pow(y2-y1,2));
    }

    private void map(String[] lat, String[] lon, String[] ele, String[] time) throws IOException {
        double x1 = Double.parseDouble(lat[0]);
        double x2 = Double.parseDouble(lat[1]);
        double y1 = Double.parseDouble(lon[0]);
        double y2 = Double.parseDouble(lon[1]);

        double distance = distance(x1, x2, y1, y2)*111139; //metatroph se metra

        String format = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        LocalDateTime dateTime1 = LocalDateTime.parse(time[0], formatter);
        LocalDateTime dateTime2 = LocalDateTime.parse(time[1], formatter);
        // Calculate the difference between the timestamps
        Duration duration = Duration.between(dateTime1, dateTime2);
        // Print the difference in seconds
        long seconds = duration.getSeconds();

        double speed = (distance /(double)seconds);

        float elevation1 = Float.parseFloat(ele[0]);
        float elevation2 = Float.parseFloat(ele[1]);
        float Elevation=0;
        if(elevation2>elevation1){
            Elevation += elevation2-elevation1;
        }

        HashMap<Integer, ArrayList<Double>> Map = new HashMap<>();
        ArrayList<Double> chunk = new ArrayList<>();
        chunk.add(distance); //0
        chunk.add(speed); //1
        chunk.add((double) Elevation);
        chunk.add((double) seconds);
        Map.put(userID, chunk);


        synchronized (socket){
            socketLock.lock();
            try {
                DataOutputStream d = new DataOutputStream(socket.getOutputStream());
                ObjectOutputStream obj2 = new ObjectOutputStream(d);
                obj2.writeObject(Map);
            } finally {
            // Release the lock after sending data
            socketLock.unlock();
        }
        }
    }
}