// generated with ast extension for cup
// version 0.8
// 5/7/2021 12:12:27


package rs.ac.bg.etf.pp1.ast;

public class LiteralBool extends Literal {

    private Boolean b1;

    public LiteralBool (Boolean b1) {
        this.b1=b1;
    }

    public Boolean getB1() {
        return b1;
    }

    public void setB1(Boolean b1) {
        this.b1=b1;
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
        buffer.append("LiteralBool(\n");

        buffer.append(" "+tab+b1);
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [LiteralBool]");
        return buffer.toString();
    }
}
