// generated with ast extension for cup
// version 0.8
// 8/7/2021 21:10:27


package rs.ac.bg.etf.pp1.ast;

public class Expr_Addition extends Expr {

    private Addition Addition;

    public Expr_Addition (Addition Addition) {
        this.Addition=Addition;
        if(Addition!=null) Addition.setParent(this);
    }

    public Addition getAddition() {
        return Addition;
    }

    public void setAddition(Addition Addition) {
        this.Addition=Addition;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(Addition!=null) Addition.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(Addition!=null) Addition.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(Addition!=null) Addition.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("Expr_Addition(\n");

        if(Addition!=null)
            buffer.append(Addition.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [Expr_Addition]");
        return buffer.toString();
    }
}
