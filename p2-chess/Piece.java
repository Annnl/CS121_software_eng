
import java.util.*;

abstract public class Piece {
    private static Map<Character, PieceFactory> pieceFactories = new HashMap<>();
    private Color color;
    public Piece(Color c) {
        color = c;
    }

    public static void registerPiece(PieceFactory pf) {
        pieceFactories.put(pf.symbol(), pf);
    }

    public static Piece createPiece(String name) {
        if (name == null || name.length() != 2){
            throw new IllegalArgumentException();
        }
        char color_char = name.charAt(0);
       // System.out.println(color_char);
        char kind_char = name.charAt(1);
        Color color;
        if (color_char == 'b'){
            color = Color.BLACK;
        }else if(color_char == 'w'){
            color = Color.WHITE;
        }else{
            throw new IllegalArgumentException();
        }
        PieceFactory factory = pieceFactories.get(kind_char);
        if (factory == null){
            throw new IllegalArgumentException();
        }
        return factory.create(color);
    }

    public Color color() {
	// You should write code here and just inherit it in
	// subclasses. For this to work, you should know
	// that subclasses can access superclass fields.
        return color;
    }

    abstract public String toString();

    abstract public List<String> moves(Board b, String loc);
}