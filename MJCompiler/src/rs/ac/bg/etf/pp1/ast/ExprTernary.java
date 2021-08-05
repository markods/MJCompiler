// generated with ast extension for cup
// version 0.8
// 5/7/2021 17:50:47


package rs.ac.bg.etf.pp1.ast;

public class ExprTernary extends Expr {

    private Addition Addition;
    private Addition Addition1;
    private Addition Addition2;

    public ExprTernary (Addition Addition, Addition Addition1, Addition Addition2) {
        this.Addition=Addition;
        if(Addition!=null) Addition.setParent(this);
        this.Addition1=Addition1;
        if(Addition1!=null) Addition1.setParent(this);
        this.Addition2=Addition2;
        if(Addition2!=null) Addition2.setParent(this);
    }

    public Addition getAddition() {
        return Addition;
    }

    public void setAddition(Addition Addition) {
        this.Addition=Addition;
    }

    public Addition getAddition1() {
        return Addition1;
    }

    public void setAddition1(Addition Addition1) {
        this.Addition1=Addition1;
    }

    public Addition getAddition2() {
        return Addition2;
    }

    public void setAddition2(Addition Addition2) {
        this.Addition2=Addition2;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(Addition!=null) Addition.accept(visitor);
        if(Addition1!=null) Addition1.accept(visitor);
        if(Addition2!=null) Addition2.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(Addition!=null) Addition.traverseTopDown(visitor);
        if(Addition1!=null) Addition1.traverseTopDown(visitor);
        if(Addition2!=null) Addition2.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(Addition!=null) Addition.traverseBottomUp(visitor);
        if(Addition1!=null) Addition1.traverseBottomUp(visitor);
        if(Addition2!=null) Addition2.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ExprTernary(\n");

        if(Addition!=null)
            buffer.append(Addition.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(Addition1!=null)
            buffer.append(Addition1.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(Addition2!=null)
            buffer.append(Addition2.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ExprTernary]");
        return buffer.toString();
    }
}
