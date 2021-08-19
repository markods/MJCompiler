package rs.ac.bg.etf.pp1;


public class Symbol extends java_cup.runtime.Symbol
{
    // java_cup.runtime.Symbol's fields
 // public int symbolCode;   // === sym

 // public int symbolIdx;    // === left
 // public int line;         // === right
    public int column;
 // public Object value;     // === obj
 
 // public int parse_state;
 // boolean used_by_parser;


    public Symbol( int symbolCode, int symbolIdx, int line, int column, Object value )
    {
     // super( sym,        left,      right, obj );
        super( symbolCode, symbolIdx, line, value );
        this.column = column;
    }

    public boolean isIgnored() { return SymbolCode.isIgnored( sym ); }
    public boolean isInvalid() { return SymbolCode.isInvalid( sym ); }

    @Override
    public String toString()
    {
        String symbolName = SymbolCode.getSymbolName( sym );
        String symbolValue = ( value != null ) ? "`" + value.toString() + "`" : "``";

        return String.format( "Ln %-3d Col %-3d Idx %-3d     %-15s %s", right, column, left, symbolName, symbolValue );
    }
}

