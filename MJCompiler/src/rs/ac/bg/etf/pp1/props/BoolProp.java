package rs.ac.bg.etf.pp1.props;

public class BoolProp
{
    private final boolean bool_def;
    private boolean bool;

    public BoolProp() { this( false ); }
    public BoolProp( boolean bool ) { this.bool = this.bool_def = bool; }
    
    public boolean get() { return bool; }
    public boolean set() { bool = true; return bool; }
    public boolean reset() { bool = false; return bool; }
    public boolean true_() { return bool; }
    public boolean false_() { return !bool; }
    public boolean clear() { boolean bool = this.bool; this.bool = bool_def; return bool; }
}
