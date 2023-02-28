package xxxx.tao.graph;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Graph {

    private Edges edges;
    private Callees callees;
    private Callers callers;
    private String sink;
    private StringBuffer stringBuffer;

    Set<String> nodes = Collections.synchronizedSet(new HashSet<>());
    Set<String> links = Collections.synchronizedSet(new HashSet<>());

    public Graph(Edges edges, Callers callers, Callees callees, String sink) {
        this.edges = edges;
        this.callers = callers;
        this.callees = callees;
        this.sink = sink;
        stringBuffer = new StringBuffer(
                "digraph CallGraph{\n" +
                        "node [fontsize=\"20\",];\n" +
                        "edge [fontsize=\"10\",];\n");
    }

    public void run() {
        Callee callee = this.callees.getCalleeBySignature(this.sink);
        Set<Edge> edgesByCallee = this.edges.getEdgeByCallee(callee);
        recursion(callee.getSignature(), edgesByCallee);
        drawGraph();
    }

    private void recursion(String parentSignature, Set<Edge> edgeSet){
        nodes.add(parentSignature);
        edgeSet.forEach(edge -> {
            Caller caller = edge.getCaller();
            String currentSignature = caller.getSignature();
            nodes.add(currentSignature);
            links.add(String.format(" %s @ %s @ %s (%d)", currentSignature, parentSignature, edge.getCallerType().toString(), caller.getCallerLineNumber()));
            Callee tempCallee = this.callees.getCalleeBySignature(currentSignature);
            if (tempCallee != null) {
                Set<Edge> temp = this.edges.getEdgeByCallee(tempCallee);
                recursion(tempCallee.getSignature(), temp);
            }
        });
    }

    private void drawGraph() {
        System.out.println("_______________________________________________");
        System.out.println("Sink: " + this.sink);
        System.out.println();
        Map<String, String> nodeNum = new HashMap<>();
        int num = 0;
        for (String node:
             nodes) {
            nodeNum.put(node, String.valueOf(num));
            stringBuffer.append(String.format(" %d[label=\"%s\", shape=\"box\"];\n", num, node));
            num ++;
        }
        for (String link:
             links) {
            String[] links = link.split("@");
            String parent = links[0].trim();
            String child = links[1].trim();
            String label = links[2].trim();
            stringBuffer.append(String.format(" %s -> %s [label=\"%s\"];\n", nodeNum.get(parent), nodeNum.get(child), label));
        }
        stringBuffer.append("}");
        System.out.println(stringBuffer.toString());
        System.out.println("_______________________________________________");
    }
}
