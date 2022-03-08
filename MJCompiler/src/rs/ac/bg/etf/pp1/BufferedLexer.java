package rs.ac.bg.etf.pp1;

import java.io.IOException;
import java.io.Reader;

import java_cup.runtime.Symbol;


public class BufferedLexer implements AutoCloseable
{
    private static final int PREFETCH_LINE  = 30;
    private static final int PREFETCH_TOKEN = 10;

    private final TokenList tokens = new TokenList();
    private final Lexer lexer;
    private final Reader reader;
    private boolean isEndOfFileSet = false;
    private String cachedTokenString = null;


    public BufferedLexer( Reader reader )
    {
        this.reader = reader;
        this.lexer = new Lexer( reader );
    }

    public TokenIterator newTokenIterator()               { return new TokenIterator(); }
    public TokenIterator newTokenIterator( int startIdx ) { return new TokenIterator( startIdx ); }
    public LineIterator newLineIterator()                 { return new LineIterator(); }
    public LineIterator newLineIterator( int startIdx )   { return new LineIterator( startIdx ); }



    // get the token at the given index
    // NOTE: throws an exception if the wanted token hasn't yet been lexed
    public Token get( int index )
    {
        // if the wanted token hasn't yet been lexed or doesn't exist, throw an error
        if( !tokens.checkIndex( index ) ) throw new AssertionError( "The wanted token either doesn't exist or has not yet been lexed" );
        // get the wanted token
        return tokens.get( index );
    }

    // prefetch tokens from the lexer
    private void prefetch( int prefetchCount )
    {
        // if there definitely aren't any more tokens, return
        if( isEndOfFileSet ) return;

        try
        {
            // try to prefetch this many tokens
            for( int i = 0; i < prefetchCount; i++ )
            {
                // add the current token to the list
                Token token = ( Token )lexer.next_token();
                tokens.add( token );

                // if there aren't any more tokens, the reader should be closed
                if( token.isEOF() ) { ensureEndOfFileIsSet(); break; }
            }
        }
        // if an io exception occured
        catch( IOException ex )
        {
            // the reader should be closed
            ensureEndOfFileIsSet();
        }
    }

    // ensure the end-of-file token is at the end of the token list
    private void ensureEndOfFileIsSet()
    {
        // if the end of file has already been set, return
        if( isEndOfFileSet ) return;

        // if the token list is empty
        if( !tokens.hasLast() )
        {
            // add an artificial eof token at the end
            Token eofToken = new Token( TokenCode.EOF, 0/*idx*/, /*line*/1, /*column*/0, /*value*/"" );
            tokens.add( eofToken );
        }
        // otherwise, if the last token in the list is not eof
        else if( !tokens.getLast().isEOF() )
        {
            // add an artificial eof token at the end
            Token lastToken = tokens.getLast();
            Token eofToken = new Token( TokenCode.EOF, tokens.lastIdx() + 1, lastToken._line() + 1, lastToken._column(), /*value*/"" );
            tokens.add( eofToken );
        }

        // save that the end of file has been set
        isEndOfFileSet = true;
    }

    // close the underlying reader and add the end-of-file token (if it is missing)
    @Override
    public void close() throws IOException
    {
        ensureEndOfFileIsSet();
        // try to close the reader
        // NOTE: this potentially throws an io exception
        this.reader.close();
    }

    // write the tokens to a string
    public String tokensToString()
    {
        // if the token string has already been made, return it
        if( cachedTokenString != null ) return cachedTokenString;

        // if the token string is wanted before the entire file has been lexed, throw an error
        if( !isEndOfFileSet ) throw new AssertionError( "The token list should not be toString()-d until it is complete (has an eof-token at its end)" );

        // save the token string and return it
        cachedTokenString = tokens.toString();
        return cachedTokenString;
    }



    // iterate over the token list one token at a time
    // +   also fetch new tokens if needed
    public class TokenIterator implements java_cup.runtime.Scanner
    {
        private int currIdx;

        public TokenIterator()
        {
            this( 0/*startIdx*/ );
        }
        public TokenIterator( int startIdx/*0,1,2...*/ )
        {
            // if the current token is not yet lexed, or doesn't exist, throw an error
            // NOTE: allow for the first token to not yet be lexed
            if( startIdx != 0 && !tokens.checkIndex( startIdx ) ) throw new AssertionError( "The starting token in the token iterator either doesn't exist or has not yet been lexed" );
            this.currIdx = startIdx;
        }

        // getters and setters
        public int _currIdx() { return currIdx; }


        // get the current token
        public Token currToken()
        {
            // if the token list is completely empty, do the initial prefetch
            if( tokens.size() == 0 ) prefetch( PREFETCH_TOKEN );
            // get the current token
            return tokens.get( currIdx );
        }
        // check if there is a next token
        public boolean hasCurrToken()
        {
            // return if the current iterator has not yet reached the end of the list (which tries to lex more tokens when any iterator reaches its apparent end)
            // NOTE: allow for the first token to not yet be lexed
            return !isEndOfFileSet || currIdx <= tokens.lastIdx();
        }

        // go to the next token
        // NOTE: always return the eof token when there isn't anything more to lex
        public void nextToken()
        {
            // if there definitely aren't any more tokens, return
            if( !hasCurrToken() ) return;

            // if the end of the token list is reached, but there are probably more tokens, try to lex them
            // NOTE: we haven't hit the eof token yet, so at least one token exists after the current token
            if( currIdx+1 >= tokens.lastIdx() ) prefetch( PREFETCH_TOKEN );

            // go to the next token
            currIdx++;
        }
        @Deprecated
        @Override
        // get the next token if it exists
        public Symbol next_token()
        {
            // if there isn't a next token
            if( !hasCurrToken() )
            {
                // make an artificial eof token after the actual eof token
                Token lastToken = tokens.getLast();
                Token fakeEofToken = new Token( TokenCode.EOF, tokens.lastIdx() + 1, lastToken._line(), lastToken._column() + 1, /*value*/"" );
                tokens.add( fakeEofToken );
            }

            // get the current token and advance to the next one
            Token token = currToken(); nextToken();
            // return the current token
            return token;
        }
    }

    // iterate over the token list one line at a time
    // +   also fetch new tokens if needed
    public class LineIterator
    {
        private int currIdx;
        private int currLine;
        private String cachedValue = null;

        public LineIterator()
        {
            this( 0/*startIdx*/ );
        }
        public LineIterator( int startIdx/*0,1,2...*/ )
        {
            // if the current token is not yet lexed, or doesn't exist, throw an error
            // NOTE: allow for the first token to not yet be lexed
            if( startIdx != 0 && !tokens.checkIndex( startIdx ) ) throw new AssertionError( "The starting token in the line iterator either doesn't exist or has not yet been lexed" );

            // set the current token's index and line
            this.currIdx = startIdx;
            this.currLine = ( this.currIdx > 0 ) ? tokens.get( this.currIdx )._line() : 1;
            // rewind until the start of the line
            // NOTE: this updates the current index
            rewindUptoThisManyLines( 0 );
        }

        // getters and setters
        public int _currIdx() { return currIdx; }
        public int _currLine() { return currLine; }


        // rewind upto the given number of lines until the line's starting token is found
        public void rewindUptoThisManyLines( int rewindCnt )
        {
            // get the wanted line
            currLine = Math.max( 1, currLine - rewindCnt );

            // if the wanted line is the first line in the file
            if( currLine == 1 )
            {
                // use defaults for the first line and return
                this.currIdx = 0;
                this.currLine = 1;
                return;
            }

            // search for the start of the wanted line
            while( true )
            {
                Token token = tokens.get( currIdx );

                // if the current token is on a line immediately before the wanted line
                if( token._line() < currLine )
                {
                    // don't count that token and stop the search
                    currIdx++; break;
                }

                // if there isn't a previous token, stop the search
                if( !tokens.checkIndex( currIdx-1 ) ) break;

                // go to the previous token
                currIdx--;
            }
        }

        // get the current line
        public String currLine()
        {
            // if the line has already been processed, return the cached version
            if( cachedValue != null ) return cachedValue;

            // if the token list is completely empty, do the initial prefetch
            if( tokens.size() == 0 ) prefetch( PREFETCH_LINE );
            // if there definitely aren't any more tokens, return
            if( !hasCurrLine() ) { cachedValue = ""; return cachedValue; }

            // initialize the token string builder and the next line's starting token's index
            StringBuilder builder = new StringBuilder();
            int nextIdx = currIdx;
            
            // search for the start of the next line
            while( true )
            {
                // get the current token
                Token token = tokens.get( nextIdx );

                // if the current token is an eof, stop the search
                if( token.isEOF() ) break;
                // if the current token is on the next line, stop the search
                if( token._line() > currLine ) break;
                
                // add the token to the token string builder
                builder.append( token.getValue() );

                // if the next token isn't yet lexed, try to lex it
                // NOTE: we haven't hit the eof token yet, so at least one token exists after the current token
                if( nextIdx+1 >= tokens.lastIdx() ) prefetch( PREFETCH_LINE );

                // go to the next token
                nextIdx++;
            }
            // if the last character isn't a newline, add it
            if( builder.length() != 0 && builder.charAt( builder.length() - 1 ) != '\n' ) builder.append( "\n" );

            // cache the resulting line
            cachedValue = builder.toString();
            if( cachedValue == null ) { cachedValue = ""; }

            // return the cached value
            return cachedValue;
        }
        // get the current line's underline
        public String currUnderline( int markerStartIdx, char markerStartChar, int markerEndIdx, char markerEndChar )
        {
            // get the underline for the current line
            StringBuilder builder = new StringBuilder( currLine().replaceAll( "[^\t\r\n]", " ") );

            // if the <error end token> to be marked is on this line
            if( tokens.checkIndex( markerEndIdx ) && tokens.get( markerEndIdx )._line() == currLine )
            {
                // get the token's column in the current line
                int col = tokens.get( markerEndIdx )._column();
                // replace the character at that line with the given character
                builder.replace( col, col + 1, Character.toString( markerEndChar ) );
            }

            // if the <error start token> to be marked is on this line
            if( tokens.checkIndex( markerStartIdx ) && tokens.get( markerStartIdx )._line() == currLine )
            {
                // get the token's column in the current line
                int col = tokens.get( markerStartIdx )._column();
                // replace the character at that line with the given character
                builder.replace( col, col + 1, Character.toString( markerStartChar ) );
            }

            // return the actual underline
            return builder.toString();
        }
        // check if there is a next line
        public boolean hasCurrLine()
        {
            // return if the current iterator has not yet reached the end of the list (which tries to lex more tokens when any iterator reaches its apparent end)
            // NOTE: allow for the first token to not yet be lexed
            return !isEndOfFileSet || currIdx <= tokens.lastIdx();
        }

        // go to the next line
        public void nextLine()
        {
            // if the token list is completely empty, do the initial prefetch
            if( tokens.size() == 0 ) prefetch( PREFETCH_LINE );
            // if there definitely aren't any more tokens, return
            if( !hasCurrLine() ) return;

            // initialize the next line's starting token's index
            int nextIdx = currIdx;
            
            // search for the start of the next line
            while( true )
            {
                // get the current token
                Token token = tokens.get( nextIdx );

                // if the current token is an eof, stop the search
                if( token.isEOF() ) break;
                // if the current token is on the next line, stop the search
                if( token._line() > currLine ) break;
                
                // if the next token isn't yet lexed, try to lex it
                // NOTE: we haven't hit the eof token yet, so at least one token exists after the current token
                if( nextIdx+1 >= tokens.lastIdx() ) prefetch( PREFETCH_LINE );

                // go to the next token
                nextIdx++;
            }

            // save the next line's starting index as the current line's starting index
            currIdx = nextIdx;
            currLine++;
            cachedValue = null;
        }
    }
}
