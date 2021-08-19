package rs.ac.bg.etf.pp1;

public class CompilerError
{
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

    private int line;
    private int col;
    private String message;
    private int kind;

    CompilerError( int line, int col, String message, int kind )
    {
        this.line = line;
        this.col = col;
        this.message = message;
        this.kind = kind;
    }

    public int getLine() { return line; }
    public int getCol() { return col; }
    public String getMessage() { return message; }
    public int getKind() { return kind; }

    @Override
    public String toString()
    {
        return String.format( "Ln %-3s Col %-3s %-3s     %s",
            ( line >= 0 ? line : "." ),
            ( col  >= 0 ? col  : "." ),
            getKindName( kind ),
            message
        );
    }
}
