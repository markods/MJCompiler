package rs.ac.bg.etf.pp1;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;


public class TokenCode implements ITokenCode
{
    private static ArrayList<String> tokenNameList = null;

    private static void initTokenNameList()
    {
        Field[] fieldList = TokenCode.class.getFields();
        tokenNameList = new ArrayList<>( fieldList.length );

        TokenCode tokenInstance = new TokenCode();
        int wantedModifiers = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;

        for( Field field : fieldList )
        {
            try
            {
                int fieldModifiers = field.getModifiers();
                if( field.getType() == int.class && ( fieldModifiers & wantedModifiers ) == wantedModifiers )
                {
                    int fieldValue = field.getInt( tokenInstance );
                    String fieldName = field.getName();

                    while( fieldValue >= tokenNameList.size() )
                    {
                        tokenNameList.add( null );
                    }

                    tokenNameList.set( fieldValue, fieldName );
                }
            }
            catch( IllegalAccessException ex )
            {
            }
        }
    }


    public static String getTokenName( int tokenCode )
    {
        if( tokenNameList == null )
        {
            initTokenNameList();
        }
        
        return tokenNameList.get( tokenCode );
    }

    public static boolean isIgnored( int tokenCode )
    {
        return tokenCode == TokenCode.newline
            || tokenCode == TokenCode.whitespace
            || tokenCode == TokenCode.line_comment
            || tokenCode == TokenCode.inline_comment;
    }

    public static boolean isInvalid( int tokenCode )
    {
        return tokenCode == TokenCode.error
            || tokenCode == TokenCode.invalid;
    }

    public static boolean isWhitespace( int tokenCode )
    {
        return tokenCode == TokenCode.whitespace;
    }

    public static boolean isNewline( int tokenCode )
    {
        return tokenCode == TokenCode.newline;
    }

    public static boolean isEqualityComparison( int tokenCode )
    {
        return tokenCode == TokenCode.eq
            || tokenCode == TokenCode.ne;
    }

    public static boolean isEOF( int tokenCode )
    {
        return tokenCode == TokenCode.EOF;
    }

}
