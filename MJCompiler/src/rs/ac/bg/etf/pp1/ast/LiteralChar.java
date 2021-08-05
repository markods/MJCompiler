// generated with ast extension for cup
// version 0.8
// 5/7/2021 12:12:27


package rs.ac.bg.etf.pp1.ast;

public class LiteralChar extends Literal {

    private Character c1;

    public LiteralChar (Character c1) {
        this.c1=c1;
    }

    public Character getC1() {
        return c1;
    }

    public void setC1(Character c1) {
        this.c1=c1;
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
        buffer.append("LiteralChar(\n");

        buffer.append(" "+tab+c1);
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [LiteralChar]");
        return buffer.toString();
    }
}
