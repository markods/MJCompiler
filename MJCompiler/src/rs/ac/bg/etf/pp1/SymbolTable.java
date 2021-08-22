package rs.ac.bg.etf.pp1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import rs.ac.bg.etf.pp1.util.SystemStreamReplacer;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Scope;
import rs.etf.pp1.symboltable.concepts.Struct;


public class SymbolTable
{
    static
    {
        // initialize the universal scope (the first scope in the symbol table)
        // +   that way, chrObj, ordObj and lenObj can be final
        // +   defined symbols:
        //     +   types [obj]:     int, char
        //     +   constants [obj]: eol, null
        //     +   methods [obj]:   chr( i ), ord( ch ), len( arr )
        Tab.init();

        noType   = Tab.noType;
        intType  = Tab.intType;
        charType = Tab.charType;
        nullType = Tab.nullType;

        noObj  = Tab.noObj;
        chrObj = Tab.chrObj;
        ordObj = Tab.ordObj;
        lenObj = Tab.lenObj;
    
    }

	public static final Struct noType;
	public static final Struct intType;
    public static final Struct charType;
    public static final Struct nullType;
    
	public static final Obj noObj;
	public static final Obj chrObj;
    public static final Obj ordObj;
    public static final Obj lenObj;

    private SymbolTable() {}



    // sets the object's local variables to the ones in the inner scope
    // +   important: the object must not be in the innermost scope! (otherwise the object will contain itself as a local variable!!!)
	public static void chainLocalSymbols( Obj objectt )
    {
        Tab.chainLocalSymbols( objectt );
	}

    // sets the structure's local variables to the ones in the inner scope
    // +   important: the structure must not be in the innermost scope! (otherwise the object will contain itself as a local variable!!!)
	public static void chainLocalSymbols( Struct clazz )
    {
        Tab.chainLocalSymbols( clazz );
	}
	
    // open a new scope
	public static void openScope()
    {
        Tab.openScope();
	}

    // close the most recent scope
	public static void closeScope()
    {
        Tab.closeScope();
	}

    // return the current scope
	public static Scope currentScope()
    {
		return Tab.currentScope();
	}


    // creates a new Obj, adds it to the symbol table and returns it
    // +   if the Obj already exists, return the existing Obj (if it exists, otherwise the default noObj -- should never happen)
    // +   always returns a Obj (noObj instead of null)
	public static Obj insertObj( int kind, String name, Struct type )
    {
        return Tab.insert( kind, name, type );
	}

    // find the Obj with the given name in the symbol table
    // +   start the search from the most recent open scope
    // +   if the object cannot be found in the current scope, go to its parent scope and search there
    // +   return the found object, or noObj if the search was unsuccessful
	public static Obj findObj( String name )
    {
        return Tab.find( name );
	}
	

    
    // return the symbol table as string
	public static String dump()
    {
        String output = null;
        
        try( ByteArrayOutputStream buffer = new ByteArrayOutputStream();
             SystemStreamReplacer replacer = new SystemStreamReplacer( SystemStreamReplacer.STDOUT, buffer );
        )
        {
            // workaround since symbol table dump method only outputs to System.out
            Tab.dump();
            output = buffer.toString( "UTF-8" );
        }
        catch( IOException ex )
        {
            Compiler.errors.add( CompilerError.SEMANTIC_ERROR, "Error during conversion of symbol table to string", ex );
            return null;
        }
        
        return output;
	}
}
