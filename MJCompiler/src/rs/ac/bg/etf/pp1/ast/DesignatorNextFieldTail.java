// generated with ast extension for cup
// version 0.8
// 5/7/2021 12:12:27


package rs.ac.bg.etf.pp1.ast;

public class DesignatorNextFieldTail extends DesignatorNext {

    private String i1;
    private DesignatorNext DesignatorNext;

    public DesignatorNextFieldTail (String i1, DesignatorNext DesignatorNext) {
        this.i1=i1;
        this.DesignatorNext=DesignatorNext;
        if(DesignatorNext!=null) DesignatorNext.setParent(this);
    }

    public String getI1() {
        return i1;
    }

    public void setI1(String i1) {
        this.i1=i1;
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
        if(DesignatorNext!=null) DesignatorNext.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(DesignatorNext!=null) DesignatorNext.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(DesignatorNext!=null) DesignatorNext.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("DesignatorNextFieldTail(\n");

        buffer.append(" "+tab+i1);
        buffer.append("\n");

        if(DesignatorNext!=null)
            buffer.append(DesignatorNext.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [DesignatorNextFieldTail]");
        return buffer.toString();
    }
}
