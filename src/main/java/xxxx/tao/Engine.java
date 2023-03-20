package xxxx.tao;

import org.apache.commons.cli.ParseException;
import soot.SootClass;
import utils.Command;
import utils.Sinks;
import utils.YamlUtil;
import xxxx.tao.graph.*;

import java.util.Set;
import java.util.concurrent.TimeUnit;

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

    private void run(String[] args) {
        try {
            final long analysisStart = System.currentTimeMillis();
            parseCommand(args);
            loadSinks();
            Edges edges = new Edges();
            Callees callees = new Callees();
            Callers callers = new Callers();
            this.classVisitor = new ClassVisitor(command);
            classVisitor.run();
            Set<SootClass> sootClassSet = classVisitor.getSootClassSet();
            sootClassSet.forEach(sootClass -> {
                MethodVisitor methodVisitor = new MethodVisitor(sootClass, edges, callers, callees);
                methodVisitor.run();
            });
            System.out.println("Edge Num: " + edges.getEdgesNum());
            System.out.println("Caller Num: " + callers.getNum());
            System.out.println("Callee Num: " + callees.getNum());
            sinks.getSinks().forEach((component, sinks) -> {
                sinks.forEach(s -> {
                    try {
                        Graph graph = new Graph(edges, callers, callees, s, component, command.getOutPut());
                        graph.run();
                    } catch (Exception e) {

                    }
                });
            });
            final long analysisDurationSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - analysisStart);
            System.out.println(String.format("%s seconds", analysisDurationSeconds));
        } catch (Exception e) {
        }
    }
}
