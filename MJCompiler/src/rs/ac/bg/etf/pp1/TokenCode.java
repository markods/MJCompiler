package rs.ac.bg.etf.pp1;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.regex.Pattern;


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
            || tokenCode == TokenCode.multi_comment;
    }

    public static boolean isInvalid( int tokenCode )
    {
        return tokenCode == TokenCode.error
            || tokenCode == TokenCode.invalid;
    }

    public static boolean containsNewline( Token token )
    {
        if( token == null ) return false;
        int tokenCode = token.getCode();
        
        if( tokenCode == TokenCode.newline ) return true;

        if( tokenCode == TokenCode.line_comment    // possibly at the end of file, so not containing newline
         || tokenCode == TokenCode.multi_comment   // possibly inline
        )
        {
            String value = ( String )token.getValue();
            // find if there is at least one newline in the token
            boolean newlineFound = Pattern.compile( "\r|\n|\r\n" ).matcher( value ).find();
            return newlineFound;
        }

        return false;
    }

    public static boolean isEOF( int tokenCode )
    {
        return tokenCode == TokenCode.EOF;
    }

}
