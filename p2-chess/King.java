import java.util.*;

public class King extends Piece {
    public King(Color c) { super(c); }
    // implement appropriate methods

    public String toString() {
	    if (color() == Color.BLACK){
            return "bk";
        }else{
            return "wk";
        }
    }

    public List<String> moves(Board b, String loc) {
	    if (loc == null || loc.length() != 2){
            throw new IllegalArgumentException();
        }
        b.convert_loc(loc);
        List<String> options = new LinkedList<>();
        char row = loc.charAt(1);
        char col = loc.charAt(0);
        List<String> dest = new LinkedList<>();
        if (row == '8' && col == 'a'){
            dest.add(""+(char)(col + 1)+(char)(row - 1));
            dest.add(""+(char)(col + 1)+(char)(row));
            dest.add(""+(char)(col)+(char)(row - 1));
        }else if (row == '8' && col == 'h'){
            dest.add(""+(char)(col - 1)+(char)(row - 1));
            dest.add(""+(char)(col - 1)+(char)(row));
            dest.add(""+(char)(col)+(char)(row - 1));
        }else if (row == '1' && col == 'h'){
            dest.add(""+(char)(col - 1)+(char)(row + 1));
            dest.add(""+(char)(col - 1)+(char)(row));
            dest.add(""+(char)(col)+(char)(row + 1));
        }else if (row == '1' && col == 'a'){
            dest.add(""+(char)(col + 1)+(char)(row + 1));
            dest.add(""+(char)(col + 1)+(char)(row));
            dest.add(""+(char)(col)+(char)(row + 1));
        }else if (row == '1'){
            dest.add(""+(char)(col + 1)+(char)(row));
            dest.add(""+(char)(col - 1)+(char)(row));
            dest.add(""+(char)(col)+(char)(row + 1));
            dest.add(""+(char)(col + 1)+(char)(row + 1));
            dest.add(""+(char)(col - 1)+(char)(row + 1));
        }else if (row == '8'){
            dest.add(""+(char)(col + 1)+(char)(row));
            dest.add(""+(char)(col - 1)+(char)(row));
            dest.add(""+(char)(col)+(char)(row - 1));
            dest.add(""+(char)(col + 1)+(char)(row - 1));
            dest.add(""+(char)(col - 1)+(char)(row - 1));
        }else if (col == 'a'){
            dest.add(""+(char)(col)+(char)(row+1));
            dest.add(""+(char)(col)+(char)(row-1));
            dest.add(""+(char)(col+1)+(char)(row));
            dest.add(""+(char)(col + 1)+(char)(row + 1));
            dest.add(""+(char)(col + 1)+(char)(row - 1));
        }else if (col == 'h'){
            dest.add(""+(char)(col)+(char)(row+1));
            dest.add(""+(char)(col)+(char)(row-1));
            dest.add(""+(char)(col-1)+(char)(row));
            dest.add(""+(char)(col - 1)+(char)(row + 1));
            dest.add(""+(char)(col - 1)+(char)(row - 1));
        }else{
            dest.add(""+(char)(col)+(char)(row+1));
            dest.add(""+(char)(col)+(char)(row-1));
            dest.add(""+(char)(col-1)+(char)(row));
            dest.add(""+(char)(col+1)+(char)(row));
            dest.add(""+(char)(col - 1)+(char)(row + 1));
            dest.add(""+(char)(col - 1)+(char)(row - 1));
            dest.add(""+(char)(col + 1)+(char)(row + 1));
            dest.add(""+(char)(col + 1)+(char)(row - 1));
        }
        for (String s : dest){
            if (b.getPiece(s) == null || b.getPiece(s).color() != super.color()){
                options.add(s);
            }
        }
        return options;
    }

}