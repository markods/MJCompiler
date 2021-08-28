package rs.ac.bg.etf.pp1;

import java.util.ArrayList;
import rs.ac.bg.etf.pp1.util.Log4J;


public class CompilerErrorList
{
    private ArrayList<CompilerError> errorList = new ArrayList<>();

    public CompilerErrorList() {}

    public void assign( CompilerErrorList errors )
    {
        this.errorList = errors.errorList;
    }



    public boolean add( int kind, String message )
    {
        return add( kind, message, CompilerError.NO_INDEX, CompilerError.NO_INDEX );
    }

    public boolean add( int kind, String message, Throwable throwable )
    {
        return add( kind, message, CompilerError.NO_INDEX, CompilerError.NO_INDEX, throwable );
    }

    public boolean add( int kind, String message, int tokenFromIdx, int tokenToIdx )
    {
        CompilerError error = new CompilerError( kind, message, tokenFromIdx, tokenToIdx );
        Compiler.logger.log( Log4J.ERROR, error.toString(), true );
        return errorList.add( error );
    }

    public boolean add( int kind, String message, int tokenFromIdx, int tokenToIdx, Throwable throwable )
    {
        CompilerError error = new CompilerError( kind, message, tokenFromIdx, tokenToIdx );
        Compiler.logger.log( Log4J.ERROR, error.toString(), throwable, true );
        return errorList.add( error );
    }

    public void clear()
    {
        errorList.clear();
    }



    public int size()
    {
        return errorList.size();
    }

    public boolean checkIndex( int index )
    {
        return index >= 0 && index < errorList.size();
    }

    public CompilerError get( int index )
    {
        return errorList.get( index );
    }

    public CompilerError getLast()
    {
        return ( errorList.size() > 0 ) ? errorList.get( errorList.size() - 1 ) : null;
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
