// generated with ast extension for cup
// version 0.8
// 6/7/2021 17:15:5


package rs.ac.bg.etf.pp1.ast;

public class ClassDecl_Extends extends ClassDecl {

    private String i1;
    private Type Type;
    private ClassDeclScope ClassDeclScope;

    public ClassDecl_Extends (String i1, Type Type, ClassDeclScope ClassDeclScope) {
        this.i1=i1;
        this.Type=Type;
        if(Type!=null) Type.setParent(this);
        this.ClassDeclScope=ClassDeclScope;
        if(ClassDeclScope!=null) ClassDeclScope.setParent(this);
    }

    public String getI1() {
        return i1;
    }

    public void setI1(String i1) {
        this.i1=i1;
    }

    public Type getType() {
        return Type;
    }

    public void setType(Type Type) {
        this.Type=Type;
    }

    public ClassDeclScope getClassDeclScope() {
        return ClassDeclScope;
    }

    public void setClassDeclScope(ClassDeclScope ClassDeclScope) {
        this.ClassDeclScope=ClassDeclScope;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(Type!=null) Type.accept(visitor);
        if(ClassDeclScope!=null) ClassDeclScope.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(Type!=null) Type.traverseTopDown(visitor);
        if(ClassDeclScope!=null) ClassDeclScope.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(Type!=null) Type.traverseBottomUp(visitor);
        if(ClassDeclScope!=null) ClassDeclScope.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ClassDecl_Extends(\n");

        buffer.append(" "+tab+i1);
        buffer.append("\n");

        if(Type!=null)
            buffer.append(Type.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(ClassDeclScope!=null)
            buffer.append(ClassDeclScope.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ClassDecl_Extends]");
        return buffer.toString();
    }
}
