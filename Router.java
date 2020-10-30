//Work needed
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Router {
    private int routerId;
    private int numberOfInterfaces;
    private ArrayList<IPAddress> interfaceAddresses;//list of IP address of all interfaces of the router
    private ArrayList<RoutingTableEntry> routingTable;//used to implement DVR
    private ArrayList<Integer> neighborRouterIDs;//Contains both "UP" and "DOWN" state routers
    private Boolean state;//true represents "UP" state and false is for "DOWN" state
    private Map<Integer, IPAddress> gatewayIDtoIP;
    private Map<Integer,ArrayList> neighborsmap;
    private int[][]  dist_arr = new int[NetworkLayerServer.routers.size()][NetworkLayerServer.routers.size()];
    private int check=0;
    private int conv=0;
    private int sfuconv=0;


    public Router() {
        interfaceAddresses = new ArrayList<>();
        routingTable = new ArrayList<>();
        neighborRouterIDs = new ArrayList<>();

        /**
         * 80% Probability that the router is up
         */
        state=true;
        Random random = new Random();
        double p = random.nextDouble();
        if (p < 0.80) state = true;
        else state = false;

        numberOfInterfaces = 0;
    }

    public Router(int routerId, ArrayList<Integer> neighborRouters, ArrayList<IPAddress> interfaceAddresses, Map<Integer, IPAddress> gatewayIDtoIP) {
        this.routerId = routerId;
        this.interfaceAddresses = interfaceAddresses;
        this.neighborRouterIDs = neighborRouters;
        this.gatewayIDtoIP = gatewayIDtoIP;
        routingTable = new ArrayList<>();


        /**
         * 80% Probability that the router is up
         */
        state=true;
        Random random = new Random();
        double p = random.nextDouble();
        if (p < 0.80) state = true;
        else state = false;

        numberOfInterfaces = interfaceAddresses.size();
    }

    @Override
    public String toString() {
        String string = "";
        string += "Router ID: " + routerId + "\n" + "Interfaces: \n";
        for (int i = 0; i < numberOfInterfaces; i++) {
            string += interfaceAddresses.get(i).getString() + "\t";
        }
        string += "\n" + "Neighbors: \n";
        for (int i = 0; i < neighborRouterIDs.size(); i++) {
            string += neighborRouterIDs.get(i) + "\t";
        }
        return string;
    }


    /**
     * Initialize the distance(hop count) for each router.
     * for itself, distance=0; for any connected router with state=true, distance=1; otherwise distance=Constants.INFTY;
     */
    public void initiateRoutingTable() {

        for (Router neighbour : NetworkLayerServer.routers) {
        //    System.out.println("neighbour router size "+neighborRouterIDs.size());
            check=0;
            if (neighbour.routerId == routerId) {
                RoutingTableEntry re = new RoutingTableEntry(routerId, 0, routerId);
                routingTable.add(re);
                check=1;

            }
            else{
            for (int i = 0; i < neighborRouterIDs.size(); i++) {
                if (neighbour.routerId == neighborRouterIDs.get(i) && NetworkLayerServer.routerMap.get(neighbour.routerId).state) {


                            RoutingTableEntry re = new RoutingTableEntry(neighbour.routerId, 1, neighbour.routerId);
                            routingTable.add(re);
                            check=1;
                            break;


                    }
                }

            }
            if(check==0)
            {  RoutingTableEntry re = new RoutingTableEntry(neighbour.routerId, Constants.INFINITY, -9999);
            routingTable.add(re);}
        }


    }

    /**
     * Delete all the routingTableEntry
     */
    public void clearRoutingTable() {

     for(int i=0;i<routingTable.size();i++){
         routingTable.get(i).setDistance(Constants.INFINITY);
         routingTable.get(i).setGatewayRouterId(-9999);
     }
    }

    /**
     * Update the routing table for this router using the entries of Router neighbor
     *
     * @param neighbor
     */
    public boolean updateRoutingTable(Router neighbor) {
        conv=0;
        for (Router newneighbour : NetworkLayerServer.routers) {
            check=0;
            //dhore nisi 1,2,3,4 emon kore routerid thakbe,tahole 1 no ta ase ashole 0 no pos e
            System.out.println("newneighbour router id "+((newneighbour.routerId)-1));
            System.out.println("Size of routing table "+neighbor.routingTable.size());
           double dist1=neighbor.routingTable.get((newneighbour.routerId)-1).getDistance();
           double dist2=routingTable.get((neighbor.routerId)-1).getDistance();
           if(routingTable.get((newneighbour.routerId)-1).getDistance()>((neighbor.routingTable.get((newneighbour.routerId)-1).getDistance())+(routingTable.get((neighbor.routerId)-1).getDistance()))){
               routingTable.get((newneighbour.routerId)-1).setDistance(dist1+dist2);
               conv=1;
               for (int i = 0; i < neighborRouterIDs.size(); i++){
                   if(neighbor.routerId==neighborRouterIDs.get(i)){
                       routingTable.get((newneighbour.routerId)-1).setGatewayRouterId(neighbor.routerId);
                       check=1;

                       break;
                   }
               }
               if(check==0){
                   routingTable.get((newneighbour.routerId)-1).setGatewayRouterId(routingTable.get((neighbor.routerId)-1).getGatewayRouterId());
               }

           }
        }

      if(conv==1)
      { return true;}
      else{
          return false;
      }
    }

    public boolean sfupdateRoutingTable(Router neighbor) {
        sfuconv=0;
        for (Router newneighbour : NetworkLayerServer.routers){
            check=0;
            double dist1=neighbor.routingTable.get(newneighbour.routerId-1).getDistance();
            double dist2=routingTable.get(neighbor.routerId-1).getDistance();
            double dist=routingTable.get(newneighbour.routerId-1).getDistance();
           // System.out.println("x "+this.routerId);
         //   System.out.println("y "+newneighbour.getRouterId());
         //   System.out.println("z "+neighbor.routerId);
            if((neighbor.routerId==routingTable.get((newneighbour.routerId)-1).getGatewayRouterId())||(this.routerId!=neighbor.routingTable.get((newneighbour.routerId)-1).getGatewayRouterId())&&(dist>dist1+dist2)){
                routingTable.get((newneighbour.routerId)-1).setDistance(dist1+dist2);
                sfuconv=1;
                for (int i = 0; i < neighborRouterIDs.size(); i++) {
                    if (neighbor.routerId == neighborRouterIDs.get(i)) {
                        routingTable.get((newneighbour.routerId) - 1).setGatewayRouterId(neighbor.routerId);
                        check = 1;
                        break;
                    }
                }
                if(check==0){
                    routingTable.get((newneighbour.routerId)-1).setGatewayRouterId(routingTable.get((neighbor.routerId)-1).getGatewayRouterId());
                }
            }

        }
        if(sfuconv==1){
        return true;
        }
        else{
            return false;
        }
    }

    /**
     * If the state was up, down it; if state was down, up it
     */
    public void revertState() {
        state = !state;
        if(state) { initiateRoutingTable(); }
        else { clearRoutingTable(); }
    }

    public int getRouterId() {
        return routerId;
    }

    public void setRouterId(int routerId) {
        this.routerId = routerId;
    }

    public int getNumberOfInterfaces() {
        return numberOfInterfaces;
    }

    public void setNumberOfInterfaces(int numberOfInterfaces) {
        this.numberOfInterfaces = numberOfInterfaces;
    }

    public ArrayList<IPAddress> getInterfaceAddresses() {
        return interfaceAddresses;
    }

    public void setInterfaceAddresses(ArrayList<IPAddress> interfaceAddresses) {
        this.interfaceAddresses = interfaceAddresses;
        numberOfInterfaces = interfaceAddresses.size();
    }

    public ArrayList<RoutingTableEntry> getRoutingTable() {
        return routingTable;
    }

    public void addRoutingTableEntry(RoutingTableEntry entry) {
        this.routingTable.add(entry);
    }

    public ArrayList<Integer> getNeighborRouterIDs() {
        return neighborRouterIDs;
    }

    public void setNeighborRouterIDs(ArrayList<Integer> neighborRouterIDs) { this.neighborRouterIDs = neighborRouterIDs; }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public Map<Integer, IPAddress> getGatewayIDtoIP() { return gatewayIDtoIP; }

    public void printRoutingTable() {
        System.out.println("Router " + routerId);
        System.out.println("DestID Distance Nexthop");
        for (RoutingTableEntry routingTableEntry : routingTable) {
            System.out.println(routingTableEntry.getRouterId() + " " + routingTableEntry.getDistance() + " " + routingTableEntry.getGatewayRouterId());
        }
        System.out.println("-----------------------");
    }
    public String strRoutingTable() {
        String string = "Router" + routerId + "\n";
        string += "DestID Distance Nexthop\n";
        for (RoutingTableEntry routingTableEntry : routingTable) {
            string += routingTableEntry.getRouterId() + " " + routingTableEntry.getDistance() + " " + routingTableEntry.getGatewayRouterId() + "\n";
        }

        string += "-----------------------\n";
        return string;
    }

}
