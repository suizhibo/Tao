package xxxx.tao.graph;

import java.util.*;

public class Callees {
    private int num;
    private Set<Callee> calleeSet;
    private Map<String, Callee> signatureCallee;

    public Callees(){
        this.num = -1;
        this.calleeSet = Collections.synchronizedSet(new HashSet<>());
        this.signatureCallee = Collections.synchronizedMap(new HashMap<>());
    }

    public Set<Callee> getAllCallee() {
        return calleeSet;
    }

    public void addCallee(Callee callee){
        this.calleeSet.add(callee);
        this.signatureCallee.put(callee.getSignature(), callee);
    }

    public int getNum(){
        return this.calleeSet.size();
    }

    public Callee getCalleeBySignature(String signature){
        return this.signatureCallee.getOrDefault(signature, null);
    }
}
