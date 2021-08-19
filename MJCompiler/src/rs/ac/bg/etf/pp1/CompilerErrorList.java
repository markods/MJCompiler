package rs.ac.bg.etf.pp1;

import java.util.ArrayList;

import rs.ac.bg.etf.pp1.CompilerError.CompilerErrorType;

public class CompilerErrorList {
    private ArrayList<CompilerError> errors = new ArrayList<>();

    public CompilerErrorList() {}


    public boolean add( int line, int col, String message, CompilerErrorType type )
    {
        return errors.add( new CompilerError( line, col, message, type ) );
    }
    
    public boolean add( CompilerError error )
    {
        return errors.add( error );
    }

    public CompilerError getLast()
    {
        return ( errors.size() > 0 ) ? errors.get( errors.size() - 1 ) : null;
    }

    public void clear()
    {
        errors.clear();
    }
    
    public ArrayList<CompilerError> list()
    {
        return errors;
    }

    public boolean hasErrors()
    {
        return !errors.isEmpty();
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for( CompilerError error : errors )
        {
            builder.append( error.toString() ).append( "\n" );
        }

        return builder.toString();
    }

}
