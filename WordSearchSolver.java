import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

/**
 * A simple word search solver which finds words in a word search puzzle.
 * 
 * The program takes a user specified input file which contains a word search
 * puzzle.
 * It is assumed that the puzzle file is a rectangular grid of characters
 * separated by spaces.
 * 
 * The program then takes a user specified dictionary file which contains a list
 * of words to search for.
 * Each word in the dictionary is to be separated by a newline character.
 * 
 * The solution is then outputted in the form of a text file called
 * "solution.txt"
 * 
 * @author Yimin
 * @version 1.0
 * @date 2022/3/21
 */
public class WordSearchSolver {
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

    private static int fileWidth(File file) throws Exception {
        Scanner scanner = new Scanner(file);
        int length = scanner.nextLine().length();
        scanner.close();
        return length;
    }

    private static char[][] processPuzzleFile(File file) throws Exception {
        int fileHeight = fileHeight(file);
        int fileWidth = fileWidth(file);

        int lettersPerRow = (int) Math.ceil((double) fileWidth / 2);

        char[][] puzzle = new char[fileHeight][lettersPerRow];
        Scanner scanner = new Scanner(file);
        for (int i = 0; i < fileHeight; i++) {
            String line = scanner.nextLine();
            for (int j = 0; j < lettersPerRow; j++) {
                // Multiply j by 2 because there are spaces between each character
                puzzle[i][j] = line.charAt(j * 2);
            }
        }
        scanner.close();

        return puzzle;
    }

    private static String[] processWordsFile(File file) throws Exception {
        Scanner scanner = new Scanner(file);
        String[] words = new String[fileHeight(file)];
        for (int i = 0; i < words.length; i++) {
            words[i] = scanner.nextLine();
        }
        scanner.close();
        return words;
    }

    private static String reversed(String string) {
        String reversed = "";
        for (int i = string.length() - 1; i >= 0; i--) {
            reversed += string.charAt(i);
        }
        return reversed;
    }

    private static void fillRange(boolean[] array, int startIndex, int endIndex, boolean value) {
        for (int i = startIndex; i < endIndex; i++) {
            array[i % array.length] = value;
        }
    }

    private static boolean wordIsAtIndex(char[] array, int index, String word) {
        boolean match = true;
        for (int charIndex = 0; charIndex < word.length() && match; charIndex++) {
            int searchIndex = (index + charIndex) % array.length;
            if (array[searchIndex] != word.charAt(charIndex)) {
                match = false;
            }
        }
        return match;
    }

    private static boolean[] wordMatches(char[] arrayToSearch, String[] words) {
        boolean[] matchLocations = new boolean[arrayToSearch.length];

        for (String word : words) {
            for (int i = 0; i < arrayToSearch.length; i++) {
                if (wordIsAtIndex(arrayToSearch, i, word) || wordIsAtIndex(arrayToSearch, i, reversed(word))) {
                    fillRange(matchLocations, i, i + word.length(), true);
                }
            }
        }

        return matchLocations;
    }

    private static void solveLine(char[][] puzzle, boolean[][] solution, String[] words,
            int startingX, int startingY, int offsetX, int offsetY) {
        int puzzleHeight = puzzle.length;
        int puzzleWidth = puzzle[0].length;

        if (offsetX < 0) {
            offsetX += puzzleWidth;
        }
        if (offsetY < 0) {
            offsetY += puzzleHeight;
        }

        char[] line = new char[Math.max(puzzleHeight, puzzleWidth)];
        // Convert the line from it's 2D form to 1D form to be processed
        for (int charIndex = 0; charIndex < line.length; charIndex++) {
            int i = (startingY + charIndex * offsetY) % puzzleHeight;
            int j = (startingX + charIndex * offsetX) % puzzleWidth;

            line[charIndex] = puzzle[i][j];
        }

        boolean[] matchLocations = wordMatches(line, words);
        // Convert the processed 1D array back to it's 2D form to store in the solution
        for (int charIndex = 0; charIndex < line.length; charIndex++) {
            int i = (startingY + charIndex * offsetY) % puzzleHeight;
            int j = (startingX + charIndex * offsetX) % puzzleWidth;

            if (matchLocations[charIndex]) {
                solution[i][j] = true;
            }
        }
    }

    private static boolean[][] solvePuzzle(char[][] puzzle, String[] words) {
        boolean[][] solution = new boolean[puzzle.length][puzzle[0].length];

        int puzzleHeight = puzzle.length;
        int puzzleWidth = puzzle[0].length;

        // Solve rows
        for (int row = 0; row < puzzleHeight; row++) {
            solveLine(puzzle, solution, words, 0, row, 1, 0);
        }

        // Solve columns
        for (int col = 0; col < puzzleWidth; col++) {
            solveLine(puzzle, solution, words, col, 0, 0, 1);
        }

        // Solve diagonals
        for (int row = 0; row < puzzleHeight; row++) {
            for (int col = 0; col < puzzleWidth; col++) {
                solveLine(puzzle, solution, words, col, row, 1, 1);
                solveLine(puzzle, solution, words, col, row, 1, -1);
            }
        }

        return solution;
    }

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

    public static void main(String[] args) throws Exception {
        final String SOLUTION_FILE_NAME = "solution.txt";

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter puzzle file name: ");
        String puzzleFileName = scanner.nextLine();
        System.out.print("Enter dictionary file name: ");
        String wordsFileName = scanner.nextLine();

        scanner.close();

        File puzzleFile = new File(puzzleFileName);
        char[][] puzzle = processPuzzleFile(puzzleFile);
        File wordsFile = new File(wordsFileName);
        String[] words = processWordsFile(wordsFile);

        boolean[][] solution = solvePuzzle(puzzle, words);

        File output = new File(SOLUTION_FILE_NAME);
        outputSolution(output, puzzle, solution);
    }
}
