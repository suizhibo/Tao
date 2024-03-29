package xxxx.tao.graph;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;
import utils.Command;

import java.io.File;
import java.util.*;

public class ClassVisitor implements Visitor{

    private final String JRE_DIR = System.getProperty("java.home")+ File.separator+
            "lib" + File.separator + "rt.jar";

    static LinkedList<String> excludeList;
    private List<String> libs = new ArrayList<>();
    private List<String> classFilePaths = Collections.synchronizedList(new ArrayList<>());
    private List<String> jarFilePaths = new ArrayList<>();
    private int tempClassPathLength;
    private Command command;

    private Set<SootClass> sootClassSet = new HashSet<>();

    public ClassVisitor(){

    }

    public ClassVisitor(Command command){
        this.command = command;
    }

    private void scanClass(File file){
        if (file.isFile()) {
            String fileName = file.getName();
            if(fileName.endsWith(".class")) {
                String filePath = file.getAbsolutePath();
                classFilePaths.add(filePath);
            }
        }
        else if (file.isDirectory()) {
            for (File f : file.listFiles())
                scanClass(f);
        }
    }

    private void scanJar(File file){
        if (file.isFile()) {
            String fileName = file.getName();
            if(fileName.endsWith(".jar")) {
                String filePath = file.getAbsolutePath();
                jarFilePaths.add(filePath);
            }
        }
        else if (file.isDirectory()) {
            for (File f : file.listFiles())
                scanJar(f);
        }
    }

    private void buildAllSootClass(){
        for (String classFilePath:
                classFilePaths) {
            String path = classFilePath.substring(this.tempClassPathLength);
            String newPath = path.substring(1, path.lastIndexOf("."));
            newPath = newPath.replace("\\", ".");
            SootClass sootClass = Scene.v().loadClassAndSupport(newPath);
            if(!sootClass.isJavaLibraryClass()){
                sootClassSet.add(sootClass);
            }
        }

    }

    private SootClass buildOneSootClass(String classFilePath){
        String path = classFilePath.substring(this.tempClassPathLength);
        String newPath = path.substring(1, path.lastIndexOf("."));
        newPath = newPath.replace("\\", ".");
        SootClass sootClass = Scene.v().loadClassAndSupport(newPath);
        if(!sootClass.isJavaLibraryClass()){
            return sootClass;
        }
        return null;
    }

    private void loadAllClass(){
        String classPath = command.getClassPath();
        List<String> classPaths = new ArrayList<>();
        this.tempClassPathLength = classPath.length();
        scanJar(new File(command.getLibPath()));
        classPaths.add(classPath);
        libs.addAll(jarFilePaths); // add lib path
        libs.add(JRE_DIR);
        excludeJDKLibrary();
        String sootClassPath = String.join(File.pathSeparator, classPaths) + File.pathSeparator +
                String.join(File.pathSeparator, libs);
        Scene.v().setSootClassPath(sootClassPath);
        Options.v().set_process_dir(classPaths);
//        Options.v().set_whole_program(true);
        Options.v().set_keep_line_number(true);
        Options.v().set_app(true);
        scanClass(new File(classPath));
//        Scene.v().loadNecessaryClasses();
        Scene.v().loadBasicClasses();
        Scene.v().loadDynamicClasses();
        buildAllSootClass();
    }

    private static LinkedList<String> excludeList()
    {
        if(excludeList==null)
        {
            excludeList = new LinkedList<String> ();

            excludeList.add("java.*");
            excludeList.add("javax.*");
            excludeList.add("sun.*");
            excludeList.add("sunw.*");
            excludeList.add("com.sun.*");
            excludeList.add("com.ibm.*");
            excludeList.add("com.apple.*");
            excludeList.add("apple.awt.*");
            excludeList.add("jdk.internal.*");
        }
        return excludeList;
    }

    private static void excludeJDKLibrary()
    {
        Options.v().set_exclude(excludeList());
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_allow_phantom_refs(true);
    }

    public Set<SootClass> getSootClassSet() {
        return sootClassSet;
    }


    @Override
    public void run() {
        loadAllClass();
    }

    public SootClass getSootClassByID(int id){
        if(id >= classFilePaths.size() || id < 0)return null;
        return buildOneSootClass(classFilePaths.get(id));
    }

    public int getClassFileSize(){
        return classFilePaths.size();
    }
}
