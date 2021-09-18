package rs.ac.bg.etf.pp1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Predicate;

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
    // add the symbols from the given symbol map
    // +   return the index of the element that wasn't added successfully (-1 if everything is ok)
    public int addSymbols( SymbolMap symbols )
    {
        int i = 0;
        for( Symbol symbol : symbols )
        {
            if( !addSymbol( symbol ) )
            {
                return i;
            }
        }

        return -1;
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
    public List<Symbol> _sorted()
    {
        ArrayList<Symbol> result = new ArrayList<>( symbolMap.values() );
        result.sort( ( symbolA, symbolB ) -> { return symbolA._memberIdx() - symbolB._memberIdx(); } );
        return result;
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

    // count the number of positive occurences given the match function
    public int count( Predicate<Symbol> matcher )
    {
        int count = 0;
        for( Symbol symbol : _symbols() )
        {
            if( matcher.test( symbol ) ) count++;
        }
        return count;
    }

    // clear the symbol map
    public void clear() { symbolMap.clear(); }
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
        if( mapA.size() != mapB.size() ) return false;

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
    public String toString( int level )
    {
        String prefix = "";
        {
            StringBuilder prefixBuilder = new StringBuilder( "" );
            for( int i = 0; i < level; i++ )
            {
                prefixBuilder.append( "    " );
            }
            prefix = prefixBuilder.toString();
        }

        StringBuilder builder = new StringBuilder();
        for( Symbol symbol : symbolMap.values() )
        {
            builder.append( symbol.toString( prefix ) );
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
        return new ArrayList<>( symbolMap.values() );
    }

    // set the symbols in the symbol map
    @Deprecated
    public void symbols( Collection<Obj> symbols )
    {
        symbolMap.clear();
        if( symbols == null ) return;
        
        for( Obj symbol : symbols )
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
