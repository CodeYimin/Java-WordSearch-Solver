import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * A simple word search solver which finds words from a grid of letters.
 * 
 * The program takes an input file which contains the word search
 * puzzle.
 * It is assumed that the puzzle file is a rectangular grid of characters
 * separated by spaces.
 * 
 * The program then takes a user specified wordbank file which contains a list
 * of words to search for.
 * Each word in the wordbank is separated by a newline.
 * 
 * The solution containing the word locations is
 * then outputted to a text file called "solution.txt"
 * 
 * @author Yimin
 * @version 3.0 2022/3/27
 */
public class WordSearchSolver {
    /**
     * Finds the number of consecutive non empty lines in a file
     * starting from the first line
     * 
     * @param file the file to get the height of
     * @return number of non empty lines in the file
     * @throws Exception
     */
    private static int fileHeight(File file) throws Exception {
        int numRows = 0;
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine() && scanner.nextLine() != "") {
            numRows++;
        }
        scanner.close();

        return numRows;
    }

    /**
     * Gets the number of non space characters in the
     * first line of a file.
     * 
     * @param file the file to get the width of
     * @return the width of the file
     * @throws Exception
     */
    private static int fileNonSpaceWidth(File file) throws Exception {
        Scanner scanner = new Scanner(file);
        String line = scanner.nextLine();
        scanner.close();

        int width = 0;
        for (int charIndex = 0; charIndex < line.length(); charIndex++) {
            if (line.charAt(charIndex) != ' ') {
                width++;
            }
        }

        return width;
    }

    /**
     * Converts a file containing a rectangular grid of space-separated characters
     * into a java 2D array of characters.
     * 
     * @param file the file containing the grid of characters
     * @return a 2D array of characters
     * @throws Exception
     */
    private static char[][] processPuzzleFile(File file) throws Exception {
        int puzzleWidth = fileNonSpaceWidth(file);
        int puzzleHeight = fileHeight(file);

        char[][] puzzle = new char[puzzleHeight][puzzleWidth];

        Scanner scanner = new Scanner(file);
        for (int row = 0; row < puzzleHeight; row++) {
            for (int col = 0; col < puzzleWidth; col++) {
                puzzle[row][col] = scanner.next().charAt(0);
            }
        }
        scanner.close();

        return puzzle;
    }

    /**
     * Converts a new-line-separated list of strings from a file
     * into a java array of strings.
     * 
     * @param file the file to read from.
     * @return the array of strings.
     * @throws Exception
     */
    private static String[] processWordbankFile(File file) throws Exception {
        String[] wordbank = new String[fileHeight(file)];

        Scanner scanner = new Scanner(file);
        for (int row = 0; row < wordbank.length; row++) {
            wordbank[row] = scanner.nextLine();
        }
        scanner.close();

        return wordbank;
    }

    /**
     * Reverses the order of the characters in a string.
     * 
     * @param target the string to reverse
     * @return the reversed string
     */
    private static String reversed(String target) {
        String reversed = "";
        for (int i = target.length() - 1; i >= 0; i--) {
            reversed += target.charAt(i);
        }
        return reversed;
    }

    /**
     * Checks for any words (reversed version of each word is also tested)
     * from the wordbank that match the given direction
     * and starting position (position of the first letter of the word).
     * Upon finding a word that matches the criteria, the positions of the letters
     * of the word are added to the solution.
     * 
     * @param puzzle        the puzzle containing the letters
     * @param solutionStore the method will store its solutions in this 2D array
     *                      (Previously stored solutions are not cleared)
     * @param wordbank      the word bank containing the words to check for
     * @param startRow      the row of the starting position
     * @param startCol      the column of the starting position
     * @param dirX          the desired x-offset between each letter of the word
     * @param dirY          the desired y-offset between each letter of the word
     */
    private static void checkDirection(char[][] puzzle, boolean[][] solutionStore, String[] wordbank,
            int startRow, int startCol, int dirX, int dirY) {
        int puzzleHeight = puzzle.length;
        int puzzleWidth = puzzle[0].length;

        // Prevent negative indexing in the future
        while (dirX < 0) {
            dirX += puzzleWidth;
        }
        while (dirY < 0) {
            dirY += puzzleHeight;
        }

        for (String word : wordbank) {
            String wordReversed = reversed(word);

            // Keep checking if letters match until the end of
            // the word is reached (success) or a letter doesn't match (fail)
            boolean match = true;
            boolean matchReversed = true;
            for (int charIndex = 0; charIndex < word.length() && (match || matchReversed); charIndex++) {
                int row = (startRow + charIndex * dirY) % puzzleHeight;
                int col = (startCol + charIndex * dirX) % puzzleWidth;

                if (puzzle[row][col] != word.charAt(charIndex)) {
                    match = false;
                }
                if (puzzle[row][col] != wordReversed.charAt(charIndex)) {
                    matchReversed = false;
                }
            }

            if (match || matchReversed) {
                // Add the positions of the letters to the solution
                for (int charIndex = 0; charIndex < word.length(); charIndex++) {
                    int row = (startRow + charIndex * dirY) % puzzleHeight;
                    int col = (startCol + charIndex * dirX) % puzzleWidth;

                    solutionStore[row][col] = true;
                }
                // Don't break out of the word loop because
                // there may be multiple words that match
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
        int puzzleHeight = puzzle.length;
        int puzzleWidth = puzzle[0].length;

        boolean[][] solution = new boolean[puzzleHeight][puzzleWidth];

        // Search horizontal, vertical, and both diagonal axises
        int[][] directionsToSearch = { { 1, 0 }, { 0, 1 }, { 1, 1 }, { 1, -1 } };

        // Starting at each letter of the puzzle, test every direction for word matches
        for (int row = 0; row < puzzleHeight; row++) {
            for (int col = 0; col < puzzleWidth; col++) {
                for (int[] direction : directionsToSearch) {
                    checkDirection(puzzle, solution, wordbank, row, col, direction[0], direction[1]);
                }
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
        for (int row = 0; row < puzzleHeight; row++) {
            for (int col = 0; col < puzzleWidth; col++) {
                // Print letter if it's part of solution
                if (solution[row][col]) {
                    output += puzzle[row][col];
                } else {
                    output += " ";
                }

                // Space between each letter
                if (col != puzzleWidth - 1) {
                    output += " ";
                }
            }

            // New line between each row
            if (row != puzzleHeight - 1) {
                output += "\n";
            }
        }

        PrintWriter writer = new PrintWriter(file);
        writer.write(output);
        writer.close();
    }

    /**
     * Main method asking for input files,
     * finding the solution, and outputting the solution to a file.
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
        File wordbankFile = new File(wordbankFileName);
        char[][] puzzle = processPuzzleFile(puzzleFile);
        String[] wordbank = processWordbankFile(wordbankFile);

        boolean[][] solution = solvePuzzle(puzzle, wordbank);

        File outputFile = new File(SOLUTION_FILE_NAME);
        outputSolution(outputFile, puzzle, solution);

        System.out.println();
        System.out.println("The solution has been saved in: " + SOLUTION_FILE_NAME);
    }
}
