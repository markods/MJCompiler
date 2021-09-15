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
     // super( sym,        left,      right, obj );
        super( tokenCode, tokenIdx, line, value );
        this.column = column;
    }

    public boolean isIgnored() { return TokenCode.isIgnored( sym ); }
    public boolean isInvalid() { return TokenCode.isInvalid( sym ); }
    public boolean containsNewline() { return TokenCode.containsNewline( this ); }
    public boolean isEOF() { return TokenCode.isEOF( sym ); }
    public String valueToString()
    {
        if( value == null ) return "";
        if( sym == TokenCode.char_lit ) return "'" + value + "'";
        if( sym == TokenCode.bool_lit ) return ( ( Boolean )value ) ? "true" : "false";
        return value.toString();
    }

    public int getCode() { return sym; }
    public String getName() { return TokenCode.getTokenName( sym ); }
    public int getIdx() { return left; }
    public int getLine() { return right; }
    public int getCol() { return column; }
    public Object getValue() { return value; }


    @Override
    public String toString()
    {
        return String.format( "Ln %-3d Col %-3d Idx %-3d     %-15s `%s`",
            getLine(), getCol(), getIdx(), getName(), valueToString()
        );
    }
}

