package xxxx.tao;

import org.apache.commons.cli.ParseException;
import soot.Scene;
import soot.SootClass;
import utils.Command;
import utils.GraphToCSV;
import utils.Sinks;
import utils.YamlUtil;
import xxxx.tao.graph.*;

import java.util.Set;
import java.util.concurrent.*;

public class Engine {

    private Sinks sinks;

    private ClassVisitor classVisitor;
    private Command command = new Command();

    public Engine() {
    }

    public static void main(String[] args) {
        System.out.println("\n" +
                " ___________        __          ______    \n" +
                "(\"     _   \")      /\"\"\\        /    \" \\   \n" +
                " )__/  \\\\__/      /    \\      // ____  \\  \n" +
                "    \\\\_ /        /' /\\  \\    /  /    ) :) \n" +
                "    |.  |       //  __'  \\  (: (____/ //  \n" +
                "    \\:  |      /   /  \\\\  \\  \\        /   \n" +
                "     \\__|     (___/    \\___)  \\\"_____/    \n" +
                "                                          \n");
        Engine engine = new Engine();
        engine.run(args);
    }

    private void parseCommand(String[] args) throws ParseException {
        command.parse(args);
    }

    private void loadSinks() {
        try {
            String sinksPath = command.getSinksPath();
            sinks = (Sinks) YamlUtil.readYaml(sinksPath, Sinks.class);
        } catch (Exception e) {
        }
    }

    public void run(String[] args) {
        try {
            final long analysisStart = System.currentTimeMillis();
            parseCommand(args);
            loadSinks();
            Edges edges = new Edges();
            Callees callees = new Callees();
            Callers callers = new Callers();
            this.classVisitor = new ClassVisitor(command);
            classVisitor.run();
            System.out.println("Start Run CHA.......");
            Set<SootClass> sootClassSet = classVisitor.getSootClassSet();
            sootClassSet.forEach(sootClass -> {
                MethodVisitor methodVisitor = new MethodVisitor(sootClass, edges, callers, callees);
                methodVisitor.run();
            });
            System.out.println("Num: " + Scene.v().getClasses().size());
            System.out.println("Edge Num: " + edges.getEdgesNum());
            System.out.println("Caller Num: " + callers.getNum());
            System.out.println("Callee Num: " + callees.getNum());
            // save call graph
            GraphToCSV.edges2Csv(edges);
            GraphToCSV.callers2Csv(callers);
            GraphToCSV.callees2Csv(callees);
            final long analysisDurationSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - analysisStart);
            System.out.println(String.format("%s seconds", analysisDurationSeconds));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}