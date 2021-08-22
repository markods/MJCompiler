package rs.ac.bg.etf.pp1.visitors;

import rs.ac.bg.etf.pp1.ast.*;


public class CounterVisitor extends VisitorAdaptor
{
    protected int count;
    protected CounterVisitor() {}

    public int getCount() { return count; }

    public static class VarCounter extends CounterVisitor
    {
        // TODO
        @Override
        public void visit( VarDecl VarDecl ) { count++; }
    }
}
