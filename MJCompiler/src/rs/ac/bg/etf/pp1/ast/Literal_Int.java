// generated with ast extension for cup
// version 0.8
// 8/7/2021 20:30:1


package rs.ac.bg.etf.pp1.ast;

public class Literal_Int extends Literal {

    private Integer i1;

    public Literal_Int (Integer i1) {
        this.i1=i1;
    }

    public Integer getI1() {
        return i1;
    }

    public void setI1(Integer i1) {
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
        buffer.append("Literal_Int(\n");

        buffer.append(" "+tab+i1);
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [Literal_Int]");
        return buffer.toString();
    }
}
