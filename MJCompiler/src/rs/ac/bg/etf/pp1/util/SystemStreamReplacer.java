package rs.ac.bg.etf.pp1.util;

import java.io.OutputStream;
import java.io.PrintStream;

public class SystemStreamReplacer implements AutoCloseable
{
    public static final int STDOUT = 0;
    public static final int STDERR = 1;

    private int streamId = STDOUT;
    private PrintStream stream_old = null;
    private PrintStream stream_new = null;

    
    // replace the system output with the given stream in the try-with-resources block
    public SystemStreamReplacer( int streamId, OutputStream stream )
    {
        this( streamId, stream, true );
    }

    // replace the system output with the given stream in the try-with-resources block
    public SystemStreamReplacer( int streamId, OutputStream stream, boolean autoflush )
    {
        this( streamId, new PrintStream( stream, autoflush ) );
    }

    // replace the system output with the given stream in the try-with-resources block
    public SystemStreamReplacer( int streamId, PrintStream stream )
    {
        this.streamId = streamId;
        this.stream_new = stream;

        switch( streamId )
        {
            case STDOUT: { stream_old = System.out; System.setOut( stream_new ); break; }
            case STDERR: { stream_old = System.err; System.setErr( stream_new ); break; }
            default:     { throw new IllegalArgumentException( "Invalid stream id given" ); }
        }
    }

    @Override
    public void close()
    {
        if( stream_old != null )
        {
            switch( streamId )
            {
                case STDOUT: { System.setOut( stream_old ); break; }
                case STDERR: { System.setErr( stream_old ); break; }
            }
        }
        if( stream_new != null )
        {
            stream_new.close();
        }
    }

}
