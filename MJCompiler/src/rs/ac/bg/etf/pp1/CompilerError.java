package rs.ac.bg.etf.pp1;

public class CompilerError
{
    public enum CompilerErrorType
    {
        ARGUMENTS_ERROR, LEXICAL_ERROR, SYNTAX_ERROR, SEMANTIC_ERROR, RUNTIME_ERROR;
        
        String getErrorName( CompilerErrorType errType )
        {
            switch( errType )
            {
                case ARGUMENTS_ERROR: return "ARG";
                case LEXICAL_ERROR:   return "LEX";
                case SYNTAX_ERROR:    return "SYN";
                case SEMANTIC_ERROR:  return "SEM";
                case RUNTIME_ERROR:   return "RUN";
                default:              return "UNK";
            }
        }
    };

    private int line;
    private String message;
    private CompilerErrorType type;

    public CompilerError( int line, String message, CompilerErrorType type )
    {
        this.line = line;
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
        return String.format( "[Line #%-3d] [%3s]: %s", line, type, message );
    }
}
