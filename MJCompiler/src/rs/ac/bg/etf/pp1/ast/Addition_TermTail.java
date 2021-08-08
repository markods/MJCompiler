// generated with ast extension for cup
// version 0.8
// 8/7/2021 15:21:33


package rs.ac.bg.etf.pp1.ast;

public class Addition_TermTail extends Addition {

    private Term Term;
    private SignedAddition SignedAddition;

    public Addition_TermTail (Term Term, SignedAddition SignedAddition) {
        this.Term=Term;
        if(Term!=null) Term.setParent(this);
        this.SignedAddition=SignedAddition;
        if(SignedAddition!=null) SignedAddition.setParent(this);
    }

    public Term getTerm() {
        return Term;
    }

    public void setTerm(Term Term) {
        this.Term=Term;
    }

    public SignedAddition getSignedAddition() {
        return SignedAddition;
    }

    public void setSignedAddition(SignedAddition SignedAddition) {
        this.SignedAddition=SignedAddition;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(Term!=null) Term.accept(visitor);
        if(SignedAddition!=null) SignedAddition.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(Term!=null) Term.traverseTopDown(visitor);
        if(SignedAddition!=null) SignedAddition.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(Term!=null) Term.traverseBottomUp(visitor);
        if(SignedAddition!=null) SignedAddition.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("Addition_TermTail(\n");

        if(Term!=null)
            buffer.append(Term.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(SignedAddition!=null)
            buffer.append(SignedAddition.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [Addition_TermTail]");
        return buffer.toString();
    }
}
