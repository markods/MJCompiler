package rs.ac.bg.etf.pp1;


public class Symbol extends java_cup.runtime.Symbol
{

    public Symbol( int symbolCode, int left, int right, Object value )
    {
        super( symbolCode, left, right, value );
    }

    public Symbol( int symbolCode, Object value )
    {
        super( symbolCode, value );
    }

    public Symbol( int symbolCode, int left, int right )
    {
        super( symbolCode, left, right );
    }

    public Symbol( int symbolCode )
    {
        super( symbolCode );
    }

    public boolean isIgnored()
    {
        return SymbolCode.isIgnored( sym );
    }

    @Override
    public String toString()
    {
        String symbolName = SymbolCode.getSymbolName( sym );
        String symbolValue = ( value != null ) ? "`" + value.toString() + "`" : "``";

        return String.format( "Ln #%-3d Col #%-3d   %-15s %s", left, right, symbolName, symbolValue );
    }
}

