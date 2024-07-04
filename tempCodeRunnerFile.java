import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;
import java.net.SocketAddress;

class Image implements Serializable{
    int size;
    byte data[];
    Image(int size , byte data[]){
        this.data = data;
        this.size = size;
    }
}
class MessagePacket implements Serializable{
    String message;
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
class Client{
    public static void sendFile(ObjectOutputStream oos , String file , File images[])throws Exception{
        for(int i=0;i<images.length;i++){
            if(images[i].getName() == file){
                System.out.println("Match Found");
                FileInputStream fis = new FileInputStream(images[i]);
                int size = fis.available();
                int x;
                byte img[] = new byte[size];
                int idx = 0;
                while((x = fis.read()) != -1){
                    img[idx++] = (byte)x;
                }
                fis.close();
                oos.writeObject(new MessagePacket(images[i].getName()));
                oos.writeObject(new Image(size , img));
                oos.flush();
                return;
            }
        }
    }
    public static void main(String args[])throws Exception{
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        File f = new File("./images");
        System.out.println(f.getAbsolutePath());
        File images[] = f.listFiles();
        System.out.println("\n\n**********************************************************************************************************\n\n");
        for(int i=0;i<images.length;i++){
            System.out.print(images[i].getName() + "  ");
        }
        System.out.println("\n\n**********************************************************************************************************\n\n");
        Inet4Address serverIP = (Inet4Address) InetAddress.getLocalHost();
        SocketAddress serverSockAddr = new InetSocketAddress(serverIP , 4001);
        Socket socket = new Socket();
        socket.connect(serverSockAddr);
        System.out.println("Connected To Server");
        System.out.println("Trying to Get Object Input Stream");
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        System.out.println("Recieved Object Input Stream");
        System.out.println("Trying to get Object Output Stream");
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        System.out.println("Recieved Object Output Stream");
        
        int count = 100;
        while(count > 0){
            System.out.print("Enter Picture Name = ");
            String file =  in.readLine();
            file = "./images/" + file;
            System.out.println("Sending");
            sendFile(oos , file , images);
            System.out.println("Sent File to Server");
            MessagePacket serverResponse = (MessagePacket)ois.readObject();
            System.out.println(serverResponse.toString());
            count--;
        }
        socket.close();

    }
}