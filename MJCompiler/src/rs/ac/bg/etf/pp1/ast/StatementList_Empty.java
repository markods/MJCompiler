// generated with ast extension for cup
// version 0.8
// 6/7/2021 17:15:5


package rs.ac.bg.etf.pp1.ast;

public class StatementList_Empty extends StatementList {

    public StatementList_Empty () {
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
        buffer.append("StatementList_Empty(\n");

        buffer.append(tab);
        buffer.append(") [StatementList_Empty]");
        return buffer.toString();
    }
}
