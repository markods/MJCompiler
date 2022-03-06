package rs.ac.bg.etf.pp1.props;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import rs.ac.bg.etf.pp1.CodeGen;

public class JumpProp implements Iterable<JumpProp.JumpRecord>
{
    public static int NO_ADDRESS = CodeGen.NO_ADDRESS;

    private final LinkedHashMap<String, JumpRecord> jumpMap = new LinkedHashMap<>();
    private final CodeGen codeGen;

    public static class JumpRecord
    {
        private final CodeGen codeGen;
        private final String pointName;
        private int pointAddress = NO_ADDRESS;
        private final ArrayList<Integer> addressesToFix = new ArrayList<>();

        private JumpRecord( CodeGen codeGen, String pointName )
        {
            this.codeGen = codeGen;
            this.pointName = pointName;
        }


        public String _pointName() { return pointName; }

        public int _pointAddress() { return pointAddress; }
        public JumpRecord _pointAddress( int pointAddress )
        {
            if( this.pointAddress != NO_ADDRESS ) throw new IllegalArgumentException( "The point's address cannot be set more than once" );
            this.pointAddress = pointAddress;

            for( int addressToFix : addressesToFix ) codeGen.fixJumpOffset( addressToFix, pointAddress );
            addressesToFix.clear();

            return this;
        }

        public JumpRecord _addAddressToFix( int addressToFix )
        {
            if( pointAddress != CodeGen.NO_ADDRESS ) codeGen.fixJumpOffset( addressToFix, pointAddress );
            else                                     addressesToFix.add( addressToFix );
            
            return this;
        }
    }

    // constructor
    public JumpProp( CodeGen codeGen )
    {
        this.codeGen = codeGen;
    }


    public boolean add( String pointName )
    {
        return null == jumpMap.putIfAbsent( pointName, new JumpRecord( codeGen, pointName ) );
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
