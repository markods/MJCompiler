// generated with ast extension for cup
// version 0.8
// 5/7/2021 12:12:27


package rs.ac.bg.etf.pp1.ast;

public class RelopEq extends Relop {

    public RelopEq () {
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
        buffer.append("RelopEq(\n");

        buffer.append(tab);
        buffer.append(") [RelopEq]");
        return buffer.toString();
    }
}
