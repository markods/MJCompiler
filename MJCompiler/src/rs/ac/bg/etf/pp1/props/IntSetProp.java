package rs.ac.bg.etf.pp1.props;

import java.util.TreeSet;

// used in parser specification (the parser generator doesn't support templates)
public class IntSetProp
{
    private TreeSet<Integer> set = new TreeSet<Integer>();

    public boolean contains( Integer elem ) { return set.contains( elem ); }
    public boolean add( Integer elem ) { return set.add( elem ); }
    public int size() { return set.size(); }
    public void reset() { set.clear(); }
}
