package rs.ac.bg.etf.pp1;

import java.io.IOException;
import java.io.Reader;


public class BufferedLexer implements java_cup.runtime.Scanner 
{
    private static final int prefetch_max = 100;

    private final Lexer lexer;
    private final SymbolList symbols;
    private int lastSymbolIdx = 0;
    private boolean isEOF = false;


    public BufferedLexer( Reader in )
    {
        this.lexer = new Lexer( in );
        this.symbols = new SymbolList();
    }
    
    public BufferedLexer( SymbolList symbols )
    {
        this.lexer = null;
        this.symbols = symbols;
    }


    public SymbolList getSymbols() { return symbols; }

    @Override
    public Symbol next_token() throws IOException
    {
        if( lexer != null && lastSymbolIdx >= symbols.size() && !isEOF )
        {
            for( int i = 0; i < prefetch_max; i++ )
            {
                Symbol symbol = ( Symbol )lexer.next_token();
                symbols.add( symbol );

                if( symbol.containsNewline() ) break;
                if( symbol.isEOF() ) { isEOF = true; break; }
            }
        }

        if( lastSymbolIdx >= symbols.size() )
        {
            Symbol lastSymbol = symbols.getLast();
            symbols.add( new Symbol( SymbolCode.EOF, lastSymbol.getIdx() + 1, lastSymbol.getLine(), lastSymbol.getCol(), lastSymbol.getValue() ) );
        }

        return symbols.get( lastSymbolIdx++ );
    }
    
}
