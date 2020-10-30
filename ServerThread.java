

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;


public class ServerThread implements Runnable {

    NetworkUtility networkUtility;
    EndDevice endDevice;
    EndDevice endDest;
    ArrayList<Integer>routingPath=new ArrayList<>();;
    int hopcount=0;

    ServerThread(NetworkUtility networkUtility, EndDevice endDevice) {
        this.networkUtility = networkUtility;
        this.endDevice = endDevice;
        System.out.println("Server Ready for client " + NetworkLayerServer.clientCount);
        NetworkLayerServer.clientCount++;
        new Thread(this).start();
    }

    @Override
    public void run() {
        this.networkUtility.write(this.endDevice);
        for(int i=0;i<100;i++){

            hopcount=0;
            Object o=this.networkUtility.read();
            Packet p=(Packet) o;
            Random random = new Random(System.currentTimeMillis());
            System.out.println("Size of enddevice "+NetworkLayerServer.endDevices.size());
            int r = Math.abs(random.nextInt(NetworkLayerServer.endDevices.size()));
            endDest=NetworkLayerServer.endDevices.get(r);
            System.out.println("I "+i);
           // IPAddress iar=endr.getIpAddress();
            System.out.println("ip of dest "+endDest.getIpAddress().toString());
         //   p.setDestinationIP(endr.getIpAddress());
            System.out.println("I after setting dest "+i);
            boolean sem= deliverPacket(p);
           if(sem==true && (p.getSpecialMessage().equalsIgnoreCase("SHOW_ROUTE"))){
            //  this.networkUtility.write(routingPath);
               String str1 = Integer.toString(hopcount);
               this.networkUtility.write(str1);
               this.networkUtility.write("Successfully sent");
           }
           else if(sem==true && (p.getSpecialMessage().equalsIgnoreCase(""))){
             //  this.networkUtility.write(routingPath.size());
               System.out.println("Hopcount "+hopcount);
               String str1 = Integer.toString(hopcount);
               this.networkUtility.write(str1);
               this.networkUtility.write("Successfully sent");
           }
           else if(sem==false){
               this.networkUtility.write("Sending Failed");
               continue;
           }
           System.out.println("ber hoisi");
        }

    }


    public Boolean deliverPacket(Packet p) {
        IPAddress ips=this.endDevice.getIpAddress();
        IPAddress ipd=this.endDest.getIpAddress();
        System.out.println("Source Ip "+ ips.toString());
        System.out.println("Destination Ip "+ipd.toString());
        int routeridsrc=0;
        int routeriddest=0;
        routeridsrc=NetworkLayerServer.deviceiptoRouterid.get(ips);
        routeriddest=NetworkLayerServer.deviceiptoRouterid.get(ipd);

        System.out.println("source "+routeridsrc);
        System.out.println("dest "+routeriddest);
        routingPath.add(routeridsrc);
    //    this.routingPath.add(routeriddest);
        Router src=NetworkLayerServer.routers.get(routeridsrc-1);
        Router dest=NetworkLayerServer.routers.get(routeriddest-1);
        if(NetworkLayerServer.routers.get(routeridsrc-1).getState()==false){
            return false;
        }
        boolean routcheck=true;
        System.out.println("LOOP SHURU");
        while(routcheck){
            hopcount++;
            System.out.println("dhukse");
            ArrayList<RoutingTableEntry> routingTables=src.getRoutingTable();
            int newrouterid=routeridsrc;
            System.out.println("Routingtablesize "+routingTables.size());
            routeridsrc= routingTables.get(routeriddest-1).getGatewayRouterId();
            System.out.println("Routeridsrc "+routeridsrc);
            //   routingPath.add(routeridsrc);
            if(routeridsrc==-9999){
                return false;
            }
            routingPath.add(routeridsrc);
           src=NetworkLayerServer.routers.get(routeridsrc-1);
            if(NetworkLayerServer.routers.get(routeridsrc-1).getState()==false){

                  routingTables.get(routeridsrc-1).setDistance(Constants.INFINITY);
                  try {
                      NetworkLayerServer.stateChanger.thread.sleep(500);
                  }
                  catch (InterruptedException ie){

                  }
                  System.out.println("dvr er routerid "+newrouterid);
                  NetworkLayerServer.simpleDVR(newrouterid);
                return false;


            }
           else if(NetworkLayerServer.routers.get(newrouterid-1).getRoutingTable().get(routeridsrc-1).getDistance()==Constants.INFINITY){
                NetworkLayerServer.routers.get(newrouterid-1).getRoutingTable().get(routeridsrc-1).setDistance(1);
                try {
                    NetworkLayerServer.stateChanger.thread.sleep(1000);
                }
                catch (InterruptedException ie){

                }
                System.out.println("dvr er routerid else if "+routeridsrc);
                NetworkLayerServer.simpleDVR(routeridsrc);
                NetworkLayerServer.stateChanger.thread.start();


            }
           if(routeridsrc==routeriddest){
               routcheck=false;
           }
        }
        return true;

    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj); //To change body of generated methods, choose Tools | Templates.
    }
}
