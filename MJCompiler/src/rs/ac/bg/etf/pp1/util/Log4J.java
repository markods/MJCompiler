package rs.ac.bg.etf.pp1.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class Log4J
{
    private static boolean configured = false;
    private static File fLog = null;

    private static void configure()
    {
        if( configured ) return;
        
        URL urlConfigFile = Thread.currentThread().getContextClassLoader().getResource( "log4j.xml" );
        DOMConfigurator.configure( urlConfigFile );

        Logger root = Logger.getRootLogger();
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
             fLog    = new File( fnameLog );
        File fRenLog = new File( fnameRenLog );

        // five retries for renaming log file
        for( int i = 0; i < 5; i++ )
        {
            if( fLog.exists() && !fLog.renameTo( fRenLog ) && i == 5 )
            {
                System.err.println( "Could not open log file!" );
                break;
            }

            try
            {
                Thread.sleep( 50 );
            }
            catch( InterruptedException ex )
            {}
        }

        fileAppender.setFile( fLog.getAbsolutePath() );
        fileAppender.activateOptions();
        
        fTmpLog.delete();
        configured = true;
    }

    public static Log4J getLogger( Class clazz )
    {
        if( !configured ) configure();
        return new Log4J( clazz );
    }

    public static File getLogFile()
    {
        if( !configured ) configure();
        return fLog;
    }


    public static final int FATAL = 0;
    public static final int ERROR = 1;
    public static final int WARN = 2;
    public static final int INFO = 3;
    public static final int DEBUG = 4;


    @FunctionalInterface
    private interface ILogFuncA { void log( Object msg ); }
    @FunctionalInterface
    private interface ILogFuncB { void log( Object msg, Throwable t ); }
    
    private final Logger logger;
    private final ILogFuncA[] logFuncA;
    private final ILogFuncB[] logFuncB;


    private Log4J( Class clazz )
    {
        logger = Logger.getLogger( clazz );
        logFuncA = new ILogFuncA[] { logger::fatal, logger::error, logger::warn, logger::info, logger::debug };
        logFuncB = new ILogFuncB[] { logger::fatal, logger::error, logger::warn, logger::info, logger::debug };
    }


    public void fatal( String message ) { log( FATAL, message, null, false, false ); }
    public void error( String message ) { log( ERROR, message, null, false, false ); }
    public void warn ( String message ) { log( WARN,  message, null, false, false ); }
    public void info ( String message ) { log( INFO,  message, null, false, false ); }
    public void debug( String message ) { log( DEBUG, message, null, false, false ); }

    public void fatal( String message, Throwable throwable ) { log( FATAL, message, throwable, false, true ); }
    public void error( String message, Throwable throwable ) { log( ERROR, message, throwable, false, true ); }
    public void warn ( String message, Throwable throwable ) { log( WARN,  message, throwable, false, true ); }
    public void info ( String message, Throwable throwable ) { log( INFO,  message, throwable, false, true ); }
    public void debug( String message, Throwable throwable ) { log( DEBUG, message, throwable, false, true ); }

    public void log( int level, String message, boolean multiline ) { log( level, message, null, multiline, false ); }
    public void log( int level, String message, Throwable throwable, boolean multiline ) { log( level, message, throwable, multiline, true ); }


    private void log( int level, String message, Throwable throwable, boolean multiline, boolean hasThrowable )
    {
        ILogFuncA funcA = logFuncA[ level ];
        ILogFuncB funcB = logFuncB[ level ];

        if( !multiline )
        {
            if( !hasThrowable ) funcA.log( message );
            else                funcB.log( message, throwable );
            return;
        }
        
        String[] lines = message.split( "\\R", -1 );
        String lastLine = ( lines.length > 0 ) ? lines[ lines.length - 1 ] : null;

        for( int i = 0; i < lines.length - 1; i++ )
        {
            funcA.log( lines[ i ] );
        }

        if( !hasThrowable ) funcA.log( lastLine );
        else                funcB.log( lastLine, throwable );
    }

}
