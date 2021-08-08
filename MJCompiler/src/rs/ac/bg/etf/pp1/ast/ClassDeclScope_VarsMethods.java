// generated with ast extension for cup
// version 0.8
// 8/7/2021 21:10:27


package rs.ac.bg.etf.pp1.ast;

public class ClassDeclScope_VarsMethods extends ClassDeclScope {

    private ClassVarDeclList ClassVarDeclList;
    private MethodDeclScope MethodDeclScope;

    public ClassDeclScope_VarsMethods (ClassVarDeclList ClassVarDeclList, MethodDeclScope MethodDeclScope) {
        this.ClassVarDeclList=ClassVarDeclList;
        if(ClassVarDeclList!=null) ClassVarDeclList.setParent(this);
        this.MethodDeclScope=MethodDeclScope;
        if(MethodDeclScope!=null) MethodDeclScope.setParent(this);
    }

    public ClassVarDeclList getClassVarDeclList() {
        return ClassVarDeclList;
    }

    public void setClassVarDeclList(ClassVarDeclList ClassVarDeclList) {
        this.ClassVarDeclList=ClassVarDeclList;
    }

    public MethodDeclScope getMethodDeclScope() {
        return MethodDeclScope;
    }

    public void setMethodDeclScope(MethodDeclScope MethodDeclScope) {
        this.MethodDeclScope=MethodDeclScope;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(ClassVarDeclList!=null) ClassVarDeclList.accept(visitor);
        if(MethodDeclScope!=null) MethodDeclScope.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(ClassVarDeclList!=null) ClassVarDeclList.traverseTopDown(visitor);
        if(MethodDeclScope!=null) MethodDeclScope.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(ClassVarDeclList!=null) ClassVarDeclList.traverseBottomUp(visitor);
        if(MethodDeclScope!=null) MethodDeclScope.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ClassDeclScope_VarsMethods(\n");

        if(ClassVarDeclList!=null)
            buffer.append(ClassVarDeclList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(MethodDeclScope!=null)
            buffer.append(MethodDeclScope.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ClassDeclScope_VarsMethods]");
        return buffer.toString();
    }
}
