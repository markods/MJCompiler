package rs.ac.bg.etf.pp1;

public class CompilerError extends Error
{
    public static final int NO_INDEX = -1;
    public static final int ERROR_TYPES = 5;

    public static final int ARGUMENTS_ERROR = 0;
    public static final int LEXICAL_ERROR   = 1;
    public static final int SYNTAX_ERROR    = 2;
    public static final int SEMANTIC_ERROR  = 3;
    public static final int CODEGEN_ERROR   = 4;

    public static String getKindName( int errType )
    {
        switch( errType )
        {
            case ARGUMENTS_ERROR: return "Argument";
            case LEXICAL_ERROR:   return "Lexical";
            case SYNTAX_ERROR:    return "Syntax";
            case SEMANTIC_ERROR:  return "Semantic";
            case CODEGEN_ERROR:   return "Codegen";
            default:              return "Compiler";
        }
    }

    private Compiler.State state;
    private int kind;
    private String message;
    private int errorStartIdx;
    private int errorEndIdx;
    private String cachedHostErrorMessage;

    public CompilerError( Compiler.State state, int kind, String message, int errorStartIdx, int errorEndIdx )
    {
        super( message );
        this.state = state;
        this.kind = kind;
        this.message = message;
        this.errorStartIdx = errorStartIdx;
        this.errorEndIdx = errorEndIdx;
    }

    public int getKind() { return kind; }
    public String getMessage() { return message; }
    public int getErrorStartIdx() { return errorStartIdx; }
    public int getErrorEndIdx() { return errorEndIdx; }
    public String getHostErrorMessage()
    {
        // if there is a cached error message, return it
        if( cachedHostErrorMessage != null ) return cachedHostErrorMessage;
        // create the builder for the cached error message
        StringBuilder builder = new StringBuilder();
        
        // // standard compiler errors (that have something to underline)
        // if( state.tokens.checkIndex( errorStartIdx ) )
        // {
        //     Token tokenFrom = state.tokens.get( errorStartIdx );
        //     builder.append( String.format( "%s:%d:%d: %s: ", state.getInputFileName(), tokenFrom._line(), tokenFrom._column(), getKindName( kind ) ) );
        // }
        // // special case for arguments errors; they don't have a line, but they do have a column
        // else if( state.tokens.checkIndex( errorEndIdx ) )
        // {
        //     builder.append( String.format( "%s:%d: ", getKindName( kind ), errorEndIdx ) );
        // }

        // // add the error message
        // builder.append( message ).append( "\n" );

        // // if the compiler error has something to underline
        // if( state.tokens.checkIndex( errorStartIdx ) && state.tokens.checkIndex( errorEndIdx ) )
        // {
        //     int firstLine = 1;
        //     int lastLine  = state.tokens.get( state.tokens.size()-1 )._line();
        //     int extraScopeLines = 2;

        //     // get the line numbers on which the error starts and ends, and also the line numbers of the widened error scope
        //     int errorStartLine = state.tokens.get( errorStartIdx )._line();
        //     int errorEndLine   = state.tokens.get( errorEndIdx   )._line();
        //     int scopeStartLine = Math.max( firstLine, errorStartLine - extraScopeLines           );
        //     int scopeEndLine   = Math.min(            errorEndLine   + extraScopeLines, lastLine );

        //     // get the indexes of the tokens that are first and last in the scope
        //  // int errorFromIdx  = errorFromIdx;
        //  // int errorToIdx    = errorToIdx;
        //     int scopeStartIdx = errorStartIdx;
        //  // int scopeEndIdx   = errorToIdx /*not needed*/;
            
        //     // get the <scope starting token>'s index (after newline or the first token in the file)
        //     if( scopeStartLine == firstLine )
        //     {
        //         scopeStartIdx = 0/*first token in the token list*/;
        //     }
        //     else while( true )
        //     {
        //         // if the current token is on a line immediately before the <scope starting line>
        //         if( state.tokens.get( scopeStartIdx )._line() < scopeStartLine )
        //         {
        //             // don't count that token and stop the search
        //             scopeStartIdx++; break;
        //         }

        //         // if there isn't a previous token, stop the search
        //         if( !state.tokens.checkIndex( scopeStartIdx-1 )  ) break;
        //         // go to the previous token
        //         scopeStartIdx--;
        //     }

        //     // the index of the current token in the token list, the current token's line, and the line's starting index
        //     int currIdx = scopeStartIdx;
        //     int currLine = scopeStartLine;
        //     int currLineIdx = scopeStartIdx;
        //     // if the current line should be underlined
        //     boolean needsUnderline = false;
        //     // for all error context lines
        //     while( currLine <= scopeEndLine )
        //     {
        //         // save the current line's starting index
        //         currLineIdx = currIdx;

        //         // calculate the line prefix and if the current line should be underlined
        //         String linePrefix = null;
        //         if     ( currLine == errorStartLine ) { linePrefix = "> " + currLine; }
        //         else if( currLine == errorEndLine   ) { linePrefix = "* " + currLine; }
        //         else                                  { linePrefix = " "; }

        //         // write the line header
        //         builder.append( String.format( "%6s | ", linePrefix ) );

        //         // write the current error context line
        //         while( true )
        //         {
        //             // if the current token doesn't exist, break
        //             if( !state.tokens.checkIndex( currIdx ) ) break;

        //             // get the current token
        //             Token token = state.tokens.get( currIdx );
        //             // if the current token is a newline, go to the next token
        //             if( token.isNewline() ) { currIdx++; continue; }

        //             // if the token doesn't need to be underlined
        //             if( !needsUnderline )
        //             {
        //                 // write the token's value
        //                 builder.append( token.getValue() );
        //             }
        //             // otherwise, if the token should be underlined
        //             else
        //             {
        //                 // get the token's underline string
        //                 char startChar = ' ';
        //                 if     ( currIdx == errorStartIdx ) { startChar = '^'; }
        //                 else if( currIdx == errorEndIdx   ) { startChar = '*'; }
        //                 // write the underline string
        //                 builder.append( token.getUnderline( startChar ) );
        //             }

        //             // go to the next token
        //             currIdx++;
        //         }
        //         // always append a newline after the last token in the line, since we skip newlines
        //         builder.append( "\n" );


        //         // if the current line is already underlined
        //         if( needsUnderline )
        //         {
        //             // save that the current line is finished being underlined
        //             needsUnderline = false;
        //         }
        //         // otherwise, if the current line contains the error start or end token
        //         else if( currLine == errorStartLine || currLine == errorEndLine )
        //         {
        //             // save that it should be underlined and its starting position
        //             needsUnderline = true;
        //             // restore the current line's starting index
        //             currIdx = currLineIdx;
        //         }


        //         // if the now current line <is finished being underlined>/<shouldn't be underlined>
        //         if( !needsUnderline )
        //         {
        //             // go to the next line
        //             currLine++;
        //         }
        //     }
        // }

        // create the cached error message and return it
        cachedHostErrorMessage = builder.toString();
        return cachedHostErrorMessage;
    }

    @Override
    public String toString()
    {
        return getHostErrorMessage();
    }
}
