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
                caller = new Caller(this.sootClass, sootMethod, sootMethod.getSignature());
            }
            this.callers.addCaller(caller);
            resolve(invokeExpr, caller, startLine);
        }
    }

    private void resolve(InvokeExpr invokeExpr, Caller caller, int callLineNumber) {
        SootMethodRef sootMethodRef = invokeExpr.getMethodRef();
        SootClass declaringClass = sootMethodRef.getDeclaringClass();
        NumberedString subSignature = sootMethodRef.getSubSignature();
        if (invokeExpr instanceof JStaticInvokeExpr) {
            // support lambda
            if (sootMethodRef.getDeclaringClass().toString().contains("$lambda") && sootMethodRef.getName().equals("bootstrap$")) {
                SootClass deClass = sootMethodRef.getDeclaringClass();
                deClass.getMethods().forEach(sm -> {
                    String methodName = sm.getName();
                    if (!methodName.equals("bootstrap$") && !methodName.equals("<init>")) {
                        Callee callee = this.callees.getCalleeBySignature(sm.getSignature());
                        if (callee == null) {
                            callee = new Callee(deClass, sm, sm.getSignature());
                        }
                        Edge edge = this.edges.getEdgeByCallerAndCallee(caller, callee, InvokeType.Static);
                        if (edge == null) {
                            edge = new Edge(InvokeType.Static, callLineNumber, caller, callee);
                        }
                        this.callees.addCallee(callee);
                        this.edges.addEdge(edge);
                        new MethodVisitor(deClass, this.edges, this.callers, this.callees).run();
                    }
                });
            } else {
                Callee callee = this.callees.getCalleeBySignature(sootMethodRef.getSignature());
                if (callee == null) {
                    callee = new Callee(sootMethodRef.getDeclaringClass(), sootMethodRef.resolve(),
                            sootMethodRef.getSignature());
                }
                Edge edge = this.edges.getEdgeByCallerAndCallee(caller, callee, InvokeType.Static);
                if (edge == null) {
                    edge = new Edge(InvokeType.Static, callLineNumber, caller, callee);
                }
                this.callees.addCallee(callee);
                this.edges.addEdge(edge);
            }
        } else if (invokeExpr instanceof JSpecialInvokeExpr) {
            resolve(caller, declaringClass, subSignature, InvokeType.Special, callLineNumber);
            resolvePhantom(caller, invokeExpr.getMethod(), InvokeType.Phantom, callLineNumber);
        } else if (invokeExpr instanceof JVirtualInvokeExpr) {
            resolvePhantom(caller, invokeExpr.getMethod(), InvokeType.Phantom, callLineNumber);
            List<SootClass> subClasses = Scene.v().getActiveHierarchy().getSubclassesOf(declaringClass);
            for (SootClass clazz :
                    subClasses) {
                resolve(caller, clazz, subSignature, InvokeType.Virtual, callLineNumber);
            }
            resolve(caller, declaringClass, subSignature, InvokeType.Virtual, callLineNumber);
        } else if (invokeExpr instanceof JInterfaceInvokeExpr) {
            resolvePhantom(caller, invokeExpr.getMethod(), InvokeType.Phantom, callLineNumber);
            List<SootClass> subClasses = Scene.v().getActiveHierarchy().getImplementersOf(declaringClass);
            for (SootClass clazz :
                    subClasses) {
                resolve(caller, clazz, subSignature, InvokeType.Interface, callLineNumber);
            }
            resolve(caller, declaringClass, subSignature, InvokeType.Interface, callLineNumber);
        } else {
            // TODO: dynamic
        }

    }

    private void resolvePhantom(Caller caller, SootMethod sootMethod, InvokeType invokeType, int callLineNumber) {
        Callee callee = this.callees.getCalleeBySignature(sootMethod.getSignature());
        if (callee == null) {
            callee = new Callee(sootMethod.getDeclaringClass(), sootMethod,
                    sootMethod.getSignature());
        }
        Edge edge = this.edges.getEdgeByCallerAndCallee(caller, callee, invokeType);
        if (edge == null) {
            edge = new Edge(invokeType, callLineNumber, caller, callee);
        }
        this.callees.addCallee(callee);
        this.edges.addEdge(edge);
    }


    private void resolve(Caller caller, SootClass sootClass, NumberedString subSignature, InvokeType invokeType, int callLineNumber) {
        SootMethod calleeMethod = dispatch(sootClass, subSignature);
        if (calleeMethod == null) return;
        Callee callee = this.callees.getCalleeBySignature(calleeMethod.getSignature());
        if (callee == null) {
            callee = new Callee(calleeMethod.getDeclaringClass(), calleeMethod,
                    calleeMethod.getSignature());
        }
        Edge edge = this.edges.getEdgeByCallerAndCallee(caller, callee, invokeType);
        if (edge == null) {
            edge = new Edge(invokeType, callLineNumber, caller, callee);
        }
        this.callees.addCallee(callee);
        this.edges.addEdge(edge);
    }

//    private SootMethod dispatch(SootClass sootClass, NumberedString subSignature) {
//        if (sootClass.toString().equals("java.lang.Object")) {
//            return null;
//        }
//        try {
//            SootMethod sootMethod = null;
//            try {
//                sootMethod = sootClass.getMethod(subSignature);
//            }catch (RuntimeException runtimeException){
//
//            }
//            if (sootMethod != null && !sootMethod.isAbstract()) {
//                return sootMethod;
//            }
//            SootClass directSuperClass = Scene.v().getActiveHierarchy().getSuperclassesOf(sootClass).get(0); // Direct
//            return dispatch(directSuperClass, subSignature);
//        } catch (Exception exception) {
//
//        }
//        return null;
//    }

    private SootMethod dispatch(SootClass sootClass, NumberedString subSignature) {
        // TODO:检查receiver class(sootClass)是不是subSignature声明类的子类, 如果不是则return null
        for (SootClass c = sootClass; c != null; c = c.getSuperclass()) {
            SootMethod sootMethod = null;
            try {
                sootMethod = c.getMethod(subSignature);
            } catch (RuntimeException e) {
            }
            if (sootMethod != null && !sootMethod.isAbstract()) {
                return sootMethod;
            }
        }

        for (SootClass c = sootClass; c != null; c = c.getSuperclass()) {
            for (SootClass ci : c.getInterfaces()) {
                SootMethod sootMethod = lookupMethodFormSuperinterfaces(ci, subSignature);
                if (sootMethod != null) {
                    return sootMethod;
                }
            }
        }
        return null;
    }

    private SootMethod lookupMethodFormSuperinterfaces(SootClass sootClass, NumberedString subSignature) {
        try {
            SootMethod sootMethod = sootClass.getMethod(subSignature);
            if (sootMethod != null && !sootMethod.isAbstract()) {
                return sootMethod;
            }
            for (SootClass c : sootClass.getInterfaces()) {
                sootMethod = lookupMethodFormSuperinterfaces(c, subSignature);
                if (sootMethod != null) {
                    return sootMethod;
                }
            }
        } catch (RuntimeException e) {
        }
        return null;
    }
}
