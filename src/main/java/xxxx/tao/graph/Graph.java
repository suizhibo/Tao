package xxxx.tao.graph;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Graph {

    private Edges edges;
    private Callees callees;
    private Callers callers;
    private String sink;
    private AtomicInteger node;
    private StringBuffer stringBuffer;

    private Map<String, Map<String, String>> nodes = Collections.synchronizedMap(new HashMap<>());
    private Set<String> links = Collections.synchronizedSet(new HashSet<>());


    public Graph(Edges edges, Callers callers, Callees callees, String sink) {
        this.edges = edges;
        this.callers = callers;
        this.callees = callees;
        this.sink = sink;
        node = new AtomicInteger(0);
        stringBuffer = new StringBuffer(
                "digraph CallGraph{\n" +
                        "node [fontsize=\"20\",];\n" +
                        "edge [fontsize=\"10\",];\n");
    }

    public void run() {
        Callee callee = this.callees.getCalleeBySignature(this.sink);
        Set<Edge> edgesByCallee = this.edges.getEdgeByCallee(callee);
        recursion(callee, edgesByCallee);
        drawGraph();
    }

    private void recursion(Callee callee, Set<Edge> edgeSet) {
        String signature = callee.getSignature();
        Map<String, String> tempNode = nodes.getOrDefault(signature, null);
        String parentNode;
        if (tempNode == null) {
            parentNode = String.valueOf(node.get());
            String temp = String.format(" %s[label=\"%s:%s\", shape=\"box\"];\n",
                    parentNode, parentNode, signature);
            tempNode = new HashMap<>();
            tempNode.put("label", temp);
            tempNode.put("num", parentNode);
            nodes.put(signature, tempNode);
        } else {
            parentNode = (String) tempNode.get("num");
        }
        edgeSet.forEach(edge -> {
            try {
                Caller caller = edge.getCaller();
                String signatureTemp = caller.getSignature();
                Map<String, String> tempNode1 = nodes.getOrDefault(signatureTemp, null);
                String currentNum;
                if (tempNode1 == null) {
                    currentNum = String.valueOf(node.incrementAndGet());
                    String temp = String.format(" %d[label=\"%d:%s\", shape=\"box\"];\n", node.get(), node.get(), signature);
                    tempNode1 = new HashMap<>();
                    tempNode1.put("label", temp);
                    tempNode1.put("num", String.valueOf(node.get()));
                    nodes.put(signatureTemp, tempNode1);
                }else{
                    currentNum = (String) tempNode1.get("num");}
                links.add(String.format(" %s -> %s [label=\"%s\"];\n", currentNum, parentNode, edge.getCallerType().toString()));
                Callee tempCallee = this.callees.getCalleeBySignature(signatureTemp);
                if (tempCallee != null) {
                    Set<Edge> temp = this.edges.getEdgeByCallee(tempCallee);
                    recursion(tempCallee, temp);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }

    private void drawGraph() {
        nodes.forEach((key, values) -> {
            Map<String, String> entry = values;
            entry.forEach((k, v) -> {
                if (k.equals("label")) {
                    stringBuffer.append(v);
                }
            });
        });
        links.forEach(link -> {
            stringBuffer.append(link);
        });
        stringBuffer.append("}");
        System.out.println(stringBuffer.toString());

    }
}
