public interface FunTokens
{
    public static final int EOF=0;          // EOF in stream
    public static final int INVALID=1;      // Varies
    public static final int ASSIGN=2;       // :=
    public static final int SWAP=4;         // :=:
    public static final int PLUS=4;         // +
    public static final int MINUS=5;        // -
    public static final int TIMES=6;        // *
    public static final int DIVIDE=7;       // /
    public static final int EXPONENT=8;     // **
    public static final int LPAREN=9;       // (
    public static final int RPAREN=10;      // )
    public static final int INTEGER=11;     //
    public static final int FLOAT=12;       // digits.digits
    public static final int ID=13;          // letter{_|letter|digit}*
    public static final int PRINT=14;       // PRINT
    public static final int READ=15;        // READ
    public static final int STRING=16;      // " letters "
    public static final int CHARLIT=17;     // 'letter'
    public static final int BOOLEAN=18;		// TRUE or False
    public static final int CLASS=19;
    public static final int OVERRIDE=20;
    public static final int PRIVATE=21;
    public static final int COMMA=22;       // ,
    public static final int PROC=23;        // PROC
    public static final int BEGIN=24;       // BEGIN
    public static final int LBRACKET=25;    // [
    public static final int RBRACKET=26;    // ]
    public static final int NUMBER=27;      // NUMBER
    public static final int CHARACTER=28;   // CHARACTER
    public static final int IF=29;          // IF
    public static final int ELSE=30;        // ELSE
    public static final int WHILE=31;       // WHILE
    public static final int EQUAL=32;       // =
    public static final int NOTEQUAL=33;      // ~=
    public static final int LT=34;          // <
    public static final int LTE=35;         // <=
    public static final int GT=36;          // >
    public static final int GTE=37;         // >=
    public static final int END=38;         // END
	public static final int REF =39;
	public static final int NEW=40;
    public static final int INHERITS=41;
    public static final int TRUE=42;
    public static final int FALSE=43;
}
