// generated with ast extension for cup
// version 0.8
// 6/7/2021 17:15:5


package rs.ac.bg.etf.pp1.ast;

public class ReturnType_Ident extends ReturnType {

    private String i1;

    public ReturnType_Ident (String i1) {
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
        buffer.append("ReturnType_Ident(\n");

        buffer.append(" "+tab+i1);
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ReturnType_Ident]");
        return buffer.toString();
    }
}
