package rs.ac.bg.etf.pp1;

import java.util.ArrayList;

import rs.ac.bg.etf.pp1.props.StackProp;

// import java.io.ByteArrayOutputStream;
// import java.io.IOException;
// import java.util.Stack;

import rs.ac.bg.etf.pp1.util.ScopeGuard;
// import rs.ac.bg.etf.pp1.util.SystemStreamReplacer;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Scope;
import rs.etf.pp1.symboltable.structure.SymbolDataStructure;


public class SymbolTable
{
    // IMPORTANT: don't use the Tab's types and symbols! (for compatibility with Symbol and SymbolType classes)
    // +   actually, don't use the Tab class at all (it's deprecated), use the SymbolTable class instead
    // IMPORTANT: initialize <any type> first, since it's the root type for all other types, and its supertype is itself
    // +   also, most of these symbols are special cases, intended to be used sparingly
    // +   anyType is not intended to be used as Object (like in Java), since it is equivalent to all types (its used for preventing too many semantic errors from being reported)
    // +   voidType is used when no type is expected (void functions)
    // +   nullType is used when a null keyword is found
    public static final SymbolType anyType  = SymbolType.newPrimitive( "@any", SymbolType.ANY_TYPE  );
    public static final SymbolType voidType = SymbolType.newPrimitive( "void", SymbolType.VOID_TYPE );
    public static final SymbolType nullType = SymbolType.newPrimitive( "null", SymbolType.CLASS     );
    public static final SymbolType intType  = SymbolType.newPrimitive( "int",  SymbolType.INT       );
    public static final SymbolType charType = SymbolType.newPrimitive( "char", SymbolType.CHAR      );
    public static final SymbolType boolType = SymbolType.newPrimitive( "bool", SymbolType.BOOL      );
    
    // IMPORTANT: <no symbol> is returned when the symbol table cannot find a symbol
    // +   other symbols are just there for allowing their symbol types to be saved in the symbol table
    public static final Symbol noSym   = Symbol.newType ( "@noSym", anyType,  Symbol.NO_VALUE );
    public static final Symbol anySym  = Symbol.newType ( "@any",   anyType,  Symbol.NO_VALUE );
    public static final Symbol voidSym = Symbol.newType ( "void",   voidType, Symbol.NO_VALUE );
    public static final Symbol nullSym = Symbol.newConst( "null",   nullType, 0               );
    public static final Symbol intSym  = Symbol.newType ( "int",    intType,  Symbol.NO_VALUE );
    public static final Symbol charSym = Symbol.newType ( "char",   charType, Symbol.NO_VALUE );
    public static final Symbol boolSym = Symbol.newType ( "bool",   boolType, Symbol.NO_VALUE );

    // this value is copied over from the Tab.init() method
    private static int currScopeLevel = -2;
    private static Scope global = null;
    // saves all the scopes that were ever created in preorder
    // +   used for printing the symbol table
    private static ArrayList<ScopeInfo> scopeList = new ArrayList<>();
    private static class ScopeInfo
    {
        public ScopeInfo( Scope scope, int level )
        {
            this.scope = scope;
            this.level = level;
        }
        private Scope scope;
        private int level;

        public Scope _scope() { return scope; }
        public int _level() { return level; }
        public SymbolMap _symbols() { return new SymbolMap( scope.getLocals() ); }
    }

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
        Tab.closeScope();
        // initialize the global scope (-1st scope)
        global = openScope();

        // add the global types to the global scope
        global.addToLocals( noSym   );
        global.addToLocals( anySym  );
        global.addToLocals( voidSym );
        global.addToLocals( nullSym );
        global.addToLocals( intSym  );
        global.addToLocals( charSym );
        global.addToLocals( boolSym );
        
        // add a placeholder constructor to the @any type
        // +   that way, all classes that inherit from @any type can inherit the placeholder constructor (which can be called but currently doesn't do anything)
        {
            Symbol constructor = Symbol.newMethod( "@Constructor", voidType, CodeGen.NO_ADDRESS, -1/*methodIdx*/, null );
            anySym._type()._members(  new SymbolMap(){{ addSymbol( constructor ); }}  );
        }

        // char chr( int i );
        try( ScopeGuard guard = new ScopeGuard(); )
        {
            _localScope().addToLocals( Symbol.newFormalParam( "i", intType, 0 ) );
            
            // IMPORTANT: set the method's formal parameters after all locals have been added to the current scope!
            // +   the method's formal parameters aren't automatically updated due to the way the _params function is implemented)
            global.addToLocals( Symbol.newFunction( "chr", charType, Symbol.NO_VALUE, _locals() ) );
        }

        // int ord( char c );
        try( ScopeGuard guard = new ScopeGuard(); )
        {
            _localScope().addToLocals( Symbol.newFormalParam( "c", charType, 0 ) );
            global.addToLocals( Symbol.newFunction( "ord", intType, Symbol.NO_VALUE, _locals() ) );
        }

        // int len( anyType arr[] );
        try( ScopeGuard guard = new ScopeGuard(); )
        {
            SymbolType anyArrayType = SymbolType.newArray( "@anyArray", anyType );
            _localScope().addToLocals( Symbol.newFormalParam( "arr", anyArrayType, 0 ) );
            global.addToLocals( Symbol.newFunction( "len", intType, Symbol.NO_VALUE, _locals() ) );
        }

        // '\n'
        global.addToLocals( Symbol.newConst( "eol", charType, '\n' ) );
    }

    private SymbolTable() {}



    // IMPORTANT: no need to check if the scope is not null, since the symbol table should always be initialized before use
    // +   the only way the scope can be null is if the user calls closeScope() more times than openScope()

    // get the global scope
    public static Scope _globalScope() { return global; }
    // get the global scope's local symbols
    public static SymbolMap _globals() { return new SymbolMap( _globalScope().getLocals() ); }

    // get the current scope
    public static Scope _localScope() { return Tab.currentScope(); }
    // get the current scope's local symbols
    public static SymbolMap _locals() { return new SymbolMap( _localScope().getLocals() ); }
    // get the current scope's level
    public static int _localsLevel() { return currScopeLevel; }
    // get the current scope's size
    public static int _localsSize() { return _locals().size(); }

    // get the number of variables in the current scope
    public static int _localsVarCount() { return _locals().count( elem -> elem.isVar() ); }
    // get the stack frame size in the current scope (this includes formal parameters and variables)
    public static int _localsStackFrameSize() { return _locals().count( elem -> elem.isVar() || elem.isFormalParam() || elem.isThis() ); }
    // get the number of formal parameters in the current scope
    public static int _localsFormalParamCount() { return _locals().count( elem -> elem.isFormalParam() ); }
    // get the number of activation parameters in the current scope
    public static int _localsActivParamCount() { return _locals().count( elem -> elem.isActivParam() ); }

    // get the number of programs in the global scope
    public static int _globalsProgramCount() { return _globals().count( elem -> elem.isProgram() ); }
    // get the number of class declarations in the current scope
    public static int _localsClassCount() { return _locals().count( elem -> elem.isType() && elem._type().isClass() ); }
    // get the number of record declarations in the current scope
    public static int _localsRecordCount() { return _locals().count( elem -> elem.isType() && elem._type().isRecord() ); }
    // get the number of method declarations in the current scope
    public static int _localsMethodCount() { return _locals().count( elem -> elem.isMethod() ); }
    // get the number of static_method declarations in the current scope
    public static int _localsStaticMethodCount() { return _locals().count( elem -> elem.isStaticMethod() ); }
    // get the number of function declarations in the current scope
    public static int _localsFunctionCount() { return _locals().count( elem -> elem.isFunction() ); }

    // IMPORTANT: !isGlobalScope does not work as expected if the symbol's scope is an invalid value (Symbol.NO_VALUE)
    // check if this scope is the global or program scope
    public static boolean isGlobalScope() { return isGlobalScope( currScopeLevel ); }
    // check if the scope with the given level is the global or program scope
    public static boolean isGlobalScope( int scopeLevel ) { return scopeLevel == -1 || scopeLevel == 0; }


    // open a new scope
    public static Scope openScope()
    {
        Tab.openScope();
        currScopeLevel++;
        scopeList.add( new ScopeInfo( _localScope(), currScopeLevel ) );

        return _localScope();
    }
    // close the most recent scope
    // +   stop when the global scope is reached
    public static Scope closeScope()
    {
        // prevent the global scope from being closed
        if( _localScope() == global )
        {
            scopeList.clear();
            scopeList.add( new ScopeInfo( _localScope(), currScopeLevel ) );
            return global;
        }
        
        Scope curr = _localScope();
        Tab.closeScope();
        currScopeLevel--;

        return curr;
    }


    // try to add the given symbol to the symbol table and return if the addition was successful
    public static boolean addSymbol( Symbol symbol )
    {
        if( symbol == null || symbol.isNoSym() ) return false;
        Symbol existing = findSymbol( symbol._name() );

        // if a type with the given name has already been defined, and is not redefinable, this symbol cannot redefine it or hide it
        if( existing != noSym
            && existing._kind() == Symbol.TYPE
            && !existing._type().isPrimitiveType()
        ) return false;

        // if a system type with the same name already exists, prevent it from being redefined
        if(
         // existing == noSym    ||   // this symbol cannot be redefined
            existing == anySym   ||
            existing == voidSym  ||
            existing == nullSym  //
         // existing == intSym   ||   // redefinable
         // existing == charSym  ||   // redefinable
         // existing == boolSym       // redefinable
        )
        return false;

        // return if the symbol has been added to the current scope
        // +   restore the old symbol address, because for some reason the addToLocals() method changes the symbol's address if it is a field
        int address = symbol._address();
        boolean result = _localScope().addToLocals( symbol );
        symbol._address( address );

        return result;
    }

    // try to add the given symbol map to the symbol table
    // +   return the index of the element that wasn't added successfully (-1 if everything is ok)
    public static int addSymbols( SymbolMap symbols )
    {
        int i = 0;
        for( Symbol symbol : symbols )
        {
            if( !addSymbol( symbol ) )
            {
                return i;
            }
        }

        return -1;
    }

    // find the symbol with the given name in the symbol table
    // +   start the search from the most recent open scope
    // +   if the object cannot be found in the current scope, go to its parent scope and search there
    // +   return the found object, or noSymbol if the search was unsuccessful
    public static Symbol findSymbol( String name )
    {
        Symbol symbol = null;
        
        for( Scope curr = _localScope(); curr != null; curr = curr.getOuter() )
        {
            SymbolDataStructure locals = curr.getLocals();
            if( locals == null ) continue;

            symbol = ( Symbol )locals.searchKey( name );
            // if a match has been found, break
            if( symbol != null ) break;
        }

        return ( symbol != null ) ? symbol : noSym;
    }



    // return the symbol table as string
    public static String asString()
    {
        StackProp<Scope> scopeStack = new StackProp<>();
        for( Scope scope = _localScope(); scope != null;   )
        {
            scopeStack.add( scope );
            scope = scope.getOuter();
        }
        
        StringBuilder builder = new StringBuilder();
        builder.append( "=========================SYMBOL TABLE==========================\n" );
        for( ScopeInfo curr : scopeList )
        {
            // start at the -1'st (global) scope
            String scopeName = ( curr._level() > -1 ) ? String.format( "Scope[%d]", curr._level() ) : "Global";
            String scopeNameExt = "\n";
            if( curr._scope() == scopeStack.top() ) { scopeNameExt =    " *\n"; scopeStack.remove(); }
            if( curr._scope() == _localScope()         ) { scopeNameExt = " <---\n";  }

            builder.append( "--------------------------------------------------------------- <<< " )
                .append( scopeName ).append( scopeNameExt )
                .append( curr._symbols().toString( curr._level() + 1 ) );
        }

        return builder.toString();
    }

    // // return the symbol table as string
    // public static String dump_old()
    // {
    //     String output = null;
        
    //     try( ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    //          SystemStreamReplacer replacer = new SystemStreamReplacer( SystemStreamReplacer.STDOUT, buffer );
    //     )
    //     {
    //         // workaround since symbol table dump method only outputs to System.out
    //         Tab.dump();
    //         output = buffer.toString( "UTF-8" );
    //     }
    //     catch( IOException ex )
    //     {
    //         Compiler.errors.add( CompilerError.SEMANTIC_ERROR, "Error during conversion of symbol table to string", ex );
    //         return null;
    //     }
        
    //     return output;
    // }

}
