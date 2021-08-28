package rs.ac.bg.etf.pp1;

import java.io.IOException;
import java.io.Reader;


public class BufferedLexer implements java_cup.runtime.Scanner 
{
    private static final int prefetch_max = 100;

    private final Lexer lexer;
    private final TokenList tokens;
    private int lastTokenIdx = 0;
    private boolean isEOF = false;


    public BufferedLexer( Reader in )
    {
        this.lexer = new Lexer( in );
        this.tokens = new TokenList();
    }
    
    public BufferedLexer( TokenList tokens )
    {
        this.lexer = null;
        this.tokens = tokens;
    }


    public TokenList getTokens() { return tokens; }

    @Override
    public Token next_token() throws IOException
    {
        if( lexer != null && lastTokenIdx >= tokens.size() && !isEOF )
        {
            for( int i = 0; i < prefetch_max; i++ )
            {
                Token token = ( Token )lexer.next_token();
                tokens.add( token );

                if( token.containsNewline() ) break;
                if( token.isEOF() ) { isEOF = true; break; }
            }
        }

        if( lastTokenIdx >= tokens.size() )
        {
            Token lastToken = tokens.getLast();
            tokens.add( new Token( TokenCode.EOF, lastToken.getIdx() + 1, lastToken.getLine(), lastToken.getCol(), lastToken.getValue() ) );
        }

        return tokens.get( lastTokenIdx++ );
    }
    
}
