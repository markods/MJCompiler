// generated with ast extension for cup
// version 0.8
// 5/7/2021 12:12:27


package rs.ac.bg.etf.pp1.ast;

public class DesignatorNextElemTail extends DesignatorNext {

    private Expr Expr;
    private DesignatorNext DesignatorNext;

    public DesignatorNextElemTail (Expr Expr, DesignatorNext DesignatorNext) {
        this.Expr=Expr;
        if(Expr!=null) Expr.setParent(this);
        this.DesignatorNext=DesignatorNext;
        if(DesignatorNext!=null) DesignatorNext.setParent(this);
    }

    public Expr getExpr() {
        return Expr;
    }

    public void setExpr(Expr Expr) {
        this.Expr=Expr;
    }

    public DesignatorNext getDesignatorNext() {
        return DesignatorNext;
    }

    public void setDesignatorNext(DesignatorNext DesignatorNext) {
        this.DesignatorNext=DesignatorNext;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(Expr!=null) Expr.accept(visitor);
        if(DesignatorNext!=null) DesignatorNext.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(Expr!=null) Expr.traverseTopDown(visitor);
        if(DesignatorNext!=null) DesignatorNext.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(Expr!=null) Expr.traverseBottomUp(visitor);
        if(DesignatorNext!=null) DesignatorNext.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("DesignatorNextElemTail(\n");

        if(Expr!=null)
            buffer.append(Expr.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(DesignatorNext!=null)
            buffer.append(DesignatorNext.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [DesignatorNextElemTail]");
        return buffer.toString();
    }
}
