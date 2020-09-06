package sample;

public class GraphLinkAL {
    public GraphNodeAL<?> destNode; //Could also store source node if required
    public int cost; //Other link attributes could be similarly stored
    public GraphLinkAL(GraphNodeAL<?> destNode,int cost) {
        this.destNode=destNode;
        this.cost=cost;
    }
}
