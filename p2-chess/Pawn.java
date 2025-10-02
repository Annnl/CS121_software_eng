import java.util.*;

public class Pawn extends Piece {
    public Pawn(Color c) { 
        super(c);
    }
    // implement appropriate methods

    public String toString() {
	    if (color() == Color.BLACK){
            return "bp";
        }else{
            return "wp";
        }
    }

    public List<String> moves(Board b, String loc) {
        if (loc == null || loc.length() != 2){
            throw new IllegalArgumentException();
        }
        List<String> options = new LinkedList<>();
        char row = loc.charAt(1);
        char col = loc.charAt(0);
        b.convert_loc(loc);
	    if (color() == Color.BLACK){
            if (row == '1'){
                return options;
            }else{
                if (col != 'a' && b.getPiece(""+(char)(col - 1)+(char)(row - 1)) != null && (b.getPiece(""+(char)(col - 1)+(char)(row - 1)).color() == Color.WHITE)){
                    options.add(""+(char)(col - 1)+(char)(row - 1));
                }
                if (col != 'h' && b.getPiece(""+(char)(col + 1)+(char)(row - 1)) != null && (b.getPiece(""+(char)(col + 1)+(char)(row - 1)).color() == Color.WHITE)){
                    options.add(""+(char)(col + 1)+(char)(row - 1));
                }
                if (b.getPiece(""+col+(char)(row - 1))==null){
                    options.add(""+col+(char)(row - 1));
                }
                if (row == '7' && (b.getPiece(""+col+(char)(row - 2))==null) && (b.getPiece(""+col+(char)(row - 1))==null)){
                    options.add(""+col+(char)(row - 2));
                }
            }
        }else{
            if (row == '8'){
                return options;
            }else{
                if (col != 'a' && b.getPiece(""+(char)(col - 1)+(char)(row + 1)) != null&& (b.getPiece(""+(char)(col - 1)+(char)(row + 1)).color() == Color.BLACK)){
                    options.add(""+(char)(col - 1)+(char)(row + 1));
                }
                if (col != 'h' && b.getPiece(""+(char)(col + 1)+(char)(row + 1)) != null&& (b.getPiece(""+(char)(col + 1)+(char)(row + 1)).color() == Color.BLACK)){
                    options.add(""+(char)(col + 1)+(char)(row + 1));
                }
                if (b.getPiece(""+col+(char)(row + 1))==null){
                    options.add(""+col+(char)(row + 1));
                }
                if (row == '2' && (b.getPiece(""+col+(char)(row + 2))==null) && (b.getPiece(""+col+(char)(row + 1))==null)){
                    options.add(""+col+(char)(row + 2));
                }
            }
        }
        return options;
    }

}