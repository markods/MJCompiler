package rs.ac.bg.etf.pp1.util;

import rs.ac.bg.etf.pp1.SymbolTable;
import rs.etf.pp1.symboltable.concepts.Scope;

public class ScopeGuard implements AutoCloseable
{
    private Scope scope;

    public ScopeGuard()
    {
        scope = SymbolTable.openScope();
    }

    public Scope scope() { return scope; }

    @Override
    public void close()
    {
        SymbolTable.closeScope();
    }
}
