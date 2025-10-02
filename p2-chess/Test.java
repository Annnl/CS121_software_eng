public class Test {

    // Run "java -ea Test" to run with assertions enabled (If you run
    // with assertions disabled, the default, then assert statements
    // will not execute!)

    public static void test1() {
        String[] args = {"layout2", "moves2"};
        Chess.main(args);
    }
    public static void test2() {
        // Simple tests to get started
        Board b = Board.theBoard();
        Piece.registerPiece(new BishopFactory());
        Piece p = Piece.createPiece("bb");
        b.addPiece(p, "d5");
        assert b.getPiece("d5") == p;
    }
    
    public static void main(String[] args) {
        test1();
        //test2();
    }

}