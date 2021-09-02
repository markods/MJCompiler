package rs.ac.bg.etf.pp1;

import java.util.Collection;

import rs.etf.pp1.symboltable.concepts.Struct;
import rs.etf.pp1.symboltable.structure.SymbolDataStructure;


// IMPORTANT: don't add any new fields to this class, otherwise there will be problems with the Tab class
public class SymbolType extends Struct
{
    public static final int NO_TYPE = Struct.None;
    public static final int INT     = Struct.Int;
    public static final int CHAR    = Struct.Char;
    public static final int BOOL    = Struct.Bool;

    public static final int ARRAY   = Struct.Array;
    public static final int ENUM    = Struct.Enum;
    public static final int CLASS   = Struct.Class;
    public static final int INTERFACE = Struct.Interface;

    // // None, Int, Char, Array, Class, Bool, Enum, Interface
    // private int kind;

    // // niz: tip elementa niza
    // // klasa: tip roditeljske klase
    // private Struct elemType; 
    
    // // klasa: lista implementiranih interfejsa
    // private Collection<Struct> implementedInterfaceList;

    // // klasa: broj bolja klase
    // private int numOfFields;

    // // klasa i interfejs: referenca na hes tabelu u kojoj se nalaze polja klase
    // // enum: referenca na hes tabelu u kojoj se nalaze konstante nabrajanja
    // private SymbolDataStructure members;

    public SymbolType( int kind )
    {
        this( kind, null, null );
    }

    private SymbolType( int kind, SymbolType type, SymbolMap members )
    {
        super( kind );
        _type( type );
        _members( members );
    }

    // NONE, INT, CHAR, BOOL, ARRAY, ENUM, CLASS, INTERFACE
    public static SymbolType newPrimitive( int kind )                           { return new SymbolType( kind,                 null, null ); }
    public static SymbolType newArray    ( SymbolType type )                    { return new SymbolType( SymbolType.ARRAY,     type, null ); }
    public static SymbolType newEnum     ( SymbolMap members )                  { return new SymbolType( SymbolType.ENUM,      null, members ); }
    public static SymbolType newClass    ( SymbolType type, SymbolMap members ) { return new SymbolType( SymbolType.CLASS,     type, members ); }
    public static SymbolType newInterface( SymbolType type, SymbolMap members ) { return new SymbolType( SymbolType.INTERFACE, type, members ); }


    // <symbol type>'s kind
    public int _kind() { return getKind(); }

    // ARRAY: array element type
    // CLASS: supertype
    public SymbolType _type() { return ( SymbolType )getElemType(); }
    public SymbolType _type( SymbolType type ) { setElementType( type ); return this; }

    // CLASS: the list of implemented interfaces
    public Collection<Struct> _interfaceList() { return getImplementedInterfaces(); }
    public SymbolType _addInterface( SymbolType type ) { addImplementedInterface( type ); return this; }

    // CLASS: number of fields
    public int _fieldCount() { return getNumberOfFields(); }

    // CLASS, INTERFACE: fields and methods
    // ENUM: constants
    public SymbolMap _members() { return ( SymbolMap )getMembersTable(); }
    public SymbolType _members( SymbolMap symbols ) { setMembers( symbols ); return this; }
    public SymbolType _members( SymbolDataStructure symbols ) { setMembers( new SymbolMap( symbols ) ); return this; }
    

    public boolean isEqualTo( SymbolType type ) { return equals( type ); }
    public boolean isReferenceType() { return isRefType(); }
    public boolean isCompatibleWith( SymbolType type ) { return compatibleWith( type ); }
    public boolean isAssignableTo( SymbolType type ) { return assignableTo( type ); }





    //___________________________________________________________________________________
    // DEPRECATED METHODS

    @Deprecated
    @Override
    public boolean equals( Object obj )
    {
        // if the references are equal, return true
        if( super.equals( obj ) ) return true;
        if( !( obj instanceof SymbolType ) ) return false;

        return equals( ( SymbolType )obj );
    }

    @Deprecated
    @Override
    public boolean equals( Struct other )
    {
        SymbolType otherType = ( SymbolType )other;
        if( _kind() != otherType._kind() ) return false;

        switch( _kind() )
        {
            case ARRAY:
            {
                // the arrays must have the same element type
                return _type().equals( otherType._type() );
            }
            case CLASS:
            {
                // the classes must have the same members in the same order
                return SymbolMap.isEqual( _members(), otherType._members() );
            }
            default:
            {
                // the <other type> must be the same object reference
                return this == otherType;
            }
        }
    }

    @Deprecated
    @Override
    public boolean isRefType()
    {
        return _kind() == CLASS || _kind() == ARRAY;
    }

    @Deprecated
    @Override
    public boolean compatibleWith( Struct other )
    {
        SymbolType otherType = ( SymbolType )other;
        return ( this == SymbolTable.nullType && otherType.isRefType() )
            || ( otherType == SymbolTable.nullType && this.isRefType() )
            || this.equals( otherType );
    }

    @Deprecated
    @Override
    public boolean assignableTo( Struct destination )
    {
        SymbolType destType = ( SymbolType )destination;
        return ( this == SymbolTable.nullType && destType.isRefType() )
            || ( this._kind() == ARRAY && destType._kind() == ARRAY && destType._type() == SymbolTable.noType )
            || this.equals( destType );
    }
    
}
