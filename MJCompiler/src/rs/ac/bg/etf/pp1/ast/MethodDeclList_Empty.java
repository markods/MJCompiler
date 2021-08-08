// generated with ast extension for cup
// version 0.8
// 8/7/2021 15:21:33


package rs.ac.bg.etf.pp1.ast;

public class MethodDeclList_Empty extends MethodDeclList {

    public MethodDeclList_Empty () {
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
        buffer.append("MethodDeclList_Empty(\n");

        buffer.append(tab);
        buffer.append(") [MethodDeclList_Empty]");
        return buffer.toString();
    }
}
