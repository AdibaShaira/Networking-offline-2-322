//import sun.nio.cs.ext.MacArabic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

//Work needed
public class NetworkLayerServer {

    static int clientCount = 0;
    static ArrayList<Router> routers = new ArrayList<>();
    static RouterStateChanger stateChanger = null;
    static Map<IPAddress,Integer> clientInterfaces = new HashMap<>(); //Each map entry represents number of client end devices connected to the interface
    static Map<IPAddress, EndDevice> endDeviceMap = new HashMap<>();
    static ArrayList<EndDevice> endDevices = new ArrayList<>();
    static Map<Integer, Integer> deviceIDtoRouterID = new HashMap<>();
    static Map<IPAddress, Integer> interfacetoRouterID = new HashMap<>();
    static Map<Integer, Router> routerMap = new HashMap<>();
    static  Map<IPAddress,Integer>deviceiptoRouterid=new HashMap<>();
    private static boolean convergence=true;
    private static int check=0;

    public static void main(String[] args) {

        //Task: Maintain an active client list
   /*     readTopology();
        initRoutingTables();
     //   printRouters();
        DVR(1); //Update routing table using distance vector routing until convergence
        convergence=true;
        printRouters();
      //  simpleDVR(1);
    //    printRouters();*/
       ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(4444);
        } catch (IOException ex) {
            Logger.getLogger(NetworkLayerServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Server Ready: " + serverSocket.getInetAddress().getHostAddress());
        System.out.println("Creating router topology");

        readTopology();
        initRoutingTables(); //Initialize routing tables for all routers
        printRouters();
        System.out.println("DVR....");
        DVR(2); //Update routing table using distance vector routing until convergence
        convergence=true;
        printRouters();
        System.out.println("SIMPLE_DVR.....");
        simpleDVR(2);
        printRouters();
        stateChanger = new RouterStateChanger();//Starts a new thread which turns on/off routers randomly depending on parameter Constants.LAMBDA

        while(true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Client" + (clientCount + 1) + " attempted to connect");
                EndDevice endDevice = getClientDeviceSetup();
                clientCount++;
                endDevices.add(endDevice);
                endDeviceMap.put(endDevice.getIpAddress(),endDevice);
                new ServerThread(new NetworkUtility(socket), endDevice);
            } catch (IOException ex) {
                Logger.getLogger(NetworkLayerServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void initRoutingTables() {
        for (Router router : routers) {
            router.initiateRoutingTable();
        }
    }

    public static synchronized void DVR(int startingRouterId) {


    int itr=0;
        while(convergence)
        {      check=0;
               itr++;
          //     System.out.println("itr "+itr);
            ArrayList<RoutingTableEntry>re=routers.get(startingRouterId-1).getRoutingTable();
            ArrayList<Integer>neighbourids=routers.get(startingRouterId-1).getNeighborRouterIDs();
            for(int i=0;i<neighbourids.size();i++){
                if(routers.get((neighbourids.get(i))-1).getState()){
                    if(routers.get(neighbourids.get(i)-1).sfupdateRoutingTable(routers.get(startingRouterId-1))) {
                        check = 1;
                    }
                }
            }
          //  System.out.println(strrouters());
           // System.out.println("check "+check);
            for(int i=0;i<routers.size();i++){
                if(i!=startingRouterId-1){
                    ArrayList<RoutingTableEntry>ret=routers.get(i).getRoutingTable();
                    ArrayList<Integer>neighbouridsforloop=routers.get(i).getNeighborRouterIDs();
                   // System.out.println("size "+neighbouridsforloop.size() +"i "+ i);
                   // int len=neighbouridsforloop.size();
                    int len=0;


                    len=neighbouridsforloop.size();

                    for(int k=0;k<len;k++){
                    //    System.out.println("k"+k);
                     //   System.out.println("value of j "+k+"router id "+routers.get(neighbouridsforloop.get(k)-1).getRouterId());
                     boolean l=   routers.get(neighbouridsforloop.get(k)-1).getState();
                     if(l==true){
                       boolean f=  routers.get(neighbouridsforloop.get(k)-1).sfupdateRoutingTable(routers.get(i));
                       if(f==true){
                           check=1;
                       }
                     }


                    }


                }
            }
       //     System.out.println(strrouters());
            if(check==1){
                convergence=true;
               // System.out.println("dhukse");

            }
            else{
                convergence=false;
                System.out.println("dhukse");
            }
            if(itr==Constants.INFINITY){
                break;
            }

        }

    }

    public static synchronized void simpleDVR(int startingRouterId) {
        int itr=0;
        while(convergence)
        {   check=0;
            itr++;
          //  System.out.println("itr "+itr);
            ArrayList<RoutingTableEntry>re=routers.get(startingRouterId-1).getRoutingTable();
            ArrayList<Integer>neighbourids=routers.get(startingRouterId-1).getNeighborRouterIDs();
            for(int i=0;i<neighbourids.size();i++){
                if(routers.get((neighbourids.get(i))-1).getState()) {
                    if(routers.get(neighbourids.get(i) - 1).sfupdateRoutingTable(routers.get(startingRouterId - 1))){
                        check = 1;
                    }
                }
            }
            for(int i=0;i<routers.size();i++){
                if(i!=startingRouterId-1){
                    ArrayList<RoutingTableEntry>ret=routers.get(i).getRoutingTable();
                    ArrayList<Integer>neighbouridsforloop=routers.get(i).getNeighborRouterIDs();
                 //   System.out.println("size "+neighbouridsforloop.size() +"i "+ i);
                    int len=neighbouridsforloop.size();
                    for(int k=0;k<len;k++){
                      //  System.out.println("k"+k);
                    //    System.out.println("value of j "+k+"router id "+routers.get(neighbouridsforloop.get(k)-1).getRouterId());
                        boolean l=   routers.get(neighbouridsforloop.get(k)-1).getState();
                        if(l==true){
                            boolean f=  routers.get(neighbouridsforloop.get(k)-1).updateRoutingTable(routers.get(i));
                            if(f==true){
                                check=1;
                            }
                        }


                    }

                }
            }
            if(check==1){
                convergence=true;


            }
            else{
                convergence=false;
            }
            if(itr==Constants.INFINITY){
                System.out.println("dhukse");
                break;
            }

        }


    }

    public static EndDevice getClientDeviceSetup() {
        Random random = new Random(System.currentTimeMillis());
        int r = Math.abs(random.nextInt(clientInterfaces.size()));

        System.out.println("Size: " + clientInterfaces.size() + "\n" + r);

        IPAddress ip = null;
        IPAddress gateway = null;

        int i = 0;
        for (Map.Entry<IPAddress, Integer> entry : clientInterfaces.entrySet()) {
            IPAddress key = entry.getKey();
            //key ta hoche kon ip address client tar
            Integer value = entry.getValue();
            if(i == r) {
                gateway = key;
                ip = new IPAddress(gateway.getBytes()[0] + "." + gateway.getBytes()[1] + "." + gateway.getBytes()[2] + "." + (value+2));
                value++;
                clientInterfaces.put(key, value);
                deviceIDtoRouterID.put(endDevices.size(), interfacetoRouterID.get(key));
                deviceiptoRouterid.put(ip,interfacetoRouterID.get(key));
                break;
            }
            i++;
        }

        EndDevice device = new EndDevice(ip, gateway, endDevices.size());

        System.out.println("Device : " + ip + "::::" + gateway);
        return device;
    }

    public static void printRouters() {
        for(int i = 0; i < routers.size(); i++) {
            System.out.println("------------------\n" + routers.get(i));
        }
        System.out.println(strrouters());
    }

    public static String strrouters() {
        String string = "";
        for (int i = 0; i < routers.size(); i++) {
            string += "\n------------------\n" + routers.get(i).strRoutingTable();
        }
        string += "\n\n";
        return string;
    }

    public static void readTopology() {
        Scanner inputFile = null;
        try {
            inputFile = new Scanner(new File("topology.txt"));
            //skip first 27 lines
            int skipLines = 27;
            for(int i = 0; i < skipLines; i++) {
                inputFile.nextLine();
            }

            //start reading contents
            while(inputFile.hasNext()) {
                inputFile.nextLine();
                int routerId;
                ArrayList<Integer> neighborRouters = new ArrayList<>();
                ArrayList<IPAddress> interfaceAddrs = new ArrayList<>();
                Map<Integer, IPAddress> interfaceIDtoIP = new HashMap<>();

                routerId = inputFile.nextInt();

                int count = inputFile.nextInt();
                for(int i = 0; i < count; i++) {
                    neighborRouters.add(inputFile.nextInt());
                }
                count = inputFile.nextInt();
                inputFile.nextLine();

                for(int i = 0; i < count; i++) {
                    String string = inputFile.nextLine();
                    IPAddress ipAddress = new IPAddress(string);
                    interfaceAddrs.add(ipAddress);
                    interfacetoRouterID.put(ipAddress, routerId);

                    /**
                     * First interface is always client interface
                     */
                    if(i == 0) {
                        //client interface is not connected to any end device yet
                        clientInterfaces.put(ipAddress, 0);
                    }
                    else {
                        interfaceIDtoIP.put(neighborRouters.get(i - 1), ipAddress);
                    }
                }
                Router router = new Router(routerId, neighborRouters, interfaceAddrs, interfaceIDtoIP);
                routers.add(router);
                routerMap.put(routerId, router);
            }


        } catch (FileNotFoundException ex) {
            Logger.getLogger(NetworkLayerServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
