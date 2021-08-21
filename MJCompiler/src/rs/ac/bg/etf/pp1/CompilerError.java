package rs.ac.bg.etf.pp1;

public class CompilerError
{
    public static final int NO_INDEX = -1;

    public static final int ARGUMENTS_ERROR = 0;
    public static final int LEXICAL_ERROR   = 1;
    public static final int SYNTAX_ERROR    = 2;
    public static final int SEMANTIC_ERROR  = 3;

    public static String getKindName( int errType )
    {
        switch( errType )
        {
            case ARGUMENTS_ERROR: return "ARG";
            case LEXICAL_ERROR:   return "LEX";
            case SYNTAX_ERROR:    return "SYN";
            case SEMANTIC_ERROR:  return "SEM";
            default:              return ".";
        }
    }

    private int kind;
    private String message;
    private int symbolFromIdx;
    private int symbolToIdx;
    private String additionalInfo;

    CompilerError( int kind, String message )
    {
        this( kind, message, NO_INDEX, NO_INDEX );
    }

    CompilerError( int kind, String message, int symbolFromIdx, int symbolToIdx )
    {
        this.kind = kind;
        this.message = message;
        this.symbolFromIdx = symbolFromIdx;
        this.symbolToIdx = symbolToIdx;
        this.additionalInfo = null;
    }

    public int getKind() { return kind; }
    public String getMessage() { return message; }
    public int getSymbolFromIdx() { return symbolFromIdx; }
    public int getSymbolToIdx() { return symbolToIdx; }
    public String getAdditionalInfo()
    {
        // if additional info should be calculated
        if( additionalInfo == null )
        {
            additionalInfo = "";

            if( Compiler.symbols.checkIndex( symbolFromIdx ) && Compiler.symbols.checkIndex( symbolToIdx ) )
            {
                StringBuilder builder = new StringBuilder( "```" );
                for( int i = symbolFromIdx; i < symbolToIdx; i++ )
                {
                    Symbol token = Compiler.symbols.get( i );
                    builder.append( token.getValue() );
                }
                builder.append( "```" );
    
                additionalInfo = builder.toString();
            }
        }

        return additionalInfo;
    }

    @Override
    public String toString()
    {
        String info = getAdditionalInfo();
        if( info != "" ) info = "\n" + info;

        int line = NO_INDEX;
        int col = NO_INDEX;

        if( Compiler.symbols.checkIndex( symbolFromIdx ) )
        {
            Symbol symbolFrom = Compiler.symbols.get( symbolFromIdx );
            line = symbolFrom.getLine();
            col = symbolFrom.getCol();
        }
        else if( symbolToIdx >= 0 )
        {
            // special case for arguments errors; they don't have a line, but they do have a column
            col = symbolToIdx;
        }

        return String.format( "Ln %-3s Col %-3s %-3s     %s%s",
            ( line >= 0 ? line : "." ),
            ( col  >= 0 ? col  : "." ),
            getKindName( kind ),
            message,
            info
        );
    }
}
