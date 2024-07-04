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
import java.io.InputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;

class Image implements Serializable{
    int size;
    byte data[];
    private static final long serialVersionUID = 12345678L;
    Image(int size , byte data[]){
        this.data = data;
        this.size = size;
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
class Client{
    public static void sendFile(ObjectOutputStream oos , String file , File images[])throws Exception{
        for(int i=0;i<images.length;i++){
            String curFile = "./images/" + images[i].getName();
            if(curFile.equals(file)){
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
                System.out.println("SENT FILE TO SERVER");
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
        //System.out.println("\n\n**********************************************************************************************************\n\n");
        // for(int i=0;i<images.length;i++){
        //     System.out.print(images[i].getName() + "  ");
        // }
        //System.out.println("\n\n**********************************************************************************************************\n\n");
        Inet4Address serverIP = (Inet4Address) InetAddress.getLocalHost();
        //172.20.140.223
        byte addr[] = {(byte)172 , (byte)20 , (byte) 140 , (byte) 223};
        serverIP = (Inet4Address) InetAddress.getByAddress(addr);
        SocketAddress serverSockAddr = new InetSocketAddress(serverIP , 4001);
        Socket socket = new Socket();
        socket.connect(serverSockAddr);
        System.out.println("Connected To Server");
        InputStream is = socket.getInputStream();
        ObjectInputStream ois = new ObjectInputStream(is);
        System.out.println("Recieved Object Input Stream");
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        System.out.println("Recieved Object Output Stream");
        
        int count = 1;
        while(count > 0){
            System.out.print("Enter Picture Name = ");
            String file =  in.readLine();
            file = "./images/" + file;
            System.out.println("SENDING FILE TO SERVER");
            sendFile(oos , file , images);
            System.out.println("Enter the current Location of Client");
            String curLocation = in.readLine();
            oos.writeObject(new MessagePacket(curLocation));
            MessagePacket serverResponse = (MessagePacket)ois.readObject();
            System.out.println("\n\n\n\n" + serverResponse.toString());
            count--;
        }
        socket.close();

    }
}