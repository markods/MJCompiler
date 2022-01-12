package rs.ac.bg.etf.pp1;

import java.util.Collection;

import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;
import rs.etf.pp1.symboltable.structure.SymbolDataStructure;


public class SymbolType extends Struct
{
    public static final int VOID_TYPE = -1;
    public static final int ANY_TYPE  = Struct.None;
    public static final int INT       = Struct.Int;    // redefinable
    public static final int CHAR      = Struct.Char;   // redefinable
    public static final int BOOL      = Struct.Bool;   // redefinable

    public static final int ARRAY     = Struct.Array;
    public static final int ENUM      = Struct.Enum;
    public static final int RECORD    = 10;
    public static final int CLASS     = Struct.Class;
    public static final int INTERFACE = Struct.Interface;

    ////// None, Int, Char, Array, Class, Record, Bool, Enum, Interface
    // private int kind;

    private String name;

    ////// niz: tip elementa niza
    ////// klasa: tip roditeljske klase
    // private Struct elemType; 
    
    ////// klasa: lista implementiranih interfejsa
    // private Collection<Struct> implementedInterfaceList;

    ////// klasa: broj bolja klase
    // private int numOfFields;

    ////// klasa i interfejs: referenca na hes tabelu u kojoj se nalaze polja klase
    ////// enum: referenca na hes tabelu u kojoj se nalaze konstante nabrajanja
    // private SymbolDataStructure members;

    // inheritance level, zero for primitive types
    private int level = 0;

    private SymbolType( int kind, String name, SymbolType base, SymbolMap members )
    {
        super( kind );
        _name( name );
        _base( base );
        _members( members );
    }

    // NONE, INT, CHAR, BOOL, ARRAY, ENUM, CLASS, INTERFACE, RECORD
    public static SymbolType newPrimitive( String name, int kind )                           { return new SymbolType( kind,                 name, null, null    ); }
    public static SymbolType newArray    ( String name, SymbolType type )                    { return new SymbolType( SymbolType.ARRAY,     name, type, null    )._members(  new SymbolMap(){{ addSymbol( Symbol.newArrayElem( "@elem", type ) ); }}  ); }
    public static SymbolType newEnum     ( String name, SymbolMap members )                  { return new SymbolType( SymbolType.ENUM,      name, null, members ); }
    public static SymbolType newRecord   ( String name, SymbolMap members )                  { return new SymbolType( SymbolType.RECORD,    name, null, members ); }
    public static SymbolType newClass    ( String name, SymbolType base, SymbolMap members ) { return new SymbolType( SymbolType.CLASS,     name, base, members ); }
    public static SymbolType newInterface( String name, SymbolType base, SymbolMap members ) { return new SymbolType( SymbolType.INTERFACE, name, base, members ); }


    // <symbol type>'s kind
    public int _kind() { return getKind(); }

    public String _name() { return name; }
    protected SymbolType _name( String name ) { this.name = name; return this; }

    // CLASS: the class's level in the inheritance tree
    public int _level() { return level; }
    protected SymbolType _level( int level ) { this.level = level; return this; }

    // ARRAY: array element type
    // CLASS: supertype (base type)
    public SymbolType _base() { return ( SymbolType )getElemType(); }
    public SymbolType _base( SymbolType type )
    {
        if( type == null )
        {
            type = SymbolTable.anyType;
            // type can still be null if the SymbolTable's anyType has not been initialized
            if( type == null ) type = this;
        }
        
        setElementType( type );
        _level( ( type != this ) ? type.level + 1 : 0 );
        return this;
    }

    // CLASS: the list of implemented interfaces
    public Collection<Struct> _interfaceList() { return getImplementedInterfaces(); }
    public SymbolType _addInterface( SymbolType type ) { if( type != null ) addImplementedInterface( type ); return this; }

    // CLASS, RECORD: number of non-static fields
    public int _fieldCount() { return getNumberOfFields(); }

    // CLASS, INTERFACE: fields and methods
    // ENUM: constants
    // RECORD: fields
    public SymbolMap _members() { return ( SymbolMap )getMembersTable(); }
    public SymbolType _members( SymbolMap symbols ) { setMembers( ( symbols != null ) ? symbols : new SymbolMap() ); return this; }
    public SymbolType _members( SymbolDataStructure symbols ) { setMembers( new SymbolMap( symbols ) ); return this; }
    public SymbolType _members( Collection<Obj> symbols ) { setMembers( new SymbolMap( symbols ) ); return this; }
    

    // FIX: add isAnyType checks whenever these functions are used
    public boolean isAnyType()   { return _kind() == ANY_TYPE;  }
    public boolean isVoidType()  { return _kind() == VOID_TYPE; }
    public boolean isInt()       { return _kind() == INT;       }
    public boolean isChar()      { return _kind() == CHAR;      }
    public boolean isBool()      { return _kind() == BOOL;      }

    public boolean isArray()     { return _kind() == ARRAY;     }
    public boolean isEnum()      { return _kind() == ENUM;      }
    public boolean isRecord()    { return _kind() == RECORD;    }
    public boolean isClass()     { return _kind() == CLASS;     }
    public boolean isInterface() { return _kind() == INTERFACE; }


    public boolean isNullType()      { return this == SymbolTable.nullType; }
    public boolean isPrimitiveType() { return _kind() == INT || _kind() == CHAR || _kind() == BOOL; }
    public boolean isReferenceType() { return _kind() == CLASS || _kind() == RECORD || _kind() == ARRAY; }
    public boolean hasMembers()      { return _kind() == CLASS || _kind() == RECORD; }

    public boolean isEqualTo       ( SymbolType type ) { return isEqual( this, type ); }
    public boolean isEquivalentTo  ( SymbolType type ) { return isEquivalent( this, type ); }
    public boolean isCompatibleWith( SymbolType type ) { return isCompatibleWith( this, type ); }
    public boolean isAssignableFrom( SymbolType type ) { return isAssignableFrom( this, type ); }


    public static boolean isEqual( SymbolType typeA, SymbolType typeB )
    {
        if( typeA == typeB ) return true;
        if( typeA == null || typeB == null ) return false;

        return typeA._kind() == typeB._kind()
            && isEqual( typeA._base(), typeB._base() )
            && typeA._interfaceList().equals( typeB._interfaceList() )   // FIX: check if this is an element-by-element comparison
            && SymbolMap.isEqual( typeA._members(), typeB._members() );
    }

    public static boolean isInstanceOf( SymbolType type, SymbolType supertype )
    {
        if( type == supertype ) return true;
        if( supertype.isAnyType() ) return true;
        if( !type.isClass() || !supertype.isClass() ) return false;

        // if the type is higher in the inheritance tree than it supertype, it cannot be its instance, return
        // +   higher in the inheritance tree <=> lower level
        if( type.level < supertype.level ) return false;
        
        boolean isSubclass = false;
        for( SymbolType curr = type; curr.level >= supertype.level;   )
        {
            // if a match has been found, save that it has been found and break
            if( curr.name.equals( supertype.name ) )
            {
                isSubclass = true;
                break;
            }

            // if we are at the root of the tree and a match hasn't been found, break
            if( curr.isAnyType() ) break;
            
            curr = curr._base();
        }

        return isSubclass;
    }

    public static boolean isEquivalent( SymbolType left, SymbolType right )
    {
        if( left == right ) return true;
        // special case for anyType which is the base type for everything (including itself), and also equivalent to any type
        if( left.isAnyType() || right.isAnyType() ) return true;

        return left.name == right.name
            || ( left.isArray() && right.isArray() && isEquivalent( left._base(), right._base() ) );
        
    }

    public static boolean canOverride( SymbolType type, SymbolType inherited )
    {
        return isEquivalent( type, inherited )
            || isInstanceOf( type, inherited );
}

    public static boolean isCompatibleWith( SymbolType left, SymbolType right )
    {
        return isEquivalent( left, right )
            || ( left.isNullType() && right.isReferenceType()  )
            || ( left.isReferenceType()  && right.isNullType() );
    }

    public static boolean isAssignableFrom( SymbolType dst, SymbolType src )
    {
        return isEquivalent( dst, src )
            || ( dst.isReferenceType() && src.isNullType() )
            || ( dst.isArray() && dst._base().isAnyType() && src.isArray() )   // assignment of array to formal parameter of type <any array>
            || isInstanceOf( src, dst );
    }

    public String toString( String prefix )
    {
        String result = "<type>";
        switch( _kind() )
        {
            case VOID_TYPE: result = String.format( "%sNO_TYPE       %s %s\n",     prefix, _base(), _name() ); break;
            case ANY_TYPE:  result = String.format( "%sANY_TYPE      %s %s\n",     prefix, _base(), _name() ); break;
            case INT:       result = String.format( "%sINT           %s %s\n",     prefix, _base(), _name() ); break;
            case CHAR:      result = String.format( "%sCHAR          %s %s\n",     prefix, _base(), _name() ); break;
            case BOOL:      result = String.format( "%sBOOL          %s %s\n",     prefix, _base(), _name() ); break;
            case ARRAY:     result = String.format( "%sARRAY         %s %s\n",     prefix, _base(), _name() ); break;
            case ENUM:      result = String.format( "%sENUM          %s %s\n%s\n", prefix, _base(), _name(), membersToString( prefix ) ); break;
            case RECORD:    result = String.format( "%sRECORD        %s %s\n%s\n", prefix, _base(), _name(), membersToString( prefix ) ); break;
            case CLASS:     result = String.format( "%sCLASS         %s %s\n%s\n", prefix, _base(), _name(), membersToString( prefix ) ); break;
            case INTERFACE: result = String.format( "%sINTERFACE     %s %s\n%s\n", prefix, _base(), _name(), membersToString( prefix ) ); break;
        }
        return result;
    }

    String nameToString()
    {
        switch( _kind() )
        {
            case VOID_TYPE: return _name();
            case ANY_TYPE:  return _name();
            case INT:       return _name();
            case CHAR:      return _name();
            case BOOL:      return _name();
            case ARRAY:     return _base()._name() + "[]";
            case ENUM:      return _name();
            case RECORD:    return _name();
            case CLASS:     return _name();
            case INTERFACE: return _name();
            default: return "<typename>";
        }
    }

    String membersToString( String prefix )
    {
        switch( _kind() )
        {
            case ENUM:      break;
            case RECORD:    break;
            case CLASS:     break;
            case INTERFACE: break;
            default:        return "";
        }
        
        SymbolMap members = _members();
        if( members.size() == 0 ) return prefix + "{}\n";

        String memberPrefix = prefix + "    ";
        StringBuilder builder = new StringBuilder( prefix ).append( "{\n" );
        for( Symbol member : members )
        {
            builder.append( member.toString( memberPrefix ) );
        }
        builder.append( prefix ).append( "}\n" );
        return builder.toString();
    }





    //___________________________________________________________________________________
    // DEPRECATED METHODS

    @Deprecated
    @Override
    public boolean equals( Object obj )
    {
        if( !( obj instanceof SymbolType ) ) return false;
        return isEqual( this, ( SymbolType )obj );
    }

    @Deprecated
    @Override
    public boolean equals( Struct other )
    {
        return isEqual( this, ( SymbolType )other );
    }

    @Deprecated
    @Override
    public boolean isRefType()
    {
        return isReferenceType();
    }

    @Deprecated
    @Override
    public boolean compatibleWith( Struct other )
    {
        return isCompatibleWith( this, ( SymbolType )other );
    }

    @Deprecated
    @Override
    public boolean assignableTo( Struct destination )
    {
        return isAssignableFrom( ( SymbolType )destination, this );
    }
    
}
