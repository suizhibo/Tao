package xxxx.tao.graph;

import utils.Utils;

import java.util.*;

public class Graph {

    private Edges edges;
    private Callees callees;
    private Callers callers;
    private String sink;
    private String vul;
    private String outPath;
    private StringBuffer stringBuffer;

    Set<String> nodes = Collections.synchronizedSet(new HashSet<>());
    Set<String> links = Collections.synchronizedSet(new HashSet<>());

    public Graph(Edges edges, Callers callers, Callees callees, String sink, String vul, String outPath) {
        this.edges = edges;
        this.callers = callers;
        this.callees = callees;
        this.sink = sink;
        this.vul = vul;
        this.outPath = outPath;
        stringBuffer = new StringBuffer(
                "digraph CallGraph{\n" +
                        "node [fontsize=\"20\",];\n" +
                        "edge [fontsize=\"10\",];\n");

        Utils.mkDir(outPath);
    }

    public void run() {
        int count = 0;
        Callee callee = this.callees.getCalleeBySignature(this.sink);
        Set<Edge> edgesByCallee = this.edges.getEdgeByCallee(callee);
        recursion(callee.getSignature(), edgesByCallee, count);
        drawGraph();
    }

    private void recursion(String parentSignature, Set<Edge> edgeSet, int num){
        nodes.add(parentSignature);
        if(num > 20) return;
        edgeSet.forEach(edge -> {
            Caller caller = edge.getCaller();
            String currentSignature = caller.getSignature();
            nodes.add(currentSignature);
            links.add(String.format(" %s @ %s @ %s", currentSignature, parentSignature, edge.getCallerType().toString()));
            Callee tempCallee = this.callees.getCalleeBySignature(currentSignature);
            if (tempCallee != null) {
                Set<Edge> temp = this.edges.getEdgeByCallee(tempCallee);
                if(temp.size() > 0){
                    recursion(tempCallee.getSignature(), temp, num + 1);
                }
            }
        });
    }

    private void drawGraph(){
        System.out.println("_______________________________________________");
        System.out.println(String.format("Vul: %s\nSink: %s", this.vul, this.sink));
        System.out.println();
        Map<String, String> nodeNum = new HashMap<>();
        Map<String, String> numToNode= new HashMap<>();
        Set<String> entries = new HashSet<>();
        Set<String> children = new HashSet<>();
        int num = 0;
        for (String node:
             nodes) {
            nodeNum.put(node, String.valueOf(num));
            numToNode.put(String.valueOf(num), node);
            entries.add(String.valueOf(num));
            stringBuffer.append(String.format(" %d[label=\"%s\", shape=\"box\"];\n", num, node));
            num ++;
        }
        for (String link:
             links) {
            String[] links = link.split("@");
            String parent = links[0].trim();
            String child = links[1].trim();
            String label = links[2].trim();
            String parentNum = nodeNum.get(parent);
            String childNum = nodeNum.get(child);
            children.add(childNum);
            stringBuffer.append(String.format(" %s -> %s [label=\"%s\"];\n", parentNum, childNum, label));
        }
        stringBuffer.append("}");
        System.out.println(stringBuffer.toString());
        System.out.println("_______________________________________________");

        // 保存结果
//        Map<String, Object> result = new HashMap<>();
//        String filePath = this.outPath + File.separator + this.vul + ".json";
//        entries.removeAll(children);
//        Set<String> entriesSignature = new HashSet<>();
//        entries.forEach(entry ->{
//            entriesSignature.add(numToNode.get(entry));
//
//        });
//        result.put("dot", stringBuffer.toString());
//        result.put("vul", this.vul);
//        result.put("entries", entriesSignature);
//        JSONObject jsonObject = new JSONObject(result);
//        try {
//            Utils.fileWriter(filePath, jsonObject.toString());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }
}
