import java.sql.*;
import java.io.*;

public class Temp {
    public static void main(String args[])throws Exception{
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/CarsDatabase" , "root" , "MyDbPassword123!@#");
        System.out.println(con.toString());
        System.out.println(con.getClientInfo());
        // BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("./plates.txt")));
        // String cur = null;
        // PreparedStatement stmt =  con.prepareStatement("insert into cars  values (?,?,?,?,?)");
        // while((cur = br.readLine()) != null){
        //     String arr[] = cur.split(",");
        //     System.out.println(arr.length);
        //     for(int i=0;i<5;i++)    stmt.setString(i+1 , arr[i]);
        //     int val = stmt.executeUpdate();
        //     System.out.println(cur);
        // }
        con.close();
    }
}
