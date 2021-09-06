package rs.ac.bg.etf.pp1;

import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.structure.SymbolDataStructure;


public class Symbol extends Obj implements Cloneable
{
    public static final int NO_VALUE = 0;

    public static final int CONST        = Obj.Con;
    public static final int VAR          = Obj.Var;
    public static final int TYPE         = Obj.Type;
    public static final int METHOD       = Obj.Meth;
    public static final int FIELD        = Obj.Fld;
    public static final int ARRAY_ELEM   = Obj.Elem;
    public static final int PROGRAM      = Obj.Prog;
    public static final int STATIC_FIELD = 10;
    public static final int FUNCTION     = 11;
    public static final int FORMAL_PARAM = 12;

    // // symbol name
    // private String name;

    // // Con, Var, Type, Meth, Fld, Prog
    // private int kind;

    // // tip pridruzen imenu
    // private Struct type;
    
    // // konstanta(Con): vrednost
    // // Meth, Var, Fld: memorijski ofset
    // private int adr;

    // // Var: nivo ugnezdavanja
    // // Meth: broj formalnih argumenata
    // private int level;

    // // Meth: redni broj formalnog argumenta u definiciji metode
    // private int fpPos;

    // // Meth: kolekcija lokalnih promenljivih
    // // Prog: kolekcija simbola programa
    // private SymbolDataStructure locals;
    
    private Symbol( int kind, String name, SymbolType type, int address, int level, int index, SymbolDataStructure locals )
    {
        super( kind, name, type, address, level );
        this._paramIdx( index );
        // IMPORTANT: set the locals last, since this function updates the number of formal parameters as well
        this._locals( locals );
    }

    // NO_VALUE, CONST, VAR, TYPE, METHOD, FIELD, ARR_ELEM, PROGRAM
    public static Symbol newConst      ( String name, SymbolType type, int value )                                              { return new Symbol( CONST,        name, type, value,    NO_VALUE,   NO_VALUE,  null   ); }
    public static Symbol newVar        ( String name, SymbolType type, int address, int scopeLevel )                            { return new Symbol( VAR,          name, type, address,  scopeLevel, NO_VALUE,  null   ); }
    public static Symbol newStaticField( String name, SymbolType type, int address, int memberIdx )                             { return new Symbol( STATIC_FIELD, name, type, address,  NO_VALUE,   memberIdx, null   ); }
    public static Symbol newField      ( String name, SymbolType type, int address, int memberIdx )                             { return new Symbol( FIELD,        name, type, address,  NO_VALUE,   memberIdx, null   ); }
    public static Symbol newMethod     ( String name, SymbolType type, int address, int memberIdx, SymbolDataStructure locals ) { return new Symbol( METHOD,       name, type, address,  NO_VALUE,   memberIdx, locals ); }
    public static Symbol newFunction   ( String name, SymbolType type, int address, SymbolDataStructure locals )                { return new Symbol( FUNCTION,     name, type, address,  NO_VALUE,   NO_VALUE,  locals ); }
    public static Symbol newFormalParam( String name, SymbolType type, int paramIdx, int scopeLevel )                           { return new Symbol( FORMAL_PARAM, name, type, NO_VALUE, scopeLevel, paramIdx,  null   ); }
    public static Symbol newType       ( String name, SymbolType type )                                                         { return new Symbol( TYPE,         name, type, NO_VALUE, NO_VALUE,   NO_VALUE,  null   ); }
    public static Symbol newArrayElem  ( String name, SymbolType type )                                                         { return new Symbol( ARRAY_ELEM,   name, type, NO_VALUE, NO_VALUE,   NO_VALUE,  null   ); }
    public static Symbol newProgram    ( String name, SymbolType type, SymbolDataStructure locals )                             { return new Symbol( PROGRAM,      name, type, NO_VALUE, NO_VALUE,   NO_VALUE,  locals ); }
    

    // NO_VALUE, CONST, VAR, TYPE, METHOD, FIELD, ARR_ELEM, PROGRAM
    public int _kind() { return getKind(); }
    public String _name() { return getName(); }
    public SymbolType _type() { return ( SymbolType )getType(); }

    // CONST
    public int _value() { return getAdr(); }
    public Symbol _value( int value ) { setAdr( value ); return this; }
    // VAR, FIELD, METHOD
    public int _address() { return getAdr(); }
    public Symbol _address( int address ) { setAdr( address ); return this; }

    // VAR
    public int _scopeLevel() { return getLevel(); }
    public Symbol _scopeLevel( int scopeLevel ) { setLevel( scopeLevel ); return this; }
    // METHOD
    public int _paramCount() { return getLevel(); }
    private Symbol _paramCount( int paramCount ) { setLevel( paramCount ); return this; }

    // FORMAL PARAM (variable)
    public int _paramIdx() { return getFpPos(); }
    public Symbol _paramIdx( int paramIdx ) { setFpPos( paramIdx ); return this; }
    // FIELD, METHOD
    public int _memberIdx() { return getFpPos(); }
    public Symbol _memberIdx( int memberIdx ) { setFpPos( memberIdx ); return this; }
    
    // METHOD
    public SymbolMap _params() { return new SymbolMap( getLocalSymbols() ); }
    public Symbol _params( SymbolMap params ) { return _symbols( params ); }
    public Symbol _params( SymbolDataStructure params ) { return _symbols( new SymbolMap( params ) ); }
    // PROGRAM
    public SymbolMap _locals() { return new SymbolMap( getLocalSymbols() ); }
    public Symbol _locals( SymbolMap locals ) { return _symbols( locals ); }
    public Symbol _locals( SymbolDataStructure locals ) { return _symbols( new SymbolMap( locals ) ); }
    
    // set the symbols as locals or parameters depending on the symbol's kind
    private Symbol _symbols( SymbolMap symbols )
    {
        _paramCount( ( symbols != null ) ? symbols.size() : NO_VALUE );
        setLocals( symbols );

        return this;
    }


    public boolean isConst()       { return _kind() == CONST;        }
    public boolean isVar()         { return _kind() == VAR;          }
    public boolean isStaticField() { return _kind() == STATIC_FIELD; }
    public boolean isField()       { return _kind() == FIELD;        }
    public boolean isMethod()      { return _kind() == METHOD;       }
    public boolean isFunction()    { return _kind() == FUNCTION;     }
    public boolean isFormalParam() { return _kind() == FORMAL_PARAM; }
    public boolean isType()        { return _kind() == TYPE;         }
    public boolean isArrayElem()   { return _kind() == ARRAY_ELEM;   }
    public boolean isProgram()     { return _kind() == PROGRAM;      }

    public boolean isNoSym()       { return this == SymbolTable.noSym; }
    public boolean isLvalue()      { return                     _kind() == VAR || _kind() == FIELD || _kind() == STATIC_FIELD || _kind() == ARRAY_ELEM; }
    public boolean isRvalue()      { return _kind() == CONST || _kind() == VAR || _kind() == FIELD || _kind() == STATIC_FIELD || _kind() == ARRAY_ELEM; }


    public static boolean isEqual( Symbol symbolA, Symbol symbolB )
    {
        if( symbolA == symbolB ) return true;
        if( symbolA == null || symbolB == null ) return false;

        return symbolA._kind()       == symbolB._kind()
            && symbolA._name().equals(  symbolB._name() )
            && symbolA._address()    == symbolB._address()
            && symbolA._scopeLevel() == symbolB._scopeLevel()
            && symbolA._paramIdx()   == symbolB._paramIdx()
            && SymbolType.isEqual( symbolA._type(),   symbolB._type()   )   // this check is placed near the end for performance reasons
            && SymbolMap .isEqual( symbolA._locals(), symbolB._locals() );
    }

    @Override
    public Symbol clone()
    {
        return new Symbol( _kind(), _name(), _type(), _address(), _scopeLevel(), _memberIdx(), _locals().clone() );
    }





    //___________________________________________________________________________________
    // DEPRECATED METHODS

    @Deprecated
    @Override
    public boolean equals( Object obj )
    {
        if( !( obj instanceof Symbol ) ) return false;
        return isEqual( this, ( Symbol )obj );
    }


}
