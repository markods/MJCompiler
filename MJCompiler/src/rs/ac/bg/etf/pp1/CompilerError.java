package rs.ac.bg.etf.pp1;

public class CompilerError extends Error
{
    public static final int NO_INDEX = -1;
    public static final int ERROR_TYPES = 5;

    public static final int ARGUMENTS_ERROR = 0;
    public static final int LEXICAL_ERROR   = 1;
    public static final int SYNTAX_ERROR    = 2;
    public static final int SEMANTIC_ERROR  = 3;
    public static final int COMPILE_ERROR   = 4;

    public static String getKindName( int errType )
    {
        switch( errType )
        {
            case ARGUMENTS_ERROR: return "ARG";
            case LEXICAL_ERROR:   return "LEX";
            case SYNTAX_ERROR:    return "SYN";
            case SEMANTIC_ERROR:  return "SEM";
            case COMPILE_ERROR:   return "COM";
            default:              return ".";
        }
    }

    private int kind;
    private String message;
    private int tokenFromIdx;
    private int tokenToIdx;
    private String additionalInfo;

    CompilerError( int kind, String message )
    {
        this( kind, message, NO_INDEX, NO_INDEX );
    }

    CompilerError( int kind, String message, int tokenFromIdx, int tokenToIdx )
    {
        super( message );
        this.kind = kind;
        this.message = message;
        this.tokenFromIdx = tokenFromIdx;
        this.tokenToIdx = tokenToIdx;
        this.additionalInfo = null;
    }

    public int getKind() { return kind; }
    public String getMessage() { return message; }
    public int getTokenFromIdx() { return tokenFromIdx; }
    public int getTokenToIdx() { return tokenToIdx; }
    public String getAdditionalInfo()
    {
        // if additional info should be calculated
        if( additionalInfo == null )
        {
            additionalInfo = "";

            if( Compiler.tokens.checkIndex( tokenFromIdx ) && Compiler.tokens.checkIndex( tokenToIdx ) )
            {
                StringBuilder builder = new StringBuilder( "```" );
                for( int i = tokenFromIdx; i < tokenToIdx; i++ )
                {
                    Token token = Compiler.tokens.get( i );
                    builder.append( token.valueToString() );
                }
                builder.append( "```" );
    
                additionalInfo = builder.toString();
            }
        }

        return additionalInfo;
    }

    // TODO: omoguciti da se moze zvati vise puta main() tako sto se inicijalizacija radi izvan main-a, pa se main pozove
    // TODO: napraviti lep ispis gresaka
    @Override
    public String toString()
    {
        String info = getAdditionalInfo();
        if( !"".equals( info ) ) info = "\n" + info;

        int line = NO_INDEX;
        int col = NO_INDEX;

        if( Compiler.tokens.checkIndex( tokenFromIdx ) )
        {
            Token tokenFrom = Compiler.tokens.get( tokenFromIdx );
            line = tokenFrom.getLine();
            col = tokenFrom.getCol();
        }
        else if( tokenToIdx >= 0 )
        {
            // special case for arguments errors; they don't have a line, but they do have a column
            col = tokenToIdx;
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
