package rs.ac.bg.etf.pp1;

import java.util.ArrayList;
import rs.ac.bg.etf.pp1.util.Log4J;


public class CompilerErrorList
{
    private final ArrayList<CompilerError> errorList = new ArrayList<>();

    public CompilerErrorList() {}


    public boolean add( int line, int col, String message, int kind )
    {
        CompilerError error = new CompilerError( line, col, message, kind );
        Compiler.logger.log( Log4J.ERROR, error.toString(), true );
        return errorList.add( error );
    }

    public boolean add( int line, int col, String message, int kind, Throwable throwable )
    {
        CompilerError error = new CompilerError( line, col, message, kind );
        Compiler.logger.log( Log4J.ERROR, error.toString(), throwable, true );
        return errorList.add( error );
    }

    public CompilerError getLast()
    {
        return ( errorList.size() > 0 ) ? errorList.get( errorList.size() - 1 ) : null;
    }

    public void clear()
    {
        errorList.clear();
    }

    public boolean hasErrors()
    {
        return !errorList.isEmpty();
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for( CompilerError error : errorList )
        {
            builder.append( error.toString() ).append( "\n" );
        }

        return builder.toString();
    }

}
