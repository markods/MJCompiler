// generated with ast extension for cup
// version 0.8
// 5/7/2021 17:50:47


package rs.ac.bg.etf.pp1.ast;

public class ClassDeclScopeVarsMethods extends ClassDeclScope {

    private VarDeclList VarDeclList;
    private MethodDeclScope MethodDeclScope;

    public ClassDeclScopeVarsMethods (VarDeclList VarDeclList, MethodDeclScope MethodDeclScope) {
        this.VarDeclList=VarDeclList;
        if(VarDeclList!=null) VarDeclList.setParent(this);
        this.MethodDeclScope=MethodDeclScope;
        if(MethodDeclScope!=null) MethodDeclScope.setParent(this);
    }

    public VarDeclList getVarDeclList() {
        return VarDeclList;
    }

    public void setVarDeclList(VarDeclList VarDeclList) {
        this.VarDeclList=VarDeclList;
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
        if(VarDeclList!=null) VarDeclList.accept(visitor);
        if(MethodDeclScope!=null) MethodDeclScope.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(VarDeclList!=null) VarDeclList.traverseTopDown(visitor);
        if(MethodDeclScope!=null) MethodDeclScope.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(VarDeclList!=null) VarDeclList.traverseBottomUp(visitor);
        if(MethodDeclScope!=null) MethodDeclScope.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ClassDeclScopeVarsMethods(\n");

        if(VarDeclList!=null)
            buffer.append(VarDeclList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(MethodDeclScope!=null)
            buffer.append(MethodDeclScope.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ClassDeclScopeVarsMethods]");
        return buffer.toString();
    }
}
