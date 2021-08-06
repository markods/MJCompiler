// generated with ast extension for cup
// version 0.8
// 6/7/2021 17:15:5


package rs.ac.bg.etf.pp1.ast;

public class ConstDecl_Plain extends ConstDecl {

    private Type Type;
    private IdentInitList IdentInitList;

    public ConstDecl_Plain (Type Type, IdentInitList IdentInitList) {
        this.Type=Type;
        if(Type!=null) Type.setParent(this);
        this.IdentInitList=IdentInitList;
        if(IdentInitList!=null) IdentInitList.setParent(this);
    }

    public Type getType() {
        return Type;
    }

    public void setType(Type Type) {
        this.Type=Type;
    }

    public IdentInitList getIdentInitList() {
        return IdentInitList;
    }

    public void setIdentInitList(IdentInitList IdentInitList) {
        this.IdentInitList=IdentInitList;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(Type!=null) Type.accept(visitor);
        if(IdentInitList!=null) IdentInitList.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(Type!=null) Type.traverseTopDown(visitor);
        if(IdentInitList!=null) IdentInitList.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(Type!=null) Type.traverseBottomUp(visitor);
        if(IdentInitList!=null) IdentInitList.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ConstDecl_Plain(\n");

        if(Type!=null)
            buffer.append(Type.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(IdentInitList!=null)
            buffer.append(IdentInitList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ConstDecl_Plain]");
        return buffer.toString();
    }
}
