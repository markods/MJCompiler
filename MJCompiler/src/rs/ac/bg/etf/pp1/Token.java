package rs.ac.bg.etf.pp1;


public class Token extends java_cup.runtime.Symbol
{
    // inherited fields
 // public int tokenCode;   // === sym

 // public int tokenIdx;    // === left
 // public int line;        // === right
    public int column;
 // public Object value;
 
 // public int parse_state;
 // boolean used_by_parser;


    public Token( int tokenCode, int tokenIdx, int line, int column, Object value )
    {
     // super( sym,        left,    right, obj                           );
        super( tokenCode, tokenIdx, line, ( value != null ) ? value : "" );
        this.column = column;
    }

    public boolean isIgnored()    { return TokenCode.isIgnored( sym ); }
    public boolean isInvalid()    { return TokenCode.isInvalid( sym ); }
    public boolean isWhitespace() { return TokenCode.isWhitespace( sym ); }
    public boolean isNewline()    { return TokenCode.isNewline( sym ); }
    public boolean isEOF()        { return TokenCode.isEOF( sym ); }
    
    public int _code() { return sym; }
    public String _name() { return TokenCode.getTokenName( sym ); }
    public int _idx() { return left; }
    public int _line() { return right; }
    public int _column() { return column; }
    public Object _value( boolean shouldParse )
    {
        if( !shouldParse ) return value;

        switch( sym )
        {
            case TokenCode.int_lit:  return Integer.parseInt( ( String )value );
            case TokenCode.bool_lit: return Boolean.parseBoolean( ( String )value );
            case TokenCode.char_lit: return ( ( String )value ).charAt( 1 );
            default:                 return value;
        }
    }


    @Override
    public String toString()
    {
        return String.format( "Ln %-3d Col %-3d Idx %-3d     %-15s `%s`",
            _line(), _column(), _idx(), _name(), _value( false/*shouldParse*/ )
        );
    }
}

