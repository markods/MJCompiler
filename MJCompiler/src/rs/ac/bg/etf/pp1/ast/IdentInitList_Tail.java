// generated with ast extension for cup
// version 0.8
// 8/7/2021 21:10:27


package rs.ac.bg.etf.pp1.ast;

public class IdentInitList_Tail extends IdentInitList {

    private String i1;
    private Assignop Assignop;
    private Literal Literal;
    private IdentInitList IdentInitList;

    public IdentInitList_Tail (String i1, Assignop Assignop, Literal Literal, IdentInitList IdentInitList) {
        this.i1=i1;
        this.Assignop=Assignop;
        if(Assignop!=null) Assignop.setParent(this);
        this.Literal=Literal;
        if(Literal!=null) Literal.setParent(this);
        this.IdentInitList=IdentInitList;
        if(IdentInitList!=null) IdentInitList.setParent(this);
    }

    public String getI1() {
        return i1;
    }

    public void setI1(String i1) {
        this.i1=i1;
    }

    public Assignop getAssignop() {
        return Assignop;
    }

    public void setAssignop(Assignop Assignop) {
        this.Assignop=Assignop;
    }

    public Literal getLiteral() {
        return Literal;
    }

    public void setLiteral(Literal Literal) {
        this.Literal=Literal;
    }

    public IdentInitList getIdentInitList() {
        return IdentInitList;
    }

    public void setIdentInitList(IdentInitList IdentInitList) {
        this.IdentInitList=IdentInitList;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(Assignop!=null) Assignop.accept(visitor);
        if(Literal!=null) Literal.accept(visitor);
        if(IdentInitList!=null) IdentInitList.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(Assignop!=null) Assignop.traverseTopDown(visitor);
        if(Literal!=null) Literal.traverseTopDown(visitor);
        if(IdentInitList!=null) IdentInitList.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(Assignop!=null) Assignop.traverseBottomUp(visitor);
        if(Literal!=null) Literal.traverseBottomUp(visitor);
        if(IdentInitList!=null) IdentInitList.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("IdentInitList_Tail(\n");

        buffer.append(" "+tab+i1);
        buffer.append("\n");

        if(Assignop!=null)
            buffer.append(Assignop.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(Literal!=null)
            buffer.append(Literal.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(IdentInitList!=null)
            buffer.append(IdentInitList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [IdentInitList_Tail]");
        return buffer.toString();
    }
}
