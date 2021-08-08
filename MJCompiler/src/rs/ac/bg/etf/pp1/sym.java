package rs.ac.bg.etf.pp1;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import java_cup.runtime.Symbol;

/** CUP generated class containing symbol constants. */
public class sym implements Isym
{
    private static ArrayList<String> symbolNameList = null;

    public static String getSymbolName( int symbol_id )
    {
        if( symbolNameList == null )
        {
            Field[] fieldList = sym.class.getFields();
            symbolNameList = new ArrayList<>( fieldList.length );

            sym symbolInstance = new sym();
            int wantedModifiers = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
            for( Field field : fieldList )
            {
                try
                {
                    int fieldModifiers = field.getModifiers();
                    if( field.getType() == int.class && (fieldModifiers & wantedModifiers) == wantedModifiers )
                    {
                        int fieldValue = field.getInt( symbolInstance );
                        String fieldName = field.getName();

                        while( fieldValue >= symbolNameList.size() )
                        {
                            symbolNameList.add( null );
                        }

                        symbolNameList.set( fieldValue, fieldName );
                    }
                }
                catch( IllegalAccessException ex )
                {
                }
            }
        }

        return symbolNameList.get( symbol_id );
    }

    public static String symbolToString( Symbol symbol )
    {
        String symbolName = ( symbol != null ) ? getSymbolName( symbol.sym ) : "<INVALID TOKEN>";
        String symbolValue = ( symbol != null && symbol.value != null ) ? "'" + symbol.value.toString() + "'" : "''";

        return String.format( "Ln #%-3d, Col #%-3d   %-15s %s", symbol.left, symbol.right, symbolName, symbolValue );
    }
}
