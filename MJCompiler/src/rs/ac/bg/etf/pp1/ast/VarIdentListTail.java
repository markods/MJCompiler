// generated with ast extension for cup
// version 0.8
// 5/7/2021 12:12:27


package rs.ac.bg.etf.pp1.ast;

public class VarIdentListTail extends VarIdentList {

    private VarIdent VarIdent;
    private VarIdentList VarIdentList;

    public VarIdentListTail (VarIdent VarIdent, VarIdentList VarIdentList) {
        this.VarIdent=VarIdent;
        if(VarIdent!=null) VarIdent.setParent(this);
        this.VarIdentList=VarIdentList;
        if(VarIdentList!=null) VarIdentList.setParent(this);
    }

    public VarIdent getVarIdent() {
        return VarIdent;
    }

    public void setVarIdent(VarIdent VarIdent) {
        this.VarIdent=VarIdent;
    }

    public VarIdentList getVarIdentList() {
        return VarIdentList;
    }

    public void setVarIdentList(VarIdentList VarIdentList) {
        this.VarIdentList=VarIdentList;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(VarIdent!=null) VarIdent.accept(visitor);
        if(VarIdentList!=null) VarIdentList.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(VarIdent!=null) VarIdent.traverseTopDown(visitor);
        if(VarIdentList!=null) VarIdentList.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(VarIdent!=null) VarIdent.traverseBottomUp(visitor);
        if(VarIdentList!=null) VarIdentList.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("VarIdentListTail(\n");

        if(VarIdent!=null)
            buffer.append(VarIdent.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(VarIdentList!=null)
            buffer.append(VarIdentList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [VarIdentListTail]");
        return buffer.toString();
    }
}
