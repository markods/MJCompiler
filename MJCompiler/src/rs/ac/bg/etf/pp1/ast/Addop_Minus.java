// generated with ast extension for cup
// version 0.8
// 8/7/2021 20:30:1


package rs.ac.bg.etf.pp1.ast;

public class Addop_Minus extends Addop {

    public Addop_Minus () {
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
        buffer.append("Addop_Minus(\n");

        buffer.append(tab);
        buffer.append(") [Addop_Minus]");
        return buffer.toString();
    }
}
