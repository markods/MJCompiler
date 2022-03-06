package rs.ac.bg.etf.pp1.util;

import rs.ac.bg.etf.pp1.SymbolTable;
import rs.etf.pp1.symboltable.concepts.Scope;

public class ScopeGuard implements AutoCloseable
{
    private final SymbolTable symbolTable;
    private final Scope scope;

    public ScopeGuard( SymbolTable symbolTable )
    {
        this.symbolTable = symbolTable;
        this.scope = symbolTable.openScope();
    }

    public Scope scope() { return scope; }

    @Override
    public void close()
    {
        symbolTable.closeScope();
    }
}
