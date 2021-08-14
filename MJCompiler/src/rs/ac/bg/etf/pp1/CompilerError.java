package rs.ac.bg.etf.pp1;

public class CompilerError
{
    public enum CompilerErrorType
    {
        ARGUMENTS_ERROR, LEXICAL_ERROR, SYNTAX_ERROR, SEMANTIC_ERROR, RUNTIME_ERROR;
        
        public static String getTypeName( CompilerErrorType errType )
        {
            switch( errType )
            {
                case ARGUMENTS_ERROR: return "ARG";
                case LEXICAL_ERROR:   return "LEX";
                case SYNTAX_ERROR:    return "SYN";
                case SEMANTIC_ERROR:  return "SEM";
                case RUNTIME_ERROR:   return "RUN";
                default:              return "?";
            }
        }
    };

    private int line;
    private int col;
    private String message;
    private CompilerErrorType type;

    public CompilerError( int line, int col, String message, CompilerErrorType type )
    {
        this.line = line;
        this.col = col;
        this.message = message;
        this.type = type;
    }

    public void setLine( int line )
    {
        this.line = line;
    }

    public int getLine()
    {
        return line;
    }

    public void setCol( int col )
    {
        this.col = col;
    }

    public int getCol()
    {
        return col;
    }

    public String getMessage()
    {
        return message;
    }

    public CompilerErrorType getType()
    {
        return type;
    }

    @Override
    public String toString()
    {
        return String.format( "Ln %-4s Col %-4s %-3s   %s", ( line >= 0 ? "#" + line : "?" ), ( col >= 0 ? "#" + line : "?" ), CompilerErrorType.getTypeName( type ), message );
    }
}
