package rs.ac.bg.etf.pp1.props;

import java.util.TreeSet;

public class SetProp<T>
{
    private TreeSet<T> set = new TreeSet<T>();

    public boolean contains( T elem ) { return set.contains( elem ); }
    public boolean add( T elem ) { return set.add( elem ); }
    public int size() { return set.size(); }
    public void clear() { set.clear(); }
}
