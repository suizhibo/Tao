package xxxx.tao.graph;

import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.JimpleBody;
import soot.jimple.internal.*;
import soot.util.NumberedString;

import java.util.Collections;
import java.util.List;

public class MethodVisitor implements Visitor {
    private SootClass sootClass;

    private Edges edges = new Edges();
    private Callees callees = new Callees();
    private Callers callers = new Callers();


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
            SootClass declaringClass = sootMethodRef.getDeclaringClass();
            NumberedString subSignature = sootMethodRef.getSubSignature();
            SootMethod calleeMethod = dispatch(declaringClass, subSignature);
            if(calleeMethod == null) return;
            Callee callee = this.callees.getCalleeBySignature(calleeMethod.getSignature());
            if (callee == null) {
                callee = new Callee(calleeMethod.getDeclaringClass(), calleeMethod,
                        calleeMethod.getSignature(), calleeMethod.getJavaSourceStartLineNumber());
            }
            this.callees.addCallee(callee);
            Edge edge = this.edges.getEdgeByCallerAndCallee(caller, callee, InvokeType.Special);
            if (edge == null) {
                edge = new Edge(InvokeType.Special, caller, callee);
            }
            this.edges.addEdge(edge);
        } else if (invokeExpr instanceof JVirtualInvokeExpr) {
            SootClass declaringClass = sootMethodRef.getDeclaringClass();
            NumberedString subSignature = sootMethodRef.getSubSignature();
            List<SootClass> subClasses = Scene.v().getActiveHierarchy().getSubclassesOf(declaringClass);
//            subClasses.add(declaringClass);
            for (SootClass clazz :
                    subClasses) {
                SootMethod calleeMethod = dispatch(clazz, subSignature);
                if(calleeMethod == null) continue;
                Callee callee = this.callees.getCalleeBySignature(calleeMethod.getSignature());
                if (callee == null) {
                    callee = new Callee(calleeMethod.getDeclaringClass(), calleeMethod,
                            calleeMethod.getSignature(), calleeMethod.getJavaSourceStartLineNumber());
                }
                this.callees.addCallee(callee);
                Edge edge = this.edges.getEdgeByCallerAndCallee(caller, callee, InvokeType.Virtual);
                if (edge == null) {
                    edge = new Edge(InvokeType.Virtual, caller, callee);
                }
                this.edges.addEdge(edge);
            }
        } else if (invokeExpr instanceof JInterfaceInvokeExpr) {
            SootClass declaringClass = sootMethodRef.getDeclaringClass();
            NumberedString subSignature = sootMethodRef.getSubSignature();
            List<SootClass> subClasses = Scene.v().getActiveHierarchy().getImplementersOf(declaringClass);
            for (SootClass clazz :
                    subClasses) {
                SootMethod calleeMethod = dispatch(clazz, subSignature);
                if(calleeMethod == null) continue;
                Callee callee = this.callees.getCalleeBySignature(calleeMethod.getSignature());
                if (callee == null) {
                    callee = new Callee(calleeMethod.getDeclaringClass(), calleeMethod,
                            calleeMethod.getSignature(), calleeMethod.getJavaSourceStartLineNumber());
                }
                Edge edge = this.edges.getEdgeByCallerAndCallee(caller, callee, InvokeType.Interface);
                if (edge == null) {
                    edge = new Edge(InvokeType.Interface, caller, callee);
                }
                this.callees.addCallee(callee);
                this.edges.addEdge(edge);
            }
        } else {
            // TODO: dynamic
        }

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
