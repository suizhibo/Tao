package xxxx.tao.graph;

import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.JimpleBody;
import soot.jimple.internal.*;
import soot.util.NumberedString;

import java.util.List;

public class MethodVisitor implements Visitor {
    private SootClass sootClass;

    private Edges edges;
    private Callees callees;
    private Callers callers;


    public MethodVisitor(SootClass sootClass, Edges edges, Callers callers, Callees callees) {
        this.sootClass = sootClass;
        this.edges = edges;
        this.callers = callers;
        this.callees = callees;
    }

    @Override
    public void run() {
        try {
            List<SootMethod> sootMethodSet = this.sootClass.getMethods();
            for (SootMethod sootMethod :
                    sootMethodSet) {
                try {
                    JimpleBody body = (JimpleBody) sootMethod.retrieveActiveBody();
                    UnitPatchingChain units = body.getUnits();
                    units.forEach(unit -> {
                        try {
                            visitUnit(sootMethod, unit);
                        } catch (Exception ex) {
                        }
                    });
                } catch (Exception ex) {
                }
            }
        } catch (Exception exception) {
        }
    }

    private void visitUnit(SootMethod sootMethod, Unit unit) {
        InvokeExpr invokeExpr = null;
        int startLine;
        try {
            if (unit instanceof JAssignStmt) {
                JAssignStmt jAssignStmt = (JAssignStmt) unit;
                invokeExpr = (InvokeExpr) jAssignStmt.getRightOpBox().getValue();
            } else if (unit instanceof JInvokeStmt) {
                JInvokeStmt jInvokeStmt = (JInvokeStmt) unit;
                invokeExpr = jInvokeStmt.getInvokeExpr();
            }
        } catch (Exception e) {

        }
        if (invokeExpr != null) {
            startLine = unit.getJavaSourceStartLineNumber();
            Caller caller = this.callers.getCallerBySignature(sootMethod.getSignature());
            if (caller == null) {
                caller = new Caller(this.sootClass, sootMethod, sootMethod.getSignature(), startLine);
            }
            this.callers.addCaller(caller);
            resolve(invokeExpr, caller);
        }
    }

    private void resolve(InvokeExpr invokeExpr, Caller caller) {
        SootMethodRef sootMethodRef = invokeExpr.getMethodRef();
        SootClass declaringClass = sootMethodRef.getDeclaringClass();
        NumberedString subSignature = sootMethodRef.getSubSignature();
        if (invokeExpr instanceof JStaticInvokeExpr) {
            // support lambda
            if(sootMethodRef.getDeclaringClass().toString().contains("$lambda") && sootMethodRef.getName().equals("bootstrap$")){
                SootClass deClass = sootMethodRef.getDeclaringClass();
                deClass.getMethods().forEach(sm->{
                    String methodName = sm.getName();
                    if(!methodName.equals("bootstrap$") && !methodName.equals("<init>")) {
                        Callee callee = this.callees.getCalleeBySignature(sm.getSignature());
                        if (callee == null) {
                            callee = new Callee(deClass, sm, sm.getSignature(), sm.getJavaSourceStartLineNumber());
                        }
                        Edge edge = this.edges.getEdgeByCallerAndCallee(caller, callee, InvokeType.Static);
                        if (edge == null) {
                            edge = new Edge(InvokeType.Static, caller, callee);
                        }
                        this.callees.addCallee(callee);
                        this.edges.addEdge(edge);
                        new MethodVisitor(deClass, this.edges, this.callers, this.callees).run();
                    }
                });
            }else{
                Callee callee = this.callees.getCalleeBySignature(sootMethodRef.getSignature());
                if (callee == null) {
                    callee = new Callee(sootMethodRef.getDeclaringClass(), sootMethodRef.resolve(),
                            sootMethodRef.getSignature(), sootMethodRef.resolve().getJavaSourceStartLineNumber());
                }
                Edge edge = this.edges.getEdgeByCallerAndCallee(caller, callee, InvokeType.Static);
                if (edge == null) {
                    edge = new Edge(InvokeType.Static, caller, callee);
                }
                this.callees.addCallee(callee);
                this.edges.addEdge(edge);
            }
        } else if (invokeExpr instanceof JSpecialInvokeExpr) {
            resolve(caller, declaringClass, subSignature, InvokeType.Special);
        } else if (invokeExpr instanceof JVirtualInvokeExpr) {
            List<SootClass> subClasses = Scene.v().getActiveHierarchy().getSubclassesOf(declaringClass);
            for (SootClass clazz :
                    subClasses) {
                resolve(caller, clazz, subSignature, InvokeType.Virtual);
            }
            resolve(caller, declaringClass, subSignature, InvokeType.Virtual);
        } else if (invokeExpr instanceof JInterfaceInvokeExpr) {
            List<SootClass> subClasses = Scene.v().getActiveHierarchy().getImplementersOf(declaringClass);
            for (SootClass clazz :
                    subClasses) {
                resolve(caller, clazz, subSignature, InvokeType.Interface);
            }
            resolve(caller, declaringClass, subSignature, InvokeType.Interface);
        } else {
            // TODO: dynamic
        }

    }

    private void resolve(Caller caller, SootClass sootClass, NumberedString subSignature, InvokeType invokeType) {
        SootMethod calleeMethod = dispatch(sootClass, subSignature);
        if(calleeMethod == null) return;
        Callee callee = this.callees.getCalleeBySignature(calleeMethod.getSignature());
        if (callee == null) {
            callee = new Callee(calleeMethod.getDeclaringClass(), calleeMethod,
                    calleeMethod.getSignature(), calleeMethod.getJavaSourceStartLineNumber());
        }
        Edge edge = this.edges.getEdgeByCallerAndCallee(caller, callee, invokeType);
        if (edge == null) {
            edge = new Edge(invokeType, caller, callee);
        }
        this.callees.addCallee(callee);
        this.edges.addEdge(edge);
    }

    private SootMethod dispatch(SootClass sootClass, NumberedString subSignature) {
        if (sootClass.isInterface()) {
            return null;
        }
        try {
            SootMethod sootMethod = null;
            try {
                sootMethod = sootClass.getMethod(subSignature);
            }catch (RuntimeException runtimeException){

            }
            if (sootMethod != null && !sootMethod.isAbstract()) {
                return sootMethod;
            }
            SootClass directSuperClass = Scene.v().getActiveHierarchy().getSuperclassesOf(sootClass).get(0); // Direct
            return dispatch(directSuperClass, subSignature);
        } catch (Exception exception) {

        }
        return null;
    }
}
