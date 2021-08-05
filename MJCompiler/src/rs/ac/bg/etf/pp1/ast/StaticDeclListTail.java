// generated with ast extension for cup
// version 0.8
// 5/7/2021 12:12:27


package rs.ac.bg.etf.pp1.ast;

public class StaticDeclListTail extends StaticDeclList {

    private StaticDecl StaticDecl;
    private StaticDeclList StaticDeclList;

    public StaticDeclListTail (StaticDecl StaticDecl, StaticDeclList StaticDeclList) {
        this.StaticDecl=StaticDecl;
        if(StaticDecl!=null) StaticDecl.setParent(this);
        this.StaticDeclList=StaticDeclList;
        if(StaticDeclList!=null) StaticDeclList.setParent(this);
    }

    public StaticDecl getStaticDecl() {
        return StaticDecl;
    }

    public void setStaticDecl(StaticDecl StaticDecl) {
        this.StaticDecl=StaticDecl;
    }

    public StaticDeclList getStaticDeclList() {
        return StaticDeclList;
    }

    public void setStaticDeclList(StaticDeclList StaticDeclList) {
        this.StaticDeclList=StaticDeclList;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(StaticDecl!=null) StaticDecl.accept(visitor);
        if(StaticDeclList!=null) StaticDeclList.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(StaticDecl!=null) StaticDecl.traverseTopDown(visitor);
        if(StaticDeclList!=null) StaticDeclList.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(StaticDecl!=null) StaticDecl.traverseBottomUp(visitor);
        if(StaticDeclList!=null) StaticDeclList.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("StaticDeclListTail(\n");

        if(StaticDecl!=null)
            buffer.append(StaticDecl.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(StaticDeclList!=null)
            buffer.append(StaticDeclList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [StaticDeclListTail]");
        return buffer.toString();
    }
}
