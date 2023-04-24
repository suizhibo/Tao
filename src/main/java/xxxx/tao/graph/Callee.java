package xxxx.tao.graph;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import soot.SootClass;
import soot.SootMethod;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Callee {
    private SootClass sootClass;
    private SootMethod sootMethod;
    private String signature;
}
