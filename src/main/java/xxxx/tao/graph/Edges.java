package xxxx.tao.graph;

import java.util.*;

public class Edges {
    private int edgesNum;
    private Set<Edge> edges;
    private Map<String, Edge> edgeMap;
    private Map<Caller, Set<Edge>> callerEdgeMap;
    private Map<Callee, Set<Edge>> calleeEdgeMap;

    public Edges() {
        this.edgesNum = -1;
        this.edges = Collections.synchronizedSet(new HashSet<>());
        this.calleeEdgeMap = Collections.synchronizedMap(new HashMap<>());
        this.callerEdgeMap = Collections.synchronizedMap(new HashMap<>());
        this.edgeMap = Collections.synchronizedMap(new HashMap<>());
    }

    public int getEdgesNum() {
        return this.edges.size();
    }

    public Set<Edge> getAllEdges() {
        return edges;
    }

    public void addEdge(Edge edge){
        Callee callee = edge.getCallee();
        Caller caller = edge.getCaller();
        InvokeType invokeType = edge.getCallerType();
        String flag = caller.getSignature() + callee.getSignature() + invokeType.toString();
        if(this.edgeMap.getOrDefault(flag, null) != null){
            return;
        }
        this.edgeMap.put(flag, edge);
        this.edges.add(edge);
        synchronized (this){
            this.addEdgeByCaller(caller, edge);
            this.addEdgeByCallee(callee, edge);
        }
    }

    public Set<Edge> getEdgeByCaller(Caller caller){
        return this.callerEdgeMap.getOrDefault(caller, new HashSet<>());
    }

    private void addEdgeByCaller(Caller caller, Edge edge){
        Set<Edge> tempEdges = this.callerEdgeMap.getOrDefault(caller, new HashSet<>());
        tempEdges.add(edge);
        this.callerEdgeMap.put(caller, tempEdges);
    }

    public Set<Edge> getEdgeByCallee(Callee callee){
        return this.calleeEdgeMap.getOrDefault(callee, new HashSet<>());
    }

    private void addEdgeByCallee(Callee callee, Edge edge){
        Set<Edge> tempEdges = this.calleeEdgeMap.getOrDefault(callee, new HashSet<>());
        tempEdges.add(edge);
        this.calleeEdgeMap.put(callee, tempEdges);
    }

    public Edge getEdgeByCallerAndCallee(Caller caller, Callee callee, InvokeType invokeType){
        synchronized (this){
            String flag = caller.getSignature() + callee.getSignature() + invokeType.toString();
            return this.edgeMap.getOrDefault(flag, null);
        }

    }

}
