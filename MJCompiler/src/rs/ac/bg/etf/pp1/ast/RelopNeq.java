// generated with ast extension for cup
// version 0.8
// 5/7/2021 17:50:47


package rs.ac.bg.etf.pp1.ast;

public class RelopNeq extends Relop {

    public RelopNeq () {
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
        buffer.append("RelopNeq(\n");

        buffer.append(tab);
        buffer.append(") [RelopNeq]");
        return buffer.toString();
    }
}
