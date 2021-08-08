// generated with ast extension for cup
// version 0.8
// 8/7/2021 20:30:1


package rs.ac.bg.etf.pp1.ast;

public class FormParsNext_FormPar extends FormParsNext {

    private Type Type;
    private VarIdent VarIdent;
    private FormParsNext FormParsNext;

    public FormParsNext_FormPar (Type Type, VarIdent VarIdent, FormParsNext FormParsNext) {
        this.Type=Type;
        if(Type!=null) Type.setParent(this);
        this.VarIdent=VarIdent;
        if(VarIdent!=null) VarIdent.setParent(this);
        this.FormParsNext=FormParsNext;
        if(FormParsNext!=null) FormParsNext.setParent(this);
    }

    public Type getType() {
        return Type;
    }

    public void setType(Type Type) {
        this.Type=Type;
    }

    public VarIdent getVarIdent() {
        return VarIdent;
    }

    public void setVarIdent(VarIdent VarIdent) {
        this.VarIdent=VarIdent;
    }

    public FormParsNext getFormParsNext() {
        return FormParsNext;
    }

    public void setFormParsNext(FormParsNext FormParsNext) {
        this.FormParsNext=FormParsNext;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(Type!=null) Type.accept(visitor);
        if(VarIdent!=null) VarIdent.accept(visitor);
        if(FormParsNext!=null) FormParsNext.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(Type!=null) Type.traverseTopDown(visitor);
        if(VarIdent!=null) VarIdent.traverseTopDown(visitor);
        if(FormParsNext!=null) FormParsNext.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(Type!=null) Type.traverseBottomUp(visitor);
        if(VarIdent!=null) VarIdent.traverseBottomUp(visitor);
        if(FormParsNext!=null) FormParsNext.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("FormParsNext_FormPar(\n");

        if(Type!=null)
            buffer.append(Type.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(VarIdent!=null)
            buffer.append(VarIdent.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(FormParsNext!=null)
            buffer.append(FormParsNext.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [FormParsNext_FormPar]");
        return buffer.toString();
    }
}
