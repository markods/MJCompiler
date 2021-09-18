package rs.ac.bg.etf.pp1.props;

import java.util.TreeSet;

// used in parser specification (the parser generator doesn't support templates)
public class SwitchProp
{
    // TODO
    private TreeSet<Integer> map = new TreeSet<Integer>();

    public boolean contains( Integer elem ) { return map.contains( elem ); }
    public boolean add( Integer elem ) { return map.add( elem ); }
    public int size() { return map.size(); }
    public void clear() { map.clear(); }
}
