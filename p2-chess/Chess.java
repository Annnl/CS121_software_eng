import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.util.*;
public class Chess {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java Chess layout moves");
        }
        Piece.registerPiece(new KingFactory());
        Piece.registerPiece(new QueenFactory());
        Piece.registerPiece(new KnightFactory());
        Piece.registerPiece(new BishopFactory());
        Piece.registerPiece(new RookFactory());
        Piece.registerPiece(new PawnFactory());
        Board.theBoard().registerListener(new Logger());
        // args[0] is the layout file name
        // args[1] is the moves file name
        // Put your code to read the layout file and moves files
        // here.
        Board b = Board.theBoard();
        List<String> layout_lines = null;
        List<String> move_lines = null;
        try {
            layout_lines = Files.readAllLines(Paths.get(args[0]));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String line : layout_lines){
            if (line.charAt(0)=='#'){
                continue;
            }else{
                String[] part = line.split("=", 2);
                b.addPiece(Piece.createPiece(part[1]), part[0]);
            }
        }

        try {
            move_lines = Files.readAllLines(Paths.get(args[1]));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String line : move_lines){
            if (line.charAt(0)=='#'){
                continue;
            }else{
                String[] part = line.split("-", 2);
                b.movePiece(part[0], part[1]);
            }
        }
        // Leave the following code at the end of the simulation:
        System.out.println("Final board:");
        Board.theBoard().iterate(new BoardPrinter());
        // IMPORTANT: Do not clean up or otherwise reset the board state here.
   }
}