package rs.ac.bg.etf.pp1;

import java.util.ArrayList;

public class SymbolList
{
    private final ArrayList<Symbol> symbolList = new ArrayList<>();

    public SymbolList() {}


    public boolean add( Symbol symbol )
    {
        return symbolList.add( symbol );
    }

    public void removeLast()
    {
        if( symbolList.size() > 0 ) symbolList.remove( symbolList.size() - 1 );
    }

    public void clear()
    {
        symbolList.clear();
    }


    public int size()
    {
        return symbolList.size();
    }

    public Symbol get( int index )
    {
        return symbolList.get( index );
    }

    public Symbol getLast()
    {
        return ( symbolList.size() > 0 ) ? symbolList.get( symbolList.size() - 1 ) : null;
    }

}
