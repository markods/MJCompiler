// generated with ast extension for cup
// version 0.8
// 6/7/2021 17:15:5


package rs.ac.bg.etf.pp1.ast;

public class Addition_Tail extends Addition {

    private SignedAddition SignedAddition;

    public Addition_Tail (SignedAddition SignedAddition) {
        this.SignedAddition=SignedAddition;
        if(SignedAddition!=null) SignedAddition.setParent(this);
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
        if(SignedAddition!=null) SignedAddition.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(SignedAddition!=null) SignedAddition.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(SignedAddition!=null) SignedAddition.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("Addition_Tail(\n");

        if(SignedAddition!=null)
            buffer.append(SignedAddition.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [Addition_Tail]");
        return buffer.toString();
    }
}
