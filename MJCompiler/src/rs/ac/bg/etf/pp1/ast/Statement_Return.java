// generated with ast extension for cup
// version 0.8
// 8/7/2021 15:21:33


package rs.ac.bg.etf.pp1.ast;

public class Statement_Return extends Statement {

    public Statement_Return () {
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
        buffer.append("Statement_Return(\n");

        buffer.append(tab);
        buffer.append(") [Statement_Return]");
        return buffer.toString();
    }
}
