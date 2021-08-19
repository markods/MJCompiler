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

    public void clear()
    {
        symbolList.clear();
    }

}
