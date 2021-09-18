package rs.ac.bg.etf.pp1.props;

public class CountProp
{
    private final int count_def;
    private int count;

    public CountProp(){ this( 0 ); }
    public CountProp( int count ){ this.count = this.count_def = count; }
    
    public int get() { return count; }
    public int get_inc() { return count++; }
    public int get_inc( int amount ) { int count = this.count; this.count += amount; return count; }
    public int set( int count ) { this.count = count; return this.count; }
    public int clear() { int count = this.count; this.count = count_def; return count; }
}
