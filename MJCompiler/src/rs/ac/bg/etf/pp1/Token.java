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

    public boolean isIgnored()    { return TokenCode.isIgnored( sym ); }
    public boolean isInvalid()    { return TokenCode.isInvalid( sym ); }
    public boolean isWhitespace() { return TokenCode.isWhitespace( sym ); }
    public boolean isNewline()    { return TokenCode.isNewline( sym ); }
    public boolean isEOF()        { return TokenCode.isEOF( sym ); }
    
    public String getValue()
    {
        if( value == null ) return "";
        if( sym == TokenCode.char_lit ) return "'" + value + "'";
        if( sym == TokenCode.bool_lit ) return ( ( Boolean )value ) ? "true" : "false";
        return value.toString();
    }
    // NOTE: works as expected as long as the starting character doesn't overwrite a tab character! (or some other weirdness)
    // +   meaning don't provide a starting character for a whitespace token
    // +   this shouldn't happen in practise though
    public String getUnderline( char startChar )
    {
        if( value == null  ) return "";
        if( isNewline()    ) return value.toString();
        if( isWhitespace() ) return value.toString();

        char[] chars = getValue().toCharArray();
        // if the starting character is anything other than a whitespace, place it as the first character
        if( startChar != ' ' ) { chars[ 0 ] = startChar; }

        // for all other characters
        for( int i = 1; i < chars.length; i++ )
        {
            // skip tab characters
            if( chars[ i ] == '\t' ) continue;
            // replace other characters with a space
            chars[ i ] = ' ';
        }

        // return the underline for the token
        return chars.toString();
    }

    public int _code() { return sym; }
    public String _name() { return TokenCode.getTokenName( sym ); }
    public int _idx() { return left; }
    public int _line() { return right; }
    public int _column() { return column; }
    public Object _value() { return value; }


    @Override
    public String toString()
    {
        return String.format( "Ln %-3d Col %-3d Idx %-3d     %-15s `%s`",
            _line(), _column(), _idx(), _name(), getValue()
        );
    }
}

