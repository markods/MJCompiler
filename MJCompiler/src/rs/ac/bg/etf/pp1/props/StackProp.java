package rs.ac.bg.etf.pp1.props;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Predicate;

public class StackProp<T> implements Iterable<T>
{
    private LinkedList<T> stack = new LinkedList<>();

    public T add( T elem ) { stack.addFirst( elem ); return elem; }
    public T remove() { return ( !stack.isEmpty() ) ? stack.removeFirst() : null; }
    public T top() { return stack.peekFirst(); }
    public T find( Predicate<T> predicate )
    {
        for( T elem : stack )
        {
            if( predicate.test( elem ) )
            {
                return elem;
            }
        }
        return null;
    }
    public int size() { return stack.size(); }
    public void clear() { stack.clear(); }
    public Iterator<T> iterator() { return stack.iterator(); }
}
