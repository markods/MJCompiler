// generated with ast extension for cup
// version 0.8
// 8/7/2021 20:30:1


package rs.ac.bg.etf.pp1.ast;

public class Literal_Bool extends Literal {

    private Boolean b1;

    public Literal_Bool (Boolean b1) {
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
        buffer.append("Literal_Bool(\n");

        buffer.append(" "+tab+b1);
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [Literal_Bool]");
        return buffer.toString();
    }
}
