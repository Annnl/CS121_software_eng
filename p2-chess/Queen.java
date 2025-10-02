import java.util.*;

public class Queen extends Piece {
    public Queen(Color c) { super(c); }
    // implement appropriate methods

    public String toString() {
	    if (color() == Color.BLACK){
            return "bq";
        }else{
            return "wq";
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
        boolean flag = true;
        int dist = 1;
        while (flag){
            if ((num_col - dist) < 0){
                break;
            }
            if (b.getPiece(""+(char)(col - dist)+(char)(row)) != null){
                if (b.getPiece(""+(char)(col - dist)+(char)(row)).color() != super.color()){
                    options.add(""+(char)(col - dist)+(char)(row));
                    flag = false;
                }else{
                    flag = false;
                }
            }else{
                options.add(""+(char)(col - dist)+(char)(row));
            }
            dist++;
            if ((num_col - dist) < 0){
                flag = false;
            }
        }
        flag = true;
        dist = 1;
        while (flag){
            if ((num_col + dist) > 7){
                break;
            }
            if (b.getPiece(""+(char)(col + dist)+(char)(row)) != null){
                if (b.getPiece(""+(char)(col + dist)+(char)(row)).color() != super.color()){
                    options.add(""+(char)(col + dist)+(char)(row));
                    flag = false;
                }else{
                    flag = false;
                }
            }else{
                options.add(""+(char)(col + dist)+(char)(row));
            }
            dist++;
            if ((num_col + dist) > 7){
                flag = false;
            }
        }
        flag = true;
        dist = 1;
        while (flag){
            if ((num_row - dist) < 0){
                break;
            }
            if (b.getPiece(""+(char)(col )+(char)(row- dist)) != null){
                if (b.getPiece(""+(char)(col )+(char)(row- dist)).color() != super.color()){
                    options.add(""+(char)(col )+(char)(row- dist));
                    flag = false;
                }else{
                    flag = false;
                }
            }else{
                options.add(""+(char)(col)+(char)(row- dist));
            }
            dist++;
            if ((num_row - dist) < 0){
                flag = false;
            }
        }
        flag = true;
        dist = 1;
        while (flag){
            if ((num_row + dist) > 7){
                break;
            }
            if (b.getPiece(""+(char)(col )+(char)(row+ dist)) != null){
                if (b.getPiece(""+(char)(col )+(char)(row+ dist)).color() != super.color()){
                    options.add(""+(char)(col )+(char)(row+ dist));
                    flag = false;
                }else{
                    flag = false;
                }
            }else{
                options.add(""+(char)(col)+(char)(row+ dist));
            }
            dist++;
            if ((num_row + dist) > 7){
                flag = false;
            }
        }
        flag = true;
        dist = 1;
        while (flag){
            if ((num_col - dist) < 0 || (num_row - dist) < 0){
                break;
            }
            if (b.getPiece(""+(char)(col - dist)+(char)(row - dist)) != null){
                if (b.getPiece(""+(char)(col - dist)+(char)(row - dist)).color() != super.color()){
                    options.add(""+(char)(col - dist)+(char)(row - dist));
                    flag = false;
                }else{
                    flag = false;
                }
            }else{
                options.add(""+(char)(col - dist)+(char)(row - dist));
            }
            dist++;
            if ((num_col - dist) < 0 || (num_row - dist) < 0){
                flag = false;
            }
        }
        flag = true;
        dist = 1;
        while (flag){
            if ((num_col + dist) > 7 || (num_row + dist) > 7){
                break;
            }
            if (b.getPiece(""+(char)(col + dist)+(char)(row + dist)) != null){
                if (b.getPiece(""+(char)(col + dist)+(char)(row + dist)).color() != super.color()){
                    options.add(""+(char)(col + dist)+(char)(row + dist));
                    flag = false;
                }else{
                    flag = false;
                }
            }else{
                options.add(""+(char)(col + dist)+(char)(row + dist));
            }
            dist++;
            if ((num_col + dist) > 7 || (num_row + dist) > 7){
                flag = false;
            }
        }
        flag = true;
        dist = 1;
        while (flag){
            if ((num_col - dist) < 0 || (num_row + dist) > 7){
                break;
            }
            if (b.getPiece(""+(char)(col - dist)+(char)(row + dist)) != null){
                if (b.getPiece(""+(char)(col - dist)+(char)(row + dist)).color() != super.color()){
                    options.add(""+(char)(col - dist)+(char)(row + dist));
                    flag = false;
                }else{
                    flag = false;
                }
            }else{
                options.add(""+(char)(col - dist)+(char)(row + dist));
            }
            dist++;
            if ((num_col - dist) < 0 || (num_row + dist) > 7){
                flag = false;
            }
        }
        flag = true;
        dist = 1;
        while (flag){
            if ((num_col + dist) > 7 || (num_row - dist) < 0){
                break;
            }
            if (b.getPiece(""+(char)(col + dist)+(char)(row - dist)) != null){
                if (b.getPiece(""+(char)(col + dist)+(char)(row - dist)).color() != super.color()){
                    options.add(""+(char)(col + dist)+(char)(row - dist));
                    flag = false;
                }else{
                    flag = false;
                }
            }else{
                options.add(""+(char)(col + dist)+(char)(row - dist));
            }
            dist++;
            if ((num_col + dist) > 7 || (num_row - dist) < 0){
                flag = false;
            }
        }
        return options;
    }
}