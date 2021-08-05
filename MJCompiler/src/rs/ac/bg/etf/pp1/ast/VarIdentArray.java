// generated with ast extension for cup
// version 0.8
// 5/7/2021 12:12:27


package rs.ac.bg.etf.pp1.ast;

public class VarIdentArray extends VarIdent {

    private String i1;

    public VarIdentArray (String i1) {
        this.i1=i1;
    }

    public String getI1() {
        return i1;
    }

    public void setI1(String i1) {
        this.i1=i1;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("VarIdentArray(\n");

        buffer.append(" "+tab+i1);
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [VarIdentArray]");
        return buffer.toString();
    }
}
