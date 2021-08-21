package rs.ac.bg.etf.pp1;

import java.util.ArrayList;

public class SymbolList
{
    private ArrayList<Symbol> symbolList = new ArrayList<>();

    public SymbolList() {}

    public void assign( SymbolList symbols )
    {
        this.symbolList = symbols.symbolList;
    }



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

    public boolean checkIndex( int index )
    {
        return index >= 0 && index < symbolList.size();
    }

    public Symbol get( int index )
    {
        return symbolList.get( index );
    }

    public Symbol getFirst()
    {
        return symbolList.get( 0 );
    }

    public Symbol getLast()
    {
        return symbolList.get( symbolList.size() - 1 );
    }

}
