// generated with ast extension for cup
// version 0.8
// 6/7/2021 17:15:5


package rs.ac.bg.etf.pp1.ast;

public class ActPars_Tail extends ActPars {

    private Expr Expr;
    private ActParsNext ActParsNext;

    public ActPars_Tail (Expr Expr, ActParsNext ActParsNext) {
        this.Expr=Expr;
        if(Expr!=null) Expr.setParent(this);
        this.ActParsNext=ActParsNext;
        if(ActParsNext!=null) ActParsNext.setParent(this);
    }

    public Expr getExpr() {
        return Expr;
    }

    public void setExpr(Expr Expr) {
        this.Expr=Expr;
    }

    public ActParsNext getActParsNext() {
        return ActParsNext;
    }

    public void setActParsNext(ActParsNext ActParsNext) {
        this.ActParsNext=ActParsNext;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(Expr!=null) Expr.accept(visitor);
        if(ActParsNext!=null) ActParsNext.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(Expr!=null) Expr.traverseTopDown(visitor);
        if(ActParsNext!=null) ActParsNext.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(Expr!=null) Expr.traverseBottomUp(visitor);
        if(ActParsNext!=null) ActParsNext.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ActPars_Tail(\n");

        if(Expr!=null)
            buffer.append(Expr.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(ActParsNext!=null)
            buffer.append(ActParsNext.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ActPars_Tail]");
        return buffer.toString();
    }
}
