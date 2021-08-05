// generated with ast extension for cup
// version 0.8
// 5/7/2021 12:12:27


package rs.ac.bg.etf.pp1.ast;

public class Program implements SyntaxNode {

    private SyntaxNode parent;
    private int line;
    private String i1;
    private StaticDeclList StaticDeclList;
    private MethodDeclScope MethodDeclScope;

    public Program (String i1, StaticDeclList StaticDeclList, MethodDeclScope MethodDeclScope) {
        this.i1=i1;
        this.StaticDeclList=StaticDeclList;
        if(StaticDeclList!=null) StaticDeclList.setParent(this);
        this.MethodDeclScope=MethodDeclScope;
        if(MethodDeclScope!=null) MethodDeclScope.setParent(this);
    }

    public String getI1() {
        return i1;
    }

    public void setI1(String i1) {
        this.i1=i1;
    }

    public StaticDeclList getStaticDeclList() {
        return StaticDeclList;
    }

    public void setStaticDeclList(StaticDeclList StaticDeclList) {
        this.StaticDeclList=StaticDeclList;
    }

    public MethodDeclScope getMethodDeclScope() {
        return MethodDeclScope;
    }

    public void setMethodDeclScope(MethodDeclScope MethodDeclScope) {
        this.MethodDeclScope=MethodDeclScope;
    }

    public SyntaxNode getParent() {
        return parent;
    }

    public void setParent(SyntaxNode parent) {
        this.parent=parent;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line=line;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(StaticDeclList!=null) StaticDeclList.accept(visitor);
        if(MethodDeclScope!=null) MethodDeclScope.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(StaticDeclList!=null) StaticDeclList.traverseTopDown(visitor);
        if(MethodDeclScope!=null) MethodDeclScope.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(StaticDeclList!=null) StaticDeclList.traverseBottomUp(visitor);
        if(MethodDeclScope!=null) MethodDeclScope.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("Program(\n");

        buffer.append(" "+tab+i1);
        buffer.append("\n");

        if(StaticDeclList!=null)
            buffer.append(StaticDeclList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(MethodDeclScope!=null)
            buffer.append(MethodDeclScope.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [Program]");
        return buffer.toString();
    }
}
