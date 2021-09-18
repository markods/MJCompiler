package rs.ac.bg.etf.pp1.props;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import rs.ac.bg.etf.pp1.CodeGen;

public class JumpProp implements Iterable<JumpProp.JumpRecord>
{
    public static int NO_ADDRESS = CodeGen.NO_ADDRESS;

    private LinkedHashMap<String, JumpRecord> jumpMap = new LinkedHashMap<>();
    public static class JumpRecord
    {
        private final String pointName;
        private int pointAddress = NO_ADDRESS;
        private final ArrayList<Integer> addressesToFix = new ArrayList<>();

        private JumpRecord( String pointName )
        {
            if( pointName == null ) throw new IllegalArgumentException( "The point name cannot be null" );
            this.pointName = pointName;
        }


        public String _pointName() { return pointName; }

        public int _pointAddress() { return pointAddress; }
        public JumpRecord _pointAddress( int pointAddress )
        {
            if( this.pointAddress != NO_ADDRESS && this.pointAddress != pointAddress ) throw new IllegalArgumentException( "The point's address cannot be changed once set" );
            this.pointAddress = pointAddress;

            for( int addressToFix : addressesToFix ) CodeGen.fixJumpOffset( addressToFix, pointAddress );
            addressesToFix.clear();

            return this;
        }

        public JumpRecord _addAddressToFix( int addressToFix )
        {
            if( this.pointAddress != CodeGen.NO_ADDRESS ) CodeGen.fixJumpOffset( addressToFix, pointAddress );
            else                                          addressesToFix.add( addressToFix );
            
            return this;
        }
    }

    public boolean add( String pointName )
    {
        return null == jumpMap.putIfAbsent( pointName, new JumpRecord( pointName ) );
    }
    public JumpRecord get( String pointName )
    {
        return jumpMap.get( pointName );
    }
    public boolean contains( String pointName )
    {
        return null != jumpMap.get( pointName );
    }
    public boolean remove( String pointName )
    {
        return null != jumpMap.remove( pointName );
    }

    public int size() { return jumpMap.size(); }
    public void clear() { jumpMap.clear(); }

    @Override
    public Iterator<JumpRecord> iterator()
    {
        return jumpMap.values().iterator();
    }
}
