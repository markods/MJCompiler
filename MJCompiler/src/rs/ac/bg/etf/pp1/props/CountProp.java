package rs.ac.bg.etf.pp1.props;

public class CountProp
{
    private int count = 0;
    
    public int get() { return count; }
    public int get_inc() { return count++; }
    public int set( int count ) { this.count = count; return this.count; }
    public int reset() { int count = this.count; this.count = 0; return count; }
}
