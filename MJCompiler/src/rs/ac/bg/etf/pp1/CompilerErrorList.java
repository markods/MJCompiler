package rs.ac.bg.etf.pp1;

import java.util.ArrayList;

import rs.ac.bg.etf.pp1.CompilerError.CompilerErrorType;

public class CompilerErrorList
{
    private final ArrayList<CompilerError> errorList = new ArrayList<>();

    public CompilerErrorList() {}


    public boolean add( int line, int col, String message, CompilerErrorType type )
    {
        return errorList.add( new CompilerError( line, col, message, type ) );
    }
    
    public boolean add( CompilerError error )
    {
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
    
    public ArrayList<CompilerError> list()
    {
        return errorList;
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
