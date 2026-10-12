package io.github.ahadporkar.ordertocash.governance;
/** Stable public errors; internal exception messages are never sent to clients. */
public final class Problem extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public final int status;
    public final String code;
    public Problem(int status, String code) { super(code); this.status=status; this.code=code; }
    public static Problem invalid() { return new Problem(400,"INVALID_INPUT"); }
    public static void require(boolean ok,int status,String code) { if(!ok) throw new Problem(status,code); }
}
