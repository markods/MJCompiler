package rs.ac.bg.etf.pp1;

import java.util.ArrayList;

import rs.ac.bg.etf.pp1.CompilerError.CompilerErrorType;

public class CompilerErrors {
    private ArrayList<CompilerError> errors = new ArrayList<>();

    public CompilerErrors() {}


    public boolean add( int line, String message, CompilerErrorType type )
    {
        return errors.add( new CompilerError( line, message, type ) );
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
            builder.append( error.toString() );
        }

        return builder.toString();
    }

}
