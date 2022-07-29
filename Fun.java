public class Fun
{
    public static void main(String[] args) {
        FunParser parser = new FunParser(new FunLexer(System.in));
        parser.parse().eval();
    }
}