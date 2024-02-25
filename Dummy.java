
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Dummy{

    int port;
    String directory = null;
    static DataOutputStream dataOutputStream;
    static DataInputStream dataInputStream;

    Dummy(int port, String directory){
        this.port = port;
        this.directory = directory;
    }
    void sendandreceive() throws Exception {
        //send gpx file to master
        Socket socket = new Socket("localhost",port);
        dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        dataInputStream = new DataInputStream(socket.getInputStream());
        sendFile(directory);

        //receive final results from masterReduce

        ObjectInputStream obj = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        ArrayList<Double> listOfData = (ArrayList<Double>) obj.readObject();
        System.out.println("information recieved");
        System.out.println("Total Distance: " + listOfData.get(0) + " meters");
        System.out.println("Average Speed: " + listOfData.get(1) +" km/h");
        System.out.println("Total Elevation: " +listOfData.get(2)+ " meters");
        System.out.println("Total Time: "+listOfData.get(3)+ " seconds");
        File file = new File("results.txt");
        FileWriter myWriter = new FileWriter("results.txt");
        myWriter.write("Total Distance: " + listOfData.get(0) + " meters");
        myWriter.write("Average Speed: " + listOfData.get(1) +" km/h");
        myWriter.write("Total Elevation: " +listOfData.get(2)+ " meters");
        myWriter.write("Total Time: "+listOfData.get(3)+ " seconds");
        myWriter.close();
        file.createNewFile();
        dataOutputStream.close();
    }
    private static void sendFile(String path) throws Exception{
        int bytes = 0;
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);
        dataOutputStream.writeLong(file.length());
        byte[] buffer = new byte[4*1024];
        while ((bytes=fileInputStream.read(buffer))!=-1){
            dataOutputStream.write(buffer,0,bytes);
            dataOutputStream.flush();
        }
        fileInputStream.close();
    }
    public static void main(String[] args) throws Exception {
        String directrory = "gpxs/route1.gpx";
        User user = new User(10090, directrory);
        user.sendandreceive();
    }

}

