// generated with ast extension for cup
// version 0.8
// 8/7/2021 21:10:27


package rs.ac.bg.etf.pp1.ast;

public class VarDeclList_Empty extends VarDeclList {

    public VarDeclList_Empty () {
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
        buffer.append("VarDeclList_Empty(\n");

        buffer.append(tab);
        buffer.append(") [VarDeclList_Empty]");
        return buffer.toString();
    }
}