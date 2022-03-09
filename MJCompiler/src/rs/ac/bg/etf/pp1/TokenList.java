package rs.ac.bg.etf.pp1;

import java.util.ArrayList;

public class TokenList
{
    private ArrayList<Token> tokenList = new ArrayList<>();

    public TokenList() {}



    public boolean add( Token token )
    {
        return tokenList.add( token );
    }

    public void removeLast()
    {
        if( tokenList.size() > 0 ) tokenList.remove( tokenList.size() - 1 );
    }

    public void clear()
    {
        tokenList.clear();
    }



    public int size()
    {
        return tokenList.size();
    }
    public int lastIdx()
    {
        return ( tokenList.size() >= 1 ) ? tokenList.size() - 1 : 0;
    }

    public boolean checkIndex( int index )
    {
        return index >= 0 && index < tokenList.size();
    }
    public boolean hasFirst()
    {
        return checkIndex( 0 );
    }
    public boolean hasLast()
    {
        return checkIndex( lastIdx() );
    }

    public Token get( int index )
    {
        if( !checkIndex( index ) ) throw new IndexOutOfBoundsException( "The wanted token's index is outside the token list" );
        return tokenList.get( index );
    }
    public Token getFirst()
    {
        return tokenList.get( 0 );
    }
    public Token getLast()
    {
        return tokenList.get( lastIdx() );
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for( Token token : tokenList )
        {
            builder.append( token._value( false/*shouldParse*/ ) );
        }
        if( builder.length() != 0 && builder.charAt( builder.length() - 1 ) != '\n' ) builder.append( "\n" );
        return builder.toString();
    }

}
