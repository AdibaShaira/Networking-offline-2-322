import java.util.ArrayList;
import java.net.*;
import java.io.*;
import java.util.Random;

//Work needed
public class Client {
  public  DataInputStream  input=null;
    public static void main(String[] args) throws InterruptedException {
        NetworkUtility networkUtility = new NetworkUtility("127.0.0.1", 4444);
        System.out.println("Connected to server");
        Object o=networkUtility.read();
        EndDevice end=(EndDevice) o;
        IPAddress ia=end.getIpAddress();
        for(int i=0;i<100;i++){

            System.out.println("i "+i);
            if(i==20){
            Packet p=new Packet("Hi","SHOW_ROUTE",ia,null);
            networkUtility.write(p);
            Object os=networkUtility.read();
            System.out.println("For packet no " +i);
            System.out.println("Hopcount " + (String) os);
          //  os=networkUtility.read();
         //   System.out.println((String) os);
        //    continue;
            }
            else {
                Packet p=new Packet("Hi","",ia,null);
                networkUtility.write(p);
                Object os=networkUtility.read();
                System.out.println("For packet no " +i);
                System.out.println("Hopcount " + (String) os);
               // os=networkUtility.read();
            //    System.out.println((String) os);
             //   continue;
            }

        }
        /**
         * Tasks
         */
        
        /*
        1. Receive EndDevice configuration from server
        2. Receive active client list from server
        3. for(int i=0;i<100;i++)
        4. {
        5.      Generate a random message
        6.      Assign a random receiver from active client list
        7.      if(i==20)
        8.      {
        9.            Send the message and recipient IP address to server and a special request "SHOW_ROUTE"
        10.           Display routing path, hop count and routing table of each router [You need to receive
                            all the required info from the server in response to "SHOW_ROUTE" request]
        11.     }
        12.     else
        13.     {
        14.           Simply send the message and recipient IP address to server.
        15.     }
        16.     If server can successfully send the message, client will get an acknowledgement along with hop count
                    Otherwise, client will get a failure message [dropped packet]
        17. }
        18. Report average number of hops and drop rate
        */
    }
}
