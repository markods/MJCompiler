package rs.ac.bg.etf.pp1;

import java.util.Iterator;
import java.util.List;

import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.structure.SymbolDataStructure;


public class Symbol extends Obj implements Cloneable
{
    public static final int NO_VALUE = -999;

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
    public static final int ACTIV_PARAM  = 13;

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
        this._symbols( locals );
    }

    // CONST, VAR, STATIC_FIELD, FIELD, METHOD, FUNCTION, FORMAL_PARAM, ACTIV_PARAM, TYPE, ARRAY_ELEM, PROGRAM
    public static Symbol newConst      ( String name, SymbolType type, int value )                                    { return new Symbol( CONST,        name, type, value,    NO_VALUE,   NO_VALUE,  null   ); }
    public static Symbol newVar        ( String name, SymbolType type, int address, int scopeLevel, int varIdx )      { return new Symbol( VAR,          name, type, address,  scopeLevel, varIdx,    null   ); }
    public static Symbol newStaticField( String name, SymbolType type, int address, int memberIdx )                   { return new Symbol( STATIC_FIELD, name, type, address,  NO_VALUE,   memberIdx, null   ); }
    public static Symbol newField      ( String name, SymbolType type, int address, int memberIdx )                   { return new Symbol( FIELD,        name, type, address,  NO_VALUE,   memberIdx, null   ); }
    public static Symbol newMethod     ( String name, SymbolType type, int address, int memberIdx, SymbolMap params ) { return new Symbol( METHOD,       name, type, address,  NO_VALUE,   memberIdx, params ); }
    public static Symbol newFunction   ( String name, SymbolType type, int address, SymbolMap params )                { return new Symbol( FUNCTION,     name, type, address,  NO_VALUE,   NO_VALUE,  params ); }
    public static Symbol newFormalParam( String name, SymbolType type, int paramIdx )                                 { return new Symbol( FORMAL_PARAM, name, type, NO_VALUE, NO_VALUE,   paramIdx,  null   ); }
    public static Symbol newActivParam ( String name, SymbolType type, int paramIdx )                                 { return new Symbol( ACTIV_PARAM,  name, type, NO_VALUE, NO_VALUE,   paramIdx,  null   ); }
    public static Symbol newType       ( String name, SymbolType type, int address )                                  { return new Symbol( TYPE,         name, type, address,  NO_VALUE,   NO_VALUE,  null   ); }
    public static Symbol newArrayElem  ( String name, SymbolType type )                                               { return new Symbol( ARRAY_ELEM,   name, type, NO_VALUE, NO_VALUE,   NO_VALUE,  null   ); }
    public static Symbol newProgram    ( String name, SymbolType type, SymbolMap locals )                             { return new Symbol( PROGRAM,      name, type, NO_VALUE, NO_VALUE,   NO_VALUE,  locals ); }
    

    // CONST, VAR, STATIC_FIELD, FIELD, METHOD, FUNCTION, FORMAL_PARAM, ACTIV_PARAM, TYPE, ARRAY_ELEM, PROGRAM
    public int _kind() { return getKind(); }
    public String _name() { return getName(); }
    public SymbolType _type() { return ( SymbolType )getType(); }

    // CONST
    public int _value() { return getAdr(); }
    public Symbol _value( int value ) { setAdr( value ); return this; }
    // VAR, STATIC_FIELD, FIELD, METHOD, FUNCTION, TYPE<CLASS>
    public int _address() { return getAdr(); }
    public Symbol _address( int address ) { setAdr( address ); return this; }

    // VAR
    public int _scopeLevel() { return getLevel(); }
    public Symbol _scopeLevel( int scopeLevel ) { setLevel( scopeLevel ); return this; }
    // METHOD, FUNCTION
    public int _paramCount() { return _params().size(); }
    // TYPE<CLASS>
    public int _fieldCount() { return _locals().count( elem -> elem.isField() ); }
    // TYPE<CLASS>
    public int _virtualTableSize() { return CodeGen.getVirtualTableSize( this ); }

    // VAR, FORMAL_PARAM (since the formal parameter is also a variable)
    public int _varIdx() { return getFpPos(); }
    public Symbol _varIdx( int varIdx ) { setFpPos( varIdx ); return this; }
    // FORMAL_PARAM, ACTIV_PARAM
    public int _paramIdx() { return getFpPos(); }
    public Symbol _paramIdx( int paramIdx ) { setFpPos( paramIdx ); return this; }
    // STATIC FIELD, FIELD, METHOD
    public int _memberIdx() { return getFpPos(); }
    public Symbol _memberIdx( int memberIdx ) { setFpPos( memberIdx ); return this; }
    
    // METHOD, FUNCTION
    public SymbolMap _params() { return _symbols(); }
    public Symbol _params( SymbolMap params ) { return _symbols( params ); }
    public Symbol _params( SymbolDataStructure params ) { return _symbols( params ); }
    // TYPE<CLASS>
    public SymbolMap _members() { return _symbols(); }
    public Symbol _members( SymbolMap members ) { return _symbols( members ); }
    public Symbol _members( SymbolDataStructure members ) { return _symbols( members ); }
    // PROGRAM
    public SymbolMap _locals() { return _symbols(); }
    public Symbol _locals( SymbolMap locals ) { return _symbols( locals ); }
    public Symbol _locals( SymbolDataStructure locals ) { return _symbols( locals ); }

    // get the symbol's locals
    private SymbolMap _symbols()
    {
        return new SymbolMap( getLocalSymbols() );
    }
    // set the symbols's locals
    private Symbol _symbols( SymbolDataStructure symbols )
    {
        // HACK: covers the case when the given symbols are null as well
        if( !( symbols instanceof SymbolMap ) ) symbols = new SymbolMap( symbols );
        
        setLocals( symbols );
        return this;
    }


    public boolean isConst()         { return _kind() == CONST;        }
    public boolean isVar()           { return _kind() == VAR;          }
    public boolean isStaticField()   { return _kind() == STATIC_FIELD; }
    public boolean isField()         { return _kind() == FIELD;        }
    public boolean isMethod()        { return _kind() == METHOD;       }
    public boolean isFunction()      { return _kind() == FUNCTION;     }
    public boolean isFormalParam()   { return _kind() == FORMAL_PARAM; }
    public boolean isActivParam()    { return _kind() == ACTIV_PARAM;  }
    public boolean isType()          { return _kind() == TYPE;         }
    public boolean isArrayElem()     { return _kind() == ARRAY_ELEM;   }
    public boolean isProgram()       { return _kind() == PROGRAM;      }

    public boolean isNoSym()         { return this == SymbolTable.noSym;  }
    public boolean isAnySym()        { return this == SymbolTable.anySym; }
    public boolean isDummySym()      { return _name().charAt( 0 ) == '@'; }
    public boolean isLvalue()        { return                     _kind() == VAR || _kind() == FIELD || _kind() == STATIC_FIELD || _kind() == ARRAY_ELEM || _kind() == FORMAL_PARAM; }
    public boolean isRvalue()        { return _kind() == CONST || _kind() == VAR || _kind() == FIELD || _kind() == STATIC_FIELD || _kind() == ARRAY_ELEM || _kind() == FORMAL_PARAM; }
    public boolean isGlobal()        { return _scopeLevel() != NO_VALUE && SymbolTable.isGlobalScope( _scopeLevel() ); }
    
    public boolean isThis()          { return _kind() == CONST    && "this"          .equals( _name() ); }
    public boolean ispVirtualTable() { return _kind() == FIELD    && "@pVirtualTable".equals( _name() ); }
    public boolean isMain()          { return _kind() == FUNCTION && "main"          .equals( _name() ); }


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

    public static boolean canOverride( Symbol methodA, Symbol methodB )
    {
        if( methodA == methodB ) return true;
        if( !methodA.isMethod() || !methodB.isMethod() ) return false;

        // check if the return type is equivalent or a subtype of the inherited return type
        SymbolType retA = methodA._type();
        SymbolType retB = methodB._type();
        if( !SymbolType.canOverride( retA, retB ) ) return false;

        if( methodA._paramCount() != methodB._paramCount() ) return false;
        
        Iterator<Symbol> iterA = methodA._params().iterator();
        Iterator<Symbol> iterB = methodB._params().iterator();
        boolean isSameSignature = true;
        while( iterA.hasNext() && iterB.hasNext() )
        {
            SymbolType typeA = iterA.next()._type();
            SymbolType typeB = iterB.next()._type();

            // check if the current formal parameter is equivalent to the inherited formal parameter at this position
            if( !SymbolType.isEquivalent( typeA, typeB ) )
            {
                isSameSignature = false;
                break;
            }
        }

        return isSameSignature;
    }

    @Override
    public Symbol clone()
    {
        return new Symbol( _kind(), _name(), _type(), _address(), _scopeLevel(), _memberIdx(), _locals().clone() );
    }

    public Symbol clone( String name )
    {
        return new Symbol( _kind(), name, _type(), _address(), _scopeLevel(), _memberIdx(), _locals().clone() );
    }

    public String toString( String prefix )
    {
        String result = "<symbol>";
        String typeName = _type().nameToString();

        switch( _kind() )
        {
            case CONST:                        { result = String.format( "%s=%-4d .....   CONST         %s %s\n",   prefix, _value(),                                 typeName, _name() ); break; }
            case VAR: if( isGlobal() )         { result = String.format( "%s&%-4d s%-4d   GLOBAL_VAR    %s %s\n",   prefix, _address(),                _scopeLevel(), typeName, _name() ); break; }
                      else                     { result = String.format( "%ss%-4d #%-4d   VAR           %s %s\n",   prefix,             _scopeLevel(), _memberIdx(),  typeName, _name() ); break; }
            case STATIC_FIELD:                 { result = String.format( "%s&%-4d #%-4d   STATIC_FIELD  %s %s\n",   prefix, _address(), _memberIdx(),                 typeName, _name() ); break; }
            case FIELD:                        { result = String.format( "%s&%-4d #%-4d   FIELD         %s %s\n",   prefix, _address(), _memberIdx(),                 typeName, _name() ); break; }
            case METHOD:                       { result = String.format( "%s&%-4d #%-4d   METHOD        %s %s\n",   prefix, _address(), _memberIdx(),                 typeName, _name() + localsToString( "" ) ); break; }
            case FUNCTION:                     { result = String.format( "%s&%-4d .....   FUNCTION      %s %s\n",   prefix, _address(),                               typeName, _name() + localsToString( "" ) ); break; }
            case FORMAL_PARAM:                 { result = String.format( "%s..... #%-4d   FORMAL_PARAM  %s %s\n",   prefix,             _paramIdx(),                  typeName, _name() ); break; }
            case ACTIV_PARAM:                  { result = String.format( "%s..... #%-4d   ACTIV_PARAM   %s %s\n",   prefix,             _paramIdx(),                  typeName, _name() ); break; }
            case TYPE: if( _type().isClass() ) { result = String.format( "%s&%-4d v%-4d   CLASS         %s %s\n%s", prefix, _address(), _virtualTableSize(),          typeName, _name(), _type().membersToString( prefix ) ); break; }
                       else                    { result = String.format( "%s..... .....   TYPE          %s %s\n%s", prefix,                                           typeName, _name(), _type().membersToString( prefix ) ); break; }
            case ARRAY_ELEM:                   { result = String.format( "%s..... .....   ARRAY_ELEM    %s %s\n",   prefix,                                           typeName, _name() ); break; }
            case PROGRAM:                      { result = String.format( "%s..... .....   PROGRAM       %s %s\n%s", prefix,                                           typeName, _name(), localsToString( prefix ) ); break; }
        }
        return result;
    }

    // print the symbol locals's names as a list
    String localsToString( String prefix )
    {
        switch( _kind() )
        {
            case METHOD:
            case FUNCTION:
            {
                List<Symbol> locals = _locals()._sorted();
                if( locals.isEmpty() ) return "()";

                StringBuilder builder = new StringBuilder( "( " );
                for( Symbol local : locals )
                {
                    builder.append( local._type().nameToString() )
                        .append( " " )
                        .append( local._name() )
                        .append( ", " );
                }
                if( locals.size() > 0 ) builder.replace( builder.length()-2, builder.length(), " )" );
                return builder.toString();
            }
            case PROGRAM:
            {
                SymbolMap members = _members();
                if( members.size() == 0 ) return "{}\n";
                
                String memberPrefix = prefix + "    ";
                StringBuilder builder = new StringBuilder( "{\n" );
                for( Symbol member : members )
                {
                    builder.append( member.toString( memberPrefix ) );
                }
                builder.append( "}\n" );
                return builder.toString();
            }
            default:
            {
                return "";
            }
        }
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
