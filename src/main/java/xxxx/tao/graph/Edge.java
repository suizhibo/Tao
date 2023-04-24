package xxxx.tao.graph;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Edge {
    private InvokeType callerType;
    private int callerLineNumber;
    private Caller caller;
    private Callee callee;

    public String toString(){
        return caller.getSignature() + "--" + callerType.toString() +  "("+ callerLineNumber+")-->" + callee.getSignature();
    }

}
