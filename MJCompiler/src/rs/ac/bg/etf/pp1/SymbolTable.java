package rs.ac.bg.etf.pp1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import rs.ac.bg.etf.pp1.util.ScopeGuard;
import rs.ac.bg.etf.pp1.util.SystemStreamReplacer;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Scope;
import rs.etf.pp1.symboltable.structure.SymbolDataStructure;


public class SymbolTable
{
    // IMPORTANT: don't use the Tab's types and symbols! (for compatibility with Symbol and SymbolType classes)
    // +   actually, don't use the Tab class at all (it's deprecated), use the SymbolTable class instead
    // IMPORTANT: initialize anyType first, since it's the root type for all other types, and its supertype is itself
    public static final SymbolType anyType  = SymbolType.newPrimitive( "@anyType",  SymbolType.ANY_TYPE );
    public static final SymbolType anyArrayType = SymbolType.newArray( "@anyArray", anyType             );
    public static final SymbolType intType  = SymbolType.newPrimitive( "int",       SymbolType.INT      );
    public static final SymbolType charType = SymbolType.newPrimitive( "char",      SymbolType.CHAR     );
    public static final SymbolType boolType = SymbolType.newPrimitive( "bool",      SymbolType.BOOL     );
    public static final SymbolType nullType = SymbolType.newPrimitive( "class",     SymbolType.CLASS    );
    public static final Symbol noSym   = Symbol.newConst( "@noSym", anyType, 0 );
    public static final Symbol voidSym = Symbol.newConst( "void", anyType, 0 );

    // this value is copied over from the Tab.init() method
    private static int currScopeLevel = -1;

    static
    {
        init();
    }

    private static void init()
    {
        // initialize the universal scope (the first scope in the symbol table)
        // +   that way, chrObj, ordObj and lenObj can be final
        // +   defined symbols:
        //     +   types [obj]:     int, char
        //     +   constants [obj]: eol, null
        //     +   methods [obj]:   chr( i ), ord( ch ), len( arr )
        Tab.init();
        // throw away what was initialized, but keep the scope level as -2
        closeScope();
        // initialize the global scope (-1st scope)
        Scope global = openScope();

        // add the global types to the global scope
        global.addToLocals( Symbol.newType( "@any", anyType  ) );
        global.addToLocals( Symbol.newType( "int",  intType  ) );
        global.addToLocals( Symbol.newType( "char", charType ) );
        global.addToLocals( Symbol.newType( "bool", boolType ) );
        global.addToLocals( Symbol.newType( "null", nullType ) );
        global.addToLocals( noSym );
        global.addToLocals( voidSym );
    
        // char chr( int i );
        try( ScopeGuard guard = new ScopeGuard(); )
        {
            _scope().addToLocals( Symbol.newFormalParam( "i", intType, 0, _scopeLevel() ) );
            
            // IMPORTANT: set the method's formal parameters after all locals have been added to the current scope!
            // +   the method's formal parameters aren't automatically updated due to the way the _params function is implemented)
            global.addToLocals( Symbol.newFunction( "chr", charType, Symbol.NO_VALUE, _locals() ) );
        }

        // int ord( char c );
        try( ScopeGuard guard = new ScopeGuard(); )
        {
            _scope().addToLocals( Symbol.newFormalParam( "c", charType, 0, _scopeLevel() ) );
            global.addToLocals( Symbol.newFunction( "ord", intType, Symbol.NO_VALUE, _locals() ) );
        }

        // int len( anyType arr[] );
        try( ScopeGuard guard = new ScopeGuard(); )
        {
            _scope().addToLocals( Symbol.newFormalParam( "arr", anyArrayType, 0, _scopeLevel() ) );
            global.addToLocals( Symbol.newFunction( "len", intType, Symbol.NO_VALUE, _locals() ) );
        }

        // '\n'
        try( ScopeGuard guard = new ScopeGuard(); )
        {
            global.addToLocals( Symbol.newConst( "eol", charType, '\n' ) );
        }
    }

    private SymbolTable() {}



    // get the current scope
    public static Scope _scope() { return Tab.currentScope(); }
    // get the current scope's local symbols
    public static SymbolDataStructure _locals() { return _scope().getLocals(); }
    // get the current scope's level
    public static int _scopeLevel() { return currScopeLevel; }
    
    // open a new scope
    public static Scope openScope()
    {
        Tab.openScope();
        currScopeLevel++;

        return _scope();
    }
    // close the most recent scope
    public static void closeScope()
    {
        if( _scope() == null ) return;

        Tab.closeScope();
        currScopeLevel--;
    }


    // try to add the given symbol to the symbol table and return if the addition was successful
    public static boolean addSymbol( Symbol symbol )
    {
        if( symbol == null ) return false;
        Symbol existing = findSymbol( symbol._name() );

        // if a type with the given name has already been defined, and is not redefinable, this symbol cannot redefine it or hide it
        if( existing != noSym
            && existing._kind() == Symbol.TYPE
            && !existing._type().isPrimitiveType()
        ) return false;

        // return if the symbol has been added to the current scope
        return _scope().addToLocals( symbol );
    }

    // find the symbol with the given name in the symbol table
    // +   start the search from the most recent open scope
    // +   if the object cannot be found in the current scope, go to its parent scope and search there
    // +   return the found object, or noSymbol if the search was unsuccessful
    public static Symbol findSymbol( String name )
    {
        Symbol symbol = null;
        
        for( Scope curr = _scope(); curr != null; curr = curr.getOuter() )
        {
            SymbolDataStructure locals = curr.getLocals();
            if( locals == null ) break;

            symbol = ( Symbol )locals.searchKey( name );
            // if a match has been found, break
            if( symbol != null ) break;
        }

        return ( symbol != null ) ? symbol : noSym;
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
