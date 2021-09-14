package rs.ac.bg.etf.pp1.props;

public class BoolProp
{
    private boolean bool = false;
    
    public boolean get() { return bool; }
    public boolean set() { bool = true; return bool; }
    public boolean true_() { return bool; }
    public boolean false_() { return !bool; }
    public boolean reset() { bool = false; return bool; }
}
