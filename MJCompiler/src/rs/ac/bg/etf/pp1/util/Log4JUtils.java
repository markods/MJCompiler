package rs.ac.bg.etf.pp1.util;

import java.io.File;
import java.net.URL;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

public class Log4JUtils
{
    private static Log4JUtils logs = new Log4JUtils();

    public static Log4JUtils instance()
    {
        return logs;
    }

    public URL findLoggerConfigFile()
    {
        return Thread.currentThread().getContextClassLoader().getResource( "log4j.xml" );
    }

    public void prepareLogFile( Logger root )
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
