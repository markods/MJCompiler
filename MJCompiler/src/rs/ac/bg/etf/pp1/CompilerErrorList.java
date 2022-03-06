package rs.ac.bg.etf.pp1;

import java.util.ArrayList;

import rs.ac.bg.etf.pp1.ast.SyntaxNode;
import rs.ac.bg.etf.pp1.util.Log4J;
import rs.ac.bg.etf.pp1.visitors.ScopeVisitor;


public class CompilerErrorList
{
    private final ArrayList<CompilerError> errorList = new ArrayList<>();
    private final Compiler.State state;
    private int errorsSinceLastCheck = 0;

    public CompilerErrorList( Compiler.State state )
    {
        this.state = state;
    }



    // IMPORTANT: helper method, not intended to be used elsewhere
    private boolean add( int kind, String message, int tokenFromIdx, int tokenToIdx, boolean logLastErrorInstead, Throwable throwable )
    {
        CompilerError error = new CompilerError( state, kind, message, tokenFromIdx, tokenToIdx );
        errorsSinceLastCheck++;

        if( logLastErrorInstead ) { throwable = error; }

        if( throwable != null ) { state._logger().log( Log4J.ERROR, error.toString(), throwable, true ); }
        else                    { state._logger().log( Log4J.ERROR, error.toString(),            true ); }

        return errorList.add( error );
    }

    public boolean add( int kind, String message )                                                          { return add( kind, message, CompilerError.NO_INDEX, CompilerError.NO_INDEX, false, null      ); }
    public boolean add( int kind, String message, Throwable throwable )                                     { return add( kind, message, CompilerError.NO_INDEX, CompilerError.NO_INDEX, false, throwable ); }
    public boolean add( int kind, String message, int tokenStartIdx, int tokenEndIdx )                      { return add( kind, message, tokenStartIdx,          tokenEndIdx,            false, null      ); }
    public boolean add( int kind, String message, int tokenStartIdx, int tokenEndIdx, Throwable throwable ) { return add( kind, message, tokenStartIdx,          tokenEndIdx,            false, throwable ); }

    public boolean add( int kind, String message, SyntaxNode node, boolean entireScope, boolean throwError )
    {
        ScopeVisitor scopeVisitor = new ScopeVisitor();
        node.accept( scopeVisitor );

        int tokenStartIdx = scopeVisitor.getTokenStartIdx();
        int tokenEndIdx = ( entireScope ) ? scopeVisitor.getTokenEndIdx() : tokenStartIdx;

        boolean res = add( kind, message, tokenStartIdx, tokenEndIdx, throwError, null );
        if( throwError ) throw getLast();

        return res;
    }

    public void clear()
    {
        errorList.clear();
    }



    public boolean noErrors()
    {
        return errorList.isEmpty();
    }
    public int size()
    {
        return errorList.size();
    }
    public boolean noErrorsSinceLastCheck()
    {
        if( errorsSinceLastCheck > 0 )
        {
            errorsSinceLastCheck = 0;
            return false;
        }
        return true;
    }

    public boolean checkIndex( int index )
    {
        return index >= 0 && index < errorList.size();
    }

    public CompilerError get( int index )
    {
        if( !checkIndex( index ) ) throw new IndexOutOfBoundsException( "<Compiler error list>'s index out of bounds" );
        return errorList.get( index );
    }
    public CompilerError getLast()
    {
        return get( errorList.size() - 1 );
    }
    
    

    @Override
    public String toString()
    {
        int[] count = new int[ CompilerError.ERROR_TYPES ];
        boolean notOnlyArgErrors = false;
        
        StringBuilder builder = new StringBuilder();
        for( CompilerError error : errorList )
        {
            if( error.getKind() != CompilerError.ARGUMENTS_ERROR )
            {
                notOnlyArgErrors = true;
            }

            builder.append( error.toString() ).append( "\n" );
            count[ error.getKind() ]++;
        }

        if( notOnlyArgErrors )
        {
            builder.append(
                String.format( "Lexical: %-3d Syntax: %-3d Semantic: %-3d\n",
                count[ CompilerError.LEXICAL_ERROR ],
                count[ CompilerError.SYNTAX_ERROR ],
                count[ CompilerError.SEMANTIC_ERROR ]
            ) );
        }

        return builder.toString();
    }

}
