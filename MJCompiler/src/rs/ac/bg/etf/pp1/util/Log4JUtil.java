package rs.ac.bg.etf.pp1.util;

import java.io.File;
import java.net.URL;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class Log4JUtil
{
    private Log4JUtil() {}

    public static void load()
    {
        DOMConfigurator.configure( findLoggerConfigFile() );
        prepareLogFile( Logger.getRootLogger() );
    }


    @FunctionalInterface
    public interface ILogFunction { void log( Object message ); }

    // logs a multiline message
    public static void logMultiline( ILogFunction logFunc, String message )
    {
        if( logFunc == null || message == null ) return;

        String[] lines = message.split( "\\R", -1 );
        for( String line: lines )
        {
            logFunc.log( line );
        }
    }



    private static URL findLoggerConfigFile()
    {
        return Thread.currentThread().getContextClassLoader().getResource( "log4j.xml" );
    }

    private static void prepareLogFile( Logger root )
    {
        Appender appender = root.getAppender( "LogFileAppender" );
        if( !( appender instanceof FileAppender ) )
        {
            System.err.println( "Log file's file appender missing!" );
            return;
        }
        
        FileAppender fileAppender = ( FileAppender )appender;
        
        String fnameTmpLog = fileAppender.getFile();
        String fnameLog = fnameTmpLog.substring( 0, fnameTmpLog.lastIndexOf( '.' ) ) + ".log";
        String fnameRenLog = String.format(
            "%s %s%s",
            fnameLog.substring( 0, fnameLog.lastIndexOf( '.' ) ),   // filename
            ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ofPattern( "yyyy-MM-dd HH.mm.ss" ) ),   // current time
            ".log"   // extension
        );

        File fTmpLog = new File( fnameTmpLog );
        File fLog = new File( fnameLog );
        File fRenLog = new File( fnameRenLog );

        if( fLog.exists() && !fLog.renameTo( fRenLog ) )
        {
            System.err.println( "Could not rename log file!" );
        }

        fileAppender.setFile( fLog.getAbsolutePath() );
        fileAppender.activateOptions();
        
        fTmpLog.delete();
    }

}
