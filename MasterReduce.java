import org.w3c.dom.Node;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.text.html.parser.AttributeList;


public class MasterReduce implements Runnable{
    Socket socket;
    DataInputStream dataInputStream;
    private HashMap<Integer, ArrayList<Double>> reduce;
    private HashMap<Integer, Integer> totalchunks;

    MasterReduce(Socket socket, DataInputStream dataInputStream,HashMap<Integer, ArrayList<Double>> reduce,HashMap<Integer, Integer> totalchunks){
        this.socket = socket;
        this.dataInputStream = dataInputStream;
        this.reduce = reduce;
        this.totalchunks = totalchunks;
    }

    public void run(){



        while(true){
            ObjectInputStream obj = null;
            try {
                obj = new ObjectInputStream(dataInputStream);
                HashMap<Integer,ArrayList<Double>> map = (HashMap<Integer, ArrayList<Double>>) obj.readObject();
                reduce(map);

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }


        }

    }
    public void reduce(HashMap<Integer, ArrayList<Double>> map){
        Iterator<ConcurrentHashMap.Entry<Integer, ArrayList<Double>>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            int userID = iterator.next().getKey();
            ArrayList<Double> chunkArraylist = map.get(userID);
            synchronized(reduce){
                if(!reduce.containsKey(userID)){
                    double chunksPlaced =1.0;
                    chunkArraylist.add(chunksPlaced);
                    reduce.put(userID, chunkArraylist);

                }else{

                    ArrayList<Double> reduceArrayList = reduce.get(userID);
                    Double totalDistance = reduceArrayList.get(0);
                    Double totalSpeed = reduceArrayList.get(1);
                    Double totalElevation = reduceArrayList.get(2);
                    Double totalSeconds = reduceArrayList.get(3);
                    Double chunksPlaced =reduceArrayList.get(4);
                    //prosthetw ola ta chunks mazi kai ta vazw sto neo map
                    totalDistance += chunkArraylist.get(0);
                    double d = totalchunks.get(userID);
                    totalSpeed += chunkArraylist.get(1);  //mesh taxuthta
                    totalElevation += chunkArraylist.get(2);
                    totalSeconds += chunkArraylist.get(3);
                    chunksPlaced++;
                    reduceArrayList.add(0, totalDistance);
                    reduceArrayList.add(1, totalSpeed);
                    reduceArrayList.add(2, totalElevation);
                    reduceArrayList.add(3, totalSeconds);
                    reduceArrayList.add(4, chunksPlaced);

                }
            }
        }

    }

}