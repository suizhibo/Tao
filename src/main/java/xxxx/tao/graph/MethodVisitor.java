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
    private Hierarchy hierarchy;

    private Edges edges = new Edges();
    private Callees callees = new Callees();
    private Callers callers = new Callers();


    public MethodVisitor(SootClass sootClass, Edges edges, Callers callers, Callees callees) {
        this.sootClass = sootClass;
        this.edges = edges;
        this.callers = callers;
        this.callees = callees;
        this.hierarchy = Scene.v().getActiveHierarchy();
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
        } else if (invokeExpr instanceof JSpecialInvokeExpr) {
            SootClass declaringClass = sootMethodRef.getDeclaringClass();
            NumberedString subSignature = sootMethodRef.getSubSignature();
            SootMethod calleeMethod = dispatch(declaringClass, subSignature);
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
            List<SootClass> subClasses = this.hierarchy.getSubclassesOf(declaringClass);
//            subClasses.add(declaringClass);
            for (SootClass clazz :
                    subClasses) {
                SootMethod calleeMethod = dispatch(clazz, subSignature);
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
            List<SootClass> subClasses = this.hierarchy.getImplementersOf(declaringClass);
            for (SootClass clazz :
                    subClasses) {
                SootMethod calleeMethod = dispatch(clazz, subSignature);
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
            SootMethod sootMethod = sootClass.getMethod(subSignature);
            if (!sootMethod.isAbstract()) {
                return sootMethod;
            }
            SootClass superClass = this.hierarchy.getSuperclassesOf(sootClass).get(0);
            return dispatch(superClass, subSignature);
        } catch (Exception exception) {

        }
        return null;
    }
}
