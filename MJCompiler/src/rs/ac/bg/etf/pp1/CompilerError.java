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

    private static final char ERROR_LINE_START_CHAR = '>';
    private static final char ERROR_LINE_END_CHAR   = '*';
    private static final char ERROR_COL_START_CHAR  = '^';
    private static final char ERROR_COL_END_CHAR    = '*';

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
    public String getKindName() { return getKindName( kind ); }
    public String getMessage() { return message; }
    public int getErrorStartIdx() { return errorStartIdx; }
    public int getErrorEndIdx() { return errorEndIdx; }
    public String getHostErrorMessage()
    {
        // if there is a cached error message, return it
        if( cachedHostErrorMessage != null ) return cachedHostErrorMessage;
        // create the builder for the cached error message
        StringBuilder builder = new StringBuilder();
        
        // standard compiler errors (that have something to underline)
        if( errorStartIdx != NO_INDEX )
        {
            Token tokenFrom = state._lexer().get( errorStartIdx );
            builder.append( String.format( "%s:%d:%d: %s: ", state.getInputFileName(), tokenFrom._line(), tokenFrom._column(), getKindName() ) );
        }
        // special case for arguments errors; they don't have a line, but they do have a column
        else if( errorEndIdx != NO_INDEX )
        {
            builder.append( String.format( "%s:%d: ", getKindName(), errorEndIdx ) );
        }

        // add the error message
        builder.append( message ).append( "\n" );

        // if the compiler error has something to underline
        if( errorStartIdx != NO_INDEX && errorEndIdx != NO_INDEX )
        {
            // get the line iterator
            BufferedLexer.LineIterator iterator = state._lexer().newLineIterator( errorStartIdx );

            // get the error's start and end line
            int errorStartLine = state._lexer().get( errorStartIdx )._line();
            int errorEndLine   = state._lexer().get( errorEndIdx   )._line();

            // get the <error scope>'s start and end line
            // NOTE: rewind some lines to widen the error context
            int scopeExtraLines = ( errorEndLine - errorStartLine <= 2 ) ? 1 : 3;
            iterator.rewindUptoThisManyLines( scopeExtraLines );
            int scopeStartLine = iterator._currLine();
            int scopeEndLine   = errorEndLine   + scopeExtraLines;
            
            // for all error context lines
            for( int currLine = scopeStartLine;  iterator.hasCurrLine() && currLine <= scopeEndLine;  iterator.nextLine(), currLine++ )
            {
                // calculate the line prefix and if the current line should be underlined
                String linePrefix = " ";
                boolean getUnderline = false;
                if     ( currLine == errorStartLine ) { linePrefix = ERROR_LINE_START_CHAR + " " + currLine; getUnderline = true;  }
                else if( currLine == errorEndLine   ) { linePrefix = ERROR_LINE_END_CHAR   + " " + currLine; getUnderline = true;  }
                else                                  { linePrefix = " "                   + " " + currLine; getUnderline = false; }

                // write the line prefix and the line
                builder.append( String.format( "%6s | ", linePrefix ) ).append( iterator.currLine() );

                // if the current line should be underlined
                if( getUnderline )
                {
                    // write the underline prefix and the underline
                    linePrefix = "";
                    builder.append( String.format( "%6s | ", linePrefix ) ).append( iterator.currUnderline( errorStartIdx, ERROR_COL_START_CHAR, errorEndIdx, ERROR_COL_END_CHAR ) );
                }
            }
        }

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
