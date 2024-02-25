import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

public class MasterThread implements Runnable{

    private static DataInputStream userInputStream;
    Socket socket;
    int numWorkers;
    DataOutputStream[] dataOutputStream;
    Socket[] socketarray;
    int userID;
    private HashMap<Integer, Integer> totalchunks;
    private HashMap<Integer, ArrayList<Double>> reduce;
    private static final Lock socketLock = new ReentrantLock();

    MasterThread(Socket socket, DataOutputStream[] dataOutputStream,int numWorkers, Socket[] socketarray, int userID, HashMap<Integer, Integer> totalchunks,HashMap<Integer, ArrayList<Double>> reduce){

        this.socket = socket;
        this.dataOutputStream = dataOutputStream;
        this.numWorkers = numWorkers;
        this.socketarray = socketarray;
        this.userID = userID;
        this.totalchunks = totalchunks;
        this.reduce =reduce;
    }
    //files master receives from App
    public void run(){
        try{

            userInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            receiveFile("NewFile1"+userID+".xml");
            File input = new File("NewFile1"+userID+".xml");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            Document doc = dbf.newDocumentBuilder().parse(input);
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xpath.evaluate("//gpx/wpt", doc, XPathConstants.NODESET);

            System.out.println("file recieved");
            int roundrobin=0;
            Document currentDoc = dbf.newDocumentBuilder().newDocument();
            Node rootNode = currentDoc.createElement("gpx");
            synchronized(totalchunks){
                totalchunks.put(userID, nodes.getLength()-1);//tha mbei ston map me ton arithmo twn chunks
            }



            for (int i=1; i <= nodes.getLength()-1; i++) {

                Node imported1 = currentDoc.importNode(nodes.item(i-1), true);
                Node imported2 = currentDoc.importNode(nodes.item(i), true);
                ArrayList<Node> Chunks = new ArrayList<>();
                System.out.println("chunk created");
                Chunks.add(imported1);
                Chunks.add(imported2);

                //stelnoume ta chunks stous workers me RR
                socketLock.lock();
                try{
                    ObjectOutputStream obj = new ObjectOutputStream(dataOutputStream[roundrobin]);
                    obj.writeObject(Chunks);
                    obj.flush();

                    dataOutputStream[roundrobin].writeInt(userID);
                    dataOutputStream[roundrobin].flush();
                } finally {
                    // Release the lock after sending data
                    socketLock.unlock();
                }
                roundrobin++;
                if(roundrobin>=numWorkers){
                    roundrobin = 0;
                }
            }


            try{
                while(true){

                    ArrayList<Double> check = reduce.get(userID);
                    double chunkdouble = totalchunks.get(userID);
                    if(check!=null && totalchunks!=null) {
                        if (check.get(4) == chunkdouble) {
                            DataOutputStream userdDataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                            ObjectOutputStream obj3 = new ObjectOutputStream(userdDataOutputStream);
                            ArrayList<Double> check2 =new ArrayList<>();
                            check2 = check;
                            double distance = check.get(0);
                            double speed = check.get(1);
                            double elevation = check.get(2);
                            double time = check.get(3);
                            check2.add(0,distance);
                            double speed2 = (speed/chunkdouble)*3.6;
                            check2.add(1,speed2);
                            check2.add(2,elevation);
                            check2.add(3,time);

                            obj3.writeObject(check2);
                            obj3.flush();

                        }
                    }
                }

            } catch (IOException e) {
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private static void receiveFile(String fileName) throws Exception{

        int bytes = 0;
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        long size = userInputStream.readLong();  // read file size
        byte[] buffer = new byte[4*1024];
        while (size > 0 && (bytes = userInputStream.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer,0,bytes);
            size -= bytes;      // read upto file size
        }
        fileOutputStream.close();
    }

} 