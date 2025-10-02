import java.util.LinkedList;
import java.util.List;

public class Board {
    private static Board b;
    private Piece[][] pieces = new Piece[8][8];
    private List<BoardListener> listener = new LinkedList<>();
    private Logger log;
    private Board(){};

    public static Board theBoard() {
        if (b == null){
            b = new Board();
        }
	    return b; // implement this
    }

    public int[] convert_loc(String loc){
        if (loc == null || loc.length() != 2){
            throw new IllegalArgumentException();
        }
        char col = loc.charAt(0);
        char row = loc.charAt(1);
        int num_col = col - 'a';
        int num_row = row - '1';
        // System.out.println(loc);
        // System.out.println(num_col);
        // System.out.println(num_row);
        if (num_col < 0 || num_col > 7 || num_row > 7 || num_row < 0){
            throw new IllegalArgumentException();
        }
        int[] result = new int[2];
        result[0] = num_row;
        result[1] = num_col;
        return result;
    }
    // Returns piece at given loc or null if no such piece
    // exists
    public Piece getPiece(String loc) {
        int[] result = convert_loc(loc);
	    return pieces[result[0]][result[1]];
    }

    public void addPiece(Piece p, String loc) {
        int[] result = convert_loc(loc);
        if (getPiece(loc)!=null){
            //System.out.println(loc);
            throw new IllegalArgumentException();
        }
	    pieces[result[0]][result[1]] = p;
    }

    public void movePiece(String from, String to) {
        int[] result_from = convert_loc(from);
        int[] result_to = convert_loc(to);
        // System.out.println(getPiece(from).toString());
        // System.out.println(from);
        // System.out.println(getPiece(from).moves(b, from));
        if (getPiece(from)==null || !getPiece(from).moves(this, from).contains(to)){
            throw new IllegalArgumentException();
        } else {
            for (BoardListener bl : listener){
                bl.onMove(from, to, getPiece(from));
            }
            if (getPiece(to) != null){
                for (BoardListener bl : listener){
                    bl.onCapture(getPiece(from), getPiece(to));
                }
            }
            pieces[result_to[0]][result_to[1]] = getPiece(from);
            pieces[result_from[0]][result_from[1]] = null;
        }

    }

    public void clear() {
        pieces = new Piece[8][8];
    }

    public void registerListener(BoardListener bl) {
	    listener.add(bl);
    }

    public void removeListener(BoardListener bl) {
	    listener.remove(bl);
    }

    public void removeAllListeners() {
	    listener.clear();
    }

    public void iterate(BoardInternalIterator bi) {
        for (int i=0 ; i<8; i++){
            for (int j=0 ; j<8; j++){
                bi.visit(""+(char)('a'+i) + (char)('1'+j), getPiece(""+(char)('a'+i) + (char)('1'+j)));
            }
        }
    }
}