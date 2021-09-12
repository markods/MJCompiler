package rs.ac.bg.etf.pp1.props;

public class RefProp<T>
{
    private T ref = null;

    public RefProp() {}
    public RefProp( T ref ) { this.ref = ref; }

    public T get() { return ref; }
    public T set( T ref ) { this.ref = ref; return this.ref; }
    public T reset() { T ref = this.ref; this.ref = null; return ref; }
}
