import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

/**
 * A simple word search solver which finds the words from a puzzle.
 * 
 * The program takes an input file which contains the word search
 * puzzle.
 * It is assumed that the puzzle file is a rectangular grid of characters
 * separated by spaces.
 * 
 * The program then takes a user specified workbank file which contains a list
 * of words to search for.
 * Each word in the wordbank is separated by a newline.
 * 
 * The solution is then outputted to a text file called
 * "solution.txt"
 * 
 * @author Yimin
 * @version 2.0
 * @date 2022/3/21
 */
public class WordSearchSolver {
    /**
     * Gets the character height of a file
     * 
     * @param file the file to get the height of
     * @return the height of the file
     * @throws Exception
     */
    private static int fileHeight(File file) throws Exception {
        Scanner scanner = new Scanner(file);
        int numRows = 0;
        while (scanner.hasNextLine()) {
            numRows++;
            scanner.nextLine();
        }
        scanner.close();
        return numRows;
    }

    /**
     * Gets the character width of a file assuming all rows
     * are the same length.
     * 
     * @param file the file to get the width of
     * @return the width of the file
     * @throws Exception
     */
    private static int fileWidth(File file) throws Exception {
        Scanner scanner = new Scanner(file);
        int length = scanner.nextLine().length();
        scanner.close();
        return length;
    }

    /**
     * Converts a file containing a rectangular grid of characters separated by
     * spaces into a java 2D array of characters.
     * 
     * @param file the file containing the grid of characters
     * @return a 2D array of characters
     * @throws Exception
     */
    private static char[][] processPuzzleFile(File file) throws Exception {
        int fileHeight = fileHeight(file);
        int fileWidth = fileWidth(file);

        // Divide by 2 to account for the space between letters
        // Ceil allows for both a trailing space or no trailing space at end of line
        int lettersPerRow = (int) Math.ceil((double) fileWidth / 2);

        char[][] puzzle = new char[fileHeight][lettersPerRow];
        Scanner scanner = new Scanner(file);
        for (int row = 0; row < fileHeight; row++) {
            String line = scanner.nextLine();
            for (int col = 0; col < lettersPerRow; col++) {
                // Multiply col by 2 to account for the space between characters
                puzzle[row][col] = line.charAt(col * 2);
            }
        }
        scanner.close();

        return puzzle;
    }

    /**
     * Converts a list of strings from a file into a java array of strings.
     * 
     * @param file the file to read from.
     * @return the array of strings.
     * @throws Exception
     */
    private static String[] processWordbankFile(File file) throws Exception {
        Scanner scanner = new Scanner(file);
        String[] wordbank = new String[fileHeight(file)];
        for (int row = 0; row < wordbank.length; row++) {
            wordbank[row] = scanner.nextLine();
        }
        scanner.close();
        return wordbank;
    }

    /**
     * Reverses the order of the characters in a string.
     * 
     * @param string the string to reverse
     * @return the reversed string
     */
    private static String reversed(String string) {
        String reversed = "";
        for (int i = string.length() - 1; i >= 0; i--) {
            reversed += string.charAt(i);
        }
        return reversed;
    }

    /**
     * If a word from the word bank (normal or reversed) is found to
     * start at the specified starting position and goes in the specified direction,
     * the positions of the letters of the word are added to the solution.
     * 
     * @param puzzle   the puzzle containing the letters
     * @param solution the method will store its solutions in this array (Previous
     *                 solutions are not cleared)
     * @param wordbank the word bank containing the words to search for
     * @param startRow the row of the starting position
     * @param startCol the column of the starting position
     * @param dirX     the direction in the x-axis to move after a letter is checked
     * @param dirY     the direction in the y-axis to move after a letter is checked
     */
    private static void solveDirection(char[][] puzzle, boolean[][] solution, String[] wordbank,
            int startRow, int startCol, int dirX, int dirY) {
        int puzzleHeight = puzzle.length;
        int puzzleWidth = puzzle[0].length;

        // Prevent negative indexing
        while (dirX < 0) {
            dirX += puzzleWidth;
        }
        while (dirY < 0) {
            dirY += puzzleHeight;
        }

        for (String word : wordbank) {
            // Keep checking if the puzzle letters match the word's letters until the end of
            // the word is reached (success) or a letter doesn't match (fail)
            boolean match = true;
            boolean matchReversed = true;
            for (int charIndex = 0; charIndex < word.length() && (match || matchReversed); charIndex++) {
                int row = (startRow + charIndex * dirY) % puzzleHeight;
                int col = (startCol + charIndex * dirX) % puzzleWidth;
                if (puzzle[row][col] != word.charAt(charIndex)) {
                    match = false;
                }
                if (puzzle[row][col] != reversed(word).charAt(charIndex)) {
                    matchReversed = false;
                }
            }

            // Add the positions of the letters to the solution if the word matches
            if (match || matchReversed) {
                for (int charIndex = 0; charIndex < word.length(); charIndex++) {
                    int row = (startRow + charIndex * dirY) % puzzleHeight;
                    int col = (startCol + charIndex * dirX) % puzzleWidth;

                    solution[row][col] = true;
                }
                // We don't break out of the loop because there may be multiple words that match
            }
        }
    }

    /**
     * Solves the word search puzzle by searching for words from the word bank,
     * and returns the positions of the letters of the words found.
     * 
     * @param puzzle   the puzzle containing the letters to search from
     * @param wordbank the word bank containing the words to search for
     * @return the positions of the letters of the words found
     */
    private static boolean[][] solvePuzzle(char[][] puzzle, String[] wordbank) {
        boolean[][] solution = new boolean[puzzle.length][puzzle[0].length];

        int puzzleHeight = puzzle.length;
        int puzzleWidth = puzzle[0].length;

        for (int row = 0; row < puzzleHeight; row++) {
            for (int col = 0; col < puzzleWidth; col++) {
                // Horizontal
                solveDirection(puzzle, solution, wordbank, row, col, 1, 0);
                // Vertical
                solveDirection(puzzle, solution, wordbank, row, col, 0, 1);
                // Diagonals
                solveDirection(puzzle, solution, wordbank, row, col, 1, 1);
                solveDirection(puzzle, solution, wordbank, row, col, 1, -1);
            }
        }

        return solution;
    }

    /**
     * Prints the puzzle solution to a file by only printing the letters
     * that belong to words from the word bank.
     * Letters not part of a word are replaced with empty spaces
     * 
     * @param file     The file to print the solution to
     * @param puzzle   The puzzle containing the letters
     * @param solution The solution containing the positions of the words
     * @throws Exception
     */
    private static void outputSolution(File file, char[][] puzzle, boolean[][] solution) throws Exception {
        int puzzleHeight = puzzle.length;
        int puzzleWidth = puzzle[0].length;

        String output = "";
        for (int i = 0; i < puzzleHeight; i++) {
            for (int j = 0; j < puzzleWidth; j++) {
                if (solution[i][j]) {
                    output += puzzle[i][j];
                } else {
                    output += " ";
                }
                if (j != puzzleWidth - 1) {
                    output += " ";
                }
            }
            output += "\n";
        }

        FileWriter writer = new FileWriter(file);
        writer.write(output);
        writer.close();
    }

    /**
     * Main method asking for input files,
     * finding the solution, and printing the solution to a file.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        final String SOLUTION_FILE_NAME = "solution.txt";

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter puzzle file name: ");
        String puzzleFileName = scanner.nextLine();
        System.out.print("Enter word bank file name: ");
        String wordbankFileName = scanner.nextLine();

        scanner.close();

        File puzzleFile = new File(puzzleFileName);
        char[][] puzzle = processPuzzleFile(puzzleFile);
        File wordsFile = new File(wordbankFileName);
        String[] wordbank = processWordbankFile(wordsFile);

        boolean[][] solution = solvePuzzle(puzzle, wordbank);

        File outputFile = new File(SOLUTION_FILE_NAME);
        outputSolution(outputFile, puzzle, solution);
    }
}
