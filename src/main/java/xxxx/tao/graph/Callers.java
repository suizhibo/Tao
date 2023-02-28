package xxxx.tao.graph;

import java.util.*;

public class Callers {
    private int num;
    private Set<Caller> callerSet;
    private Map<String, Caller> signatureCaller;

    public Callers(){
        this.num = -1;
        this.callerSet = Collections.synchronizedSet(new HashSet<>());
        this.signatureCaller = Collections.synchronizedMap(new HashMap<>());
    }

    public Set<Caller> getAllCaller() {
        return callerSet;
    }

    public void addCaller(Caller caller){
        this.callerSet.add(caller);
        this.signatureCaller.put(caller.getSignature(), caller);
    }

    public int getNum(){
        return this.callerSet.size();
    }

    public Caller getCallerBySignature(String signature){
        return this.signatureCaller.getOrDefault(signature, null);
    }
}
