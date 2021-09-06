package rs.ac.bg.etf.pp1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.structure.SymbolDataStructure;

public class SymbolMap extends SymbolDataStructure implements Iterable<Symbol>, Cloneable
{
    protected LinkedHashMap<String, Symbol> symbolMap = new LinkedHashMap<>();

    public SymbolMap() { }

    public SymbolMap( Collection<Obj> symbols )
    {
        if( symbols == null ) return;
        symbols( symbols );
    }

    public SymbolMap( SymbolDataStructure symbols )
    {
        this( ( symbols != null ) ? symbols.symbols() : null );
    }


    // add a symbol to the symbol map
    public boolean addSymbol( Symbol symbol )
    {
        if( symbol == null ) return false;
        return null == symbolMap.putIfAbsent( symbol.getName(), symbol );
    }
    // remove a symbol with the given name from the symbol map
    public boolean removeSymbol( String name )
    {
        return null != symbolMap.remove( name );
    }
    // get a symbol with the given name from the symbol map
    public Symbol findSymbol( String name )
    {
        Symbol result = symbolMap.get( name );
        return ( result != null ) ? result : SymbolTable.noSym;
    }


    // get the symbols in the symbol map in the order they were added to the map
    public Collection<Symbol> _symbols() { return symbolMap.values(); }
    // set the symbols in the symbol map
    public SymbolMap _symbols( Collection<Symbol> symbols )
    {
        symbolMap.clear();
        if( symbols == null ) return this;

        for( Symbol symbol : symbols )
        {
            if( symbol == null ) continue;
            insertKey( symbol );
        }
        
        return this;
    }

    // get the number of symbols in the symbol map
    public int size() { return symbolMap.size(); }


    // get an iterator through the symbol map elements
    @Override
    public Iterator<Symbol> iterator()
    {
        return symbolMap.values().iterator();
    }

    // check if two symbol maps are equal
    // +   two symbol maps are equal if they have the same elements in the same positions
    public static boolean isEqual( SymbolMap mapA, SymbolMap mapB )
    {
        if( mapA == mapB ) return true;
        if( mapA == null || mapB == null ) return false;
        if( mapA.numSymbols() != mapB.numSymbols() ) return false;

        Iterator<Symbol> iterA = mapA._symbols().iterator();
        Iterator<Symbol> iterB = mapB._symbols().iterator();
        
        while( iterA.hasNext() && iterB.hasNext() )
        {
            Symbol symbolA = iterA.next();
            Symbol symbolB = iterB.next();
            if( !Symbol.isEqual( symbolA, symbolB ) ) return false;
        }

        return true;
    }

    // clone the symbol map
    @Override
    public SymbolMap clone()
    {
        SymbolMap result = new SymbolMap();

        for( Symbol symbol : _symbols() )
        {
            result.addSymbol( symbol.clone() );
        }

        return result;
    }

    // return the symbol map as a string
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for( Obj obj : symbolMap.values() )
        {
            Symbol symbol = ( Symbol )obj;
            builder.append( symbol.toString() ).append( "\n" );
        }

        return builder.toString();
    }





    //___________________________________________________________________________________
    // DEPRECATED METHODS

    // add a symbol to the symbol map
    @Deprecated
    @Override
    public boolean insertKey( Obj symbol )
    {
        return addSymbol( ( Symbol )symbol );
    }

    // remove a symbol with the given name from the symbol map
    @Deprecated
    @Override
    public boolean deleteKey( String name )
    {
        return removeSymbol( name );
    }

    // get a symbol with the given name from the symbol map
    @Deprecated
    @Override
    public Obj searchKey( String name )
    {
        return findSymbol( name );
    }


    // get the symbols in the symbol map in the order they were added to the map
    @Deprecated
    @Override
    public Collection<Obj> symbols()
    {
        return new ArrayList<Obj>( symbolMap.values() );
    }

    // set the symbols in the symbol map
    @Deprecated
    public void symbols( Collection<Obj> symbols )
    {
        symbolMap.clear();
        if( symbols == null ) return;
        
        for( Obj symbol : _symbols() )
        {
            if( symbol == null ) continue;
            insertKey( ( Symbol )symbol );
        }
    }

    // get the number of symbols in the symbol map
    @Deprecated
    @Override
    public int numSymbols()
    {
        return size();
    }
}
