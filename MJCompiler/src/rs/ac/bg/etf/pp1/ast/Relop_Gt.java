// generated with ast extension for cup
// version 0.8
// 8/7/2021 20:30:1


package rs.ac.bg.etf.pp1.ast;

public class Relop_Gt extends Relop {

    public Relop_Gt () {
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
        buffer.append("Relop_Gt(\n");

        buffer.append(tab);
        buffer.append(") [Relop_Gt]");
        return buffer.toString();
    }
}
