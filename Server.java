import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.lang.Runnable;
import java.util.TreeMap;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Serializable;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.OutputStreamWriter;
import java.sql.*;
@SuppressWarnings("unused")
class Server {
    private static int n , m , u[] , v[] , w[];
    private static Road roads = null;

    private static void loadRoadNetwork(String address){
        FileReader fr;
        BufferedReader br;
        fr = null;  br = null;
        try{
            fr = new FileReader(address);
            br = new BufferedReader(fr);
            String arr[] = br.readLine().split("\\s+");
            n = Integer.parseInt(arr[0]);   
            m = Integer.parseInt(arr[1]);
            u = new int[m];
            v = new int[m];
            w = new int[m];
            for(int i=0;i<m;i++){
                arr = br.readLine().split("\\s+");
                u[i] = Integer.parseInt(arr[0]);
                v[i] = Integer.parseInt(arr[1]);
                w[i] = Integer.parseInt(arr[2]);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                if(fr != null){
                    fr.close();
                }
                if(br != null){
                    br.close();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    
    private static MessagePacket getResponseForClient(MessagePacket packet , int u)throws Exception{
        ProcessBuilder pb = new ProcessBuilder("python3" , "./ocr.py");
        Process process = pb.start();
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        bw.write("./" + packet.message);
        bw.newLine();
        bw.flush();
        bw.close();
        String regNo = "";
        String temp;
        while((temp = br.readLine()) != null){
            regNo += temp;
        }
        regNo = modifyRegNo(regNo);
        
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/CarsDatabase" , "root" , "MyDbPassword123!@#");
        System.out.println("CONNECTED TO CARS DATABASE");
        String query = "SELECT * FROM cars WHERE reg_no=\"" + regNo + "\"";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        String response = "";
        System.out.println("QUERYING LICENSE PLATE NO = " + regNo);
        String name , mobNo , email , vehicle , fees;
        vehicle = "Small";
        fees = mobNo = email = vehicle = name = "";
        while(rs.next()){
            String row = rs.getString(1) + "," + rs.getString(2) + "," + rs.getString(3) + "," + rs.getString(4) + "," + rs.getString(5);
            name = rs.getString(2);
            vehicle = rs.getString(3);
            mobNo = rs.getString(4);
            email = rs.getString(5);
            response += row;
            break;
        }
        con.close();
        if(response.length() == 0){
            response = "User Not Found";
        }else{
            if(vehicle.equals("Small")){
                fees = "80";
            }
            if(vehicle.equals("Medium")){
                fees = "150";
            }
            if(vehicle.equals("Large")){
                fees = "250";
            }
            response = "WELCOME TO " + roads.getCityName(u) + "\nAN AMOUNT OF RUPEES " + fees + " WILL BE DEDUCTED FROM " + name + " REGISTRED BANK ACCOUNT\n" + "\nTRANSACTION DETAILS WILL BE SENT TO YOUR MAIL = " + email + "\nTHANKYOU FOR VISITING " + roads.getCityName(u);
        }
        String bestPath = roads.getBestPath(u);
        response += ("\n" + bestPath);
        System.out.println("\n\n" + response);
        return new MessagePacket(response);
    }
    
    private static String modifyRegNo(String s){
        StringBuilder sb =  new StringBuilder();
        for(int i=0;i<s.length();i++){
            char x = s.charAt(i);
            if(x >= '0' && x <= '9'){
                sb.append(x);
            }else if(x >= 'A' && x <= 'Z'){
                sb.append(x);
            }else if(x >= 'a' && x <= 'z'){
                x -= 32;
                sb.append(x);
            }
        }
        return sb.toString();
    }
    
    
    private static void processClient(Socket clientSocket)throws Exception{

        ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
        System.out.println("Created Object Output Stream towards Client");
        ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
        System.out.println("Recieved Object Input Stream from Client");
        FileOutputStream fos = null;
        File f = null;
        try{
            MessagePacket packet = (MessagePacket)ois.readObject();
            Image image = (Image)ois.readObject();
            MessagePacket curLocation = (MessagePacket)ois.readObject();
            int u = Integer.parseInt(curLocation.message);
            f = new File("./" + packet.message);
            if(!f.exists()){
                if(!f.createNewFile()){
                    System.out.println("Could Not Save Client Image");    
                    return;
                }
            }else{
                f.delete();
            }
            fos = new FileOutputStream(f);
            fos.write(image.data);
            fos.flush();
            fos.close();
            MessagePacket clientResponse = getResponseForClient(packet , u);
            oos.writeObject(clientResponse);
            oos.flush();
            oos.close();
            ois.close();
            f.delete();

        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                if(ois != null){
                    ois.close();
                }
                if(oos != null){
                    oos.close();
                }
                if(fos != null){
                    fos.close();
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    public static void main(String args[]) throws Exception{
        Class.forName("com.mysql.cj.jdbc.Driver");
        roads = new Road();
        String pathToRoadData = "./road-data.txt";
        roads.readRoadNetwork(pathToRoadData);
        loadRoadNetwork(pathToRoadData);
        SensorThread sensorThreads[] = new SensorThread[m];
        for(int i=0;i<m;i++){
            sensorThreads[i] = new SensorThread(roads, u[i], v[i]);
        }
        Thread threads[] = new Thread[m];
        for(int i=0;i<m;i++){
            threads[i] = new Thread(sensorThreads[i] , "Sensor " + (i+1));
        }
        for(int i=0;i<m;i++){
            threads[i].start();
        }
        System.out.println("All Sensor Threads Started");
        try{
            ServerSocket serverSocket = new ServerSocket(4001);
            int count = 100;
            while(count-- > 0){
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connected To Client = " + clientSocket);
                processClient(clientSocket);
                clientSocket.close();
                System.out.println("Disconnected to Client");
            }
            serverSocket.close();
            System.out.println("Server Closed");
            //Closing All Sensor Threads
            for(int i=0;i<m;i++){
                sensorThreads[i].stop();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

class Image implements Serializable{
    int size;
    byte data[];
    private static final long serialVersionUID = 12345678L;
    Image(int size , byte arr[]){
        this.size = size;
        this.data = arr;
    }
    Image(){
        this.size = 0;
    }
}
class MessagePacket implements Serializable{
    String message;
    private static final long serialVersionUID = 123456789L;
    MessagePacket(String message){
        this.message = message;
    }
    MessagePacket(){
        this.message = "";
    }
    public String toString(){
        return "MESSAGE = " + this.message + "\n";
    }
}
class Pair implements Comparable<Pair>{
    int first;
    int second;

    Pair(int first, int second) {
        this.first = first;
        this.second = second;
    }

    Pair() {
        this.first = this.second = -1;
    }
    public String toString(){
        return "(" + this.first + " " + this.second + ")";
    }
    public int compareTo(Pair b){
        if (this.first != b.first) {
            return this.first - b.first;
        } else {
            return this.second - b.second;
        }
    }

}
@SuppressWarnings("unchecked")
class Road{
    static private LinkedList<Integer>[] G;
    static private TreeMap<Pair , Integer> traffic;
    static private int count = 1;
    static private TreeMap<Integer , String> city;
    public synchronized String getCityName(int u){
        return city.get(u);
    }
    public void readRoadNetwork(String address) {
        if(count == 0)  return;
        else            count--;
        BufferedReader br = null;
        FileReader fr = null;
        try {
            fr = new FileReader(address);
            br = new BufferedReader(fr);
            String arr[] = br.readLine().split("\\s+");
            int n = Integer.parseInt(arr[0]);
            int m = Integer.parseInt(arr[1]);
            traffic = new TreeMap<Pair , Integer>();
            G = new LinkedList[n + 1];
            city = new TreeMap<Integer , String>();
            for (int i = 0; i <= n; i++) {
                G[i] = new LinkedList<>();
            }
            for (int i = 0; i < m; i++) {
                arr = br.readLine().split("\\s+");
                int u, v, w;
                u = Integer.parseInt(arr[0]);
                v = Integer.parseInt(arr[1]);
                w = Integer.parseInt(arr[2]);
                G[u].add(v);
                traffic.put(new Pair(u , v) , w);
            }
            fr = new FileReader("./cities.txt");
            br = new BufferedReader(fr);
            String temp;
            while((temp = br.readLine()) != null){
                arr = temp.split(",");
                city.put(Integer.parseInt(arr[0]) , arr[1]);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
                if (fr != null)  fr.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    synchronized public void updateEdge(int u , int v , int w){
        {
            traffic.put(new Pair(u , v) , w);
            traffic.put(new Pair(v , u) , w);
        }
    }
    synchronized public String getBestPath(int u){
        int bestDist = (int)2e9;
        String bestDistCity = "";
        int x = -1;
        for(Integer v:G[u]){
            System.out.println(u + " " + v + " " + traffic.get(new Pair(u , v)));
            if(traffic.get(new Pair(u , v)) == null)    continue;
            int w = traffic.get(new Pair(u , v));
            
            if(w < bestDist){
                bestDist = w;
                bestDistCity = city.get(v);
                x = v;
            }
        }
        String bestPath = "The road from " + u + ":" + city.get(u) + " to "  + x + ":" +   bestDistCity + " has least traffic!\nAverage Traffic = " + bestDist + "\n"; 
        return bestPath;
    }
}
class SensorThread implements Runnable{
    private Road roads = null;
    int u;
    int v;
    private volatile boolean  running = true;
    SensorThread(Road G , int u , int v){
        this.roads = G;
        this.u = u;
        this.v = v;
    }
    public static int getCurrentTrafficDensity(){
        return (int) Math.floor(100*Math.random());
    }
    public void run(){
        running = true;
        while(running){
            int curTraffic = getCurrentTrafficDensity();
            roads.updateEdge(u , v , curTraffic);
            roads.updateEdge(v , u , curTraffic);
            try{
                Thread.sleep(10000);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    public void stop(){
        running = false;
    }
}
