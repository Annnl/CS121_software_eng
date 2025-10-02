import java.util.*;

public class Knight extends Piece {
    public Knight(Color c) { super(c); }
    // implement appropriate methods

    public String toString() {
	    if (color() == Color.BLACK){
            return "bn";
        }else{
            return "wn";
        }
    }

    public List<String> moves(Board b, String loc) {
	    if (loc == null || loc.length() != 2){
            throw new IllegalArgumentException();
        }
        int[] result = b.convert_loc(loc);
        int num_col = result[1];
        int num_row = result[0];

        List<String> options = new LinkedList<>();
        char row = loc.charAt(1);
        char col = loc.charAt(0);
        List<String> dest = new LinkedList<>();
        if (num_col >= 2 && num_row <= 6){
            dest.add(""+(char)(col - 2)+(char)(row + 1));
        }
        if (num_col >= 2 && num_row >= 1){
            dest.add(""+(char)(col - 2)+(char)(row - 1));
        }
        if (num_col <= 5 && num_row >= 1){
            dest.add(""+(char)(col + 2)+(char)(row - 1));
        }
        if (num_col <= 5 && num_row <= 6){
            dest.add(""+(char)(col + 2)+(char)(row + 1));
        }
        if (num_col <= 6 && num_row >= 2){
            dest.add(""+(char)(col + 1)+(char)(row - 2));
        }
        if (num_col <= 6 && num_row <= 5){
            dest.add(""+(char)(col + 1)+(char)(row + 2));
        }
        if (num_col >= 1 && num_row >= 2){
            dest.add(""+(char)(col - 1)+(char)(row - 2));
        }
        if (num_col >= 1 && num_row <= 5){
            dest.add(""+(char)(col - 1)+(char)(row + 2));
        }

        for (String s : dest){
            if (b.getPiece(s)==null || (b.getPiece(s).color() != super.color())){
                options.add(s);
            }
        }
        return options;
    }

}