// generated with ast extension for cup
// version 0.8
// 5/7/2021 12:12:27


package rs.ac.bg.etf.pp1.ast;

public class ClassDeclPlain extends ClassDecl {

    private String i1;
    private ClassDeclScope ClassDeclScope;

    public ClassDeclPlain (String i1, ClassDeclScope ClassDeclScope) {
        this.i1=i1;
        this.ClassDeclScope=ClassDeclScope;
        if(ClassDeclScope!=null) ClassDeclScope.setParent(this);
    }

    public String getI1() {
        return i1;
    }

    public void setI1(String i1) {
        this.i1=i1;
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
        if(ClassDeclScope!=null) ClassDeclScope.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(ClassDeclScope!=null) ClassDeclScope.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(ClassDeclScope!=null) ClassDeclScope.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ClassDeclPlain(\n");

        buffer.append(" "+tab+i1);
        buffer.append("\n");

        if(ClassDeclScope!=null)
            buffer.append(ClassDeclScope.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ClassDeclPlain]");
        return buffer.toString();
    }
}
