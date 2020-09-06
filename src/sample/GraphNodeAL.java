package sample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GraphNodeAL<T> {
    public T data;
    public int nodeValue=Integer.MAX_VALUE;
    public List<GraphLinkAL> adjList = new ArrayList<>(); //Could use any concrete List implementation


    public GraphNodeAL(T data) {
        this.data=data;
    }

    public void connectToNodeDirected(GraphNodeAL<T> destNode,int cost) {
        adjList.add(new GraphLinkAL(destNode,cost));
    }

    public void connectToNodeUndirected(GraphNodeAL<T> destNode,int cost) {
        adjList.add(new GraphLinkAL(destNode,cost));
        destNode.adjList.add(new GraphLinkAL(this,cost));
    }


    //////////////
    //   DFS    //
    //////////////

    public static void traverseGraphDepthFirstShowingTotalCost(GraphNodeAL<?> from, List<GraphNodeAL<?>> encountered, int totalCost ){
        System.out.println(from.data+" (Total cost of reaching node: "+ totalCost +")");
        if(encountered==null) encountered=new ArrayList<>(); //First node so create new (empty) encountered list
        encountered.add(from);
        //Could sort adjacency list here based on cost – see next slide for more info!
        for(GraphLinkAL adjLink : from.adjList)
            if(!encountered.contains(adjLink.destNode))
                traverseGraphDepthFirstShowingTotalCost(adjLink.destNode,encountered, totalCost+adjLink.cost );
    }

    ///////////////
    //    BFS    //
    ///////////////

    //Interface method to allow just the starting node and the goal node data to match to be specified
    public static <T> List<GraphNodeAL<?>> findPathBreadthFirst(GraphNodeAL<?> startNode, T lookingfor){
        List<List<GraphNodeAL<?>>> agenda=new ArrayList<>(); //Agenda comprised of path lists here!
        List<GraphNodeAL<?>> firstAgendaPath=new ArrayList<>(),resultPath;
        firstAgendaPath.add(startNode);
        agenda.add(firstAgendaPath);
        resultPath=findPathBreadthFirst(agenda,null,lookingfor); //Get single BFS path (will be shortest)
        Collections.reverse(resultPath); //Reverse path (currently has the goal node as the first item)
        return resultPath;
    }


    //Agenda list based breadth-first graph search returning a single reversed path (tail recursive)
    public static <T> List<GraphNodeAL<?>> findPathBreadthFirst(List<List<GraphNodeAL<?>>> agenda,
                                                                List<GraphNodeAL<?>> encountered, T lookingfor){
        if(agenda.isEmpty()) return null; //Search failed
        List<GraphNodeAL<?>> nextPath=agenda.remove(0); //Get first item (next path to consider) off agenda
        GraphNodeAL<?> currentNode=nextPath.get(0); //The first item in the next path is the current node
        if(currentNode.data.equals(lookingfor)) return nextPath; //If that's the goal, we've found our path (so return it)
        if(encountered==null) encountered=new ArrayList<>(); //First node considered in search so create new (empty)
        encountered.add(currentNode); //Record current node as encountered so it isn't revisited again
        for(GraphLinkAL adjNode : currentNode.adjList) //For each adjacent node
            if(!encountered.contains(adjNode.destNode)) { //If it hasn't already been encountered
                List<GraphNodeAL<?>> newPath=new ArrayList<>(nextPath); //Create a new path list as a copy of
                //the current/next path
                newPath.add(0,adjNode.destNode); //And add the adjacent node to the front of the new copy
                agenda.add(newPath); //Add the new path to the end of agenda (end->BFS!)
            }
        return findPathBreadthFirst(agenda,encountered,lookingfor); //Tail call
    }

    //////////////////
    //   DIJKSTRA   //
    /////////////////

    public static <T> CostedPath findCheapestPathDijkstra(GraphNodeAL<?> startNode, T lookingfor){
        CostedPath cp=new CostedPath(); //Create result object for cheapest path
        List<GraphNodeAL<?>> encountered=new ArrayList<>(), unencountered=new ArrayList<>(); //Create encountered/unencountered lists
        startNode.nodeValue=0; //Set the starting node value to zero
        unencountered.add(startNode); //Add the start node as the only value in the unencountered list to start
        GraphNodeAL<?> currentNode;
        do{ //Loop until unencountered list is empty
            currentNode=unencountered.remove(0); //Get the first unencountered node (sorted list, so will have lowest value)
            encountered.add(currentNode); //Record current node in encountered list
            if(currentNode.data.equals(lookingfor)){ //Found goal - assemble path list back to start and return it
                cp.pathList.add(currentNode); //Add the current (goal) node to the result list (only element)
                cp.pathCost=currentNode.nodeValue; //The total cheapest path cost is the node value of the current/goal node
                while(currentNode!=startNode) { //While we're not back to the start node...
                    boolean foundPrevPathNode=false; //Use a flag to identify when the previous path node is identified
                    for(GraphNodeAL<?> n : encountered) { //For each node in the encountered list...
                        for(GraphLinkAL e : n.adjList) //For each edge from that node...
                            if(e.destNode==currentNode && currentNode.nodeValue-e.cost==n.nodeValue){ //If that edge links to the
                                // current node and the difference in node values is the cost of the edge -> found path node!
                                cp.pathList.add(0,n); //Add the identified path node to the front of the result list
                                currentNode=n; //Move the currentNode reference back to the identified path node
                                foundPrevPathNode=true; //Set the flag to break the outer loop
                                break; //We've found the correct previous path node and moved the currentNode reference
                                       //back to it so break the inner loop
                            }
                        if(foundPrevPathNode) break; //We've identified the previous path node, so break the inner loop to continue
                    }
                }
                //Reset the node values for all nodes to (effectively) infinity so we can search again (leave no footprint!)
                for(GraphNodeAL<?> n : encountered) n.nodeValue=Integer.MAX_VALUE;
                for(GraphNodeAL<?> n : unencountered) n.nodeValue=Integer.MAX_VALUE;
                return cp; //The costed (cheapest) path has been assembled, so return it!
            }
            //We're not at the goal node yet, so...
            for(GraphLinkAL e : currentNode.adjList) //For each edge/link from the current node...
                if(!encountered.contains(e.destNode)) { //If the node it leads to has not yet been encountered (i.e. processed)
                    e.destNode.nodeValue=Integer.min(e.destNode.nodeValue, currentNode.nodeValue+e.cost); //Update the node value at the end
                    //of the edge to the minimum of its current value or the total of the current node's value plus the cost of the edge
                    unencountered.add(e.destNode);
                }
            Collections.sort(unencountered,(n1,n2)->n1.nodeValue-n2.nodeValue); //Sort in ascending node value order
        }
        while(!unencountered.isEmpty());
        return null; //No path found, so return null
    }
}