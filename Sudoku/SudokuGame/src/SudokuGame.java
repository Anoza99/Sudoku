import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class SudokuGame extends JFrame {

    private static final int GRID_SIZE = 9;
    private static final int SUBGRID_SIZE = 3;

    private final JTextField[][] grid;
    private int[][] solution;
    private int[][] puzzle;

    int countSeconds;
    int countMinutes;

    boolean congrats = true;

    String difficulty;

    /**
     * Constructor for the SudokuGame class
     */
    public SudokuGame() {
        setTitle("Sudoku Game");// Set the title of the frame
        setSize(400, 400);// Set the size of the frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close the program on window close
        setLocationRelativeTo(null);// Center the frame on the screen

        /** Create a panel for the Sudoku grid */
        JPanel sudokuPanel = new JPanel();
        sudokuPanel.setLayout(new GridLayout(GRID_SIZE, GRID_SIZE));

        /** Initialize the grid*/
        grid = new JTextField[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = new JTextField();
                grid[i][j].setHorizontalAlignment(JTextField.CENTER);
                sudokuPanel.add(grid[i][j]);
            }
        }

        /**Create a panel for buttons*/
        JPanel buttonPanel = new JPanel();
        JButton newGameButton = new JButton("New Game");
        JButton solveButton = new JButton("Solve");
        JButton checkButton = new JButton("Check");
        String[] difficultyLevels = {"Easy", "Medium", "Hard"};
        JComboBox<String> difficultyComboBox = new JComboBox<>(difficultyLevels);


        buttonPanel.add(newGameButton);
        buttonPanel.add(solveButton);
        buttonPanel.add(checkButton);
        buttonPanel.add(new JLabel("Difficulty: "));
        buttonPanel.add(difficultyComboBox);

        /**Create panel for timer*/
        JPanel timerPanel = new JPanel();
        JLabel minutesLabel = new JLabel("0 min");
        JLabel secondsLabel = new JLabel("0 sec");

        timerPanel.add(minutesLabel);
        timerPanel.add(secondsLabel);

        Timer timer = new Timer(1000, e -> {
            countSeconds++;
            if (countSeconds < 60) {
                secondsLabel.setText(countSeconds + " sec");
            } else if (countMinutes < 60) {
                countSeconds = 0;
                countMinutes++;
                minutesLabel.setText(countMinutes + " min");
                secondsLabel.setText(countSeconds + " sec");
            } else {
                ((Timer) (e.getSource())).stop();
            }
        });
        timer.setInitialDelay(0);

        /** Add action listeners to the buttons*/
        newGameButton.addActionListener(e -> {
            String selectedDifficulty = (String) difficultyComboBox.getSelectedItem();
            generateNewGame(selectedDifficulty);
            countMinutes = 0;
            countSeconds = 0;
            difficulty = selectedDifficulty;
            timer.start();
        });

        solveButton.addActionListener(e -> solveGame());

        checkButton.addActionListener(e -> checkSolution(congrats));

        /** Add panels to the frame*/
        add(sudokuPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(timerPanel, BorderLayout.NORTH);
    }

    /**
     * Method to generate a new Sudoku game
     */
    private void generateNewGame(String difficulty) {
        solution = generateSudokuSolution();// Generate a solution for the Sudoku puzzle
        puzzle = generatePuzzle(solution, difficulty);// Generate a puzzle based on the solution
        updateGrid();
    }

    /**
     * Method to generate a Sudoku solution
     */
    private int[][] generateSudokuSolution() {
        int[][] sudoku = new int[GRID_SIZE][GRID_SIZE];
        solveSudoku(sudoku);
        return sudoku;
    }

    /**
     * Method to solve the Sudoku puzzle using backtracking
     */
    private void solveSudoku(int[][] sudoku) {
        solveSudokuHelper(sudoku);
    }

    /**
     * Helper method for backtracking Sudoku solving algorithm
     */
    private boolean solveSudokuHelper(int[][] sudoku) {
        int[] numbers = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        shuffleArray(numbers);

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (sudoku[row][col] == 0) {
                    for (int num : numbers) {
                        if (isValidMove(sudoku, row, col, num)) {
                            sudoku[row][col] = num;

                            if (solveSudokuHelper(sudoku)) {
                                return true;
                            }

                            sudoku[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Method to shuffle an array of integers
     */
    private void shuffleArray(int[] array) {
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            // Swap array[i] and array[index]
            int temp = array[i];
            array[i] = array[index];
            array[index] = temp;
        }
    }

    /**
     * Method to check if a move is valid in the Sudoku grid
     */
    private boolean isValidMove(int[][] sudoku, int row, int col, int num) {
        // Check if 'num' is not in the current row and column
        for (int i = 0; i < GRID_SIZE; i++) {
            if (sudoku[row][i] == num || sudoku[i][col] == num) {
                return false;
            }
        }

        /** Check if 'num' is not in the current 3x3 subgrid*/
        int subgridRowStart = row - row % SUBGRID_SIZE;
        int subgridColStart = col - col % SUBGRID_SIZE;
        for (int i = 0; i < SUBGRID_SIZE; i++) {
            for (int j = 0; j < SUBGRID_SIZE; j++) {
                if (sudoku[subgridRowStart + i][subgridColStart + j] == num) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Method to generate a puzzle based on the solution and difficulty level
     */
    private int[][] generatePuzzle(int[][] solution, String difficulty) {
        Random random = new Random();
        int[][] puzzle = new int[GRID_SIZE][GRID_SIZE];

        /** Copy the solution to the puzzle*/
        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(solution[i], 0, puzzle[i], 0, GRID_SIZE);
        }

        /** Remove numbers based on difficulty*/
        double removalPercentage = switch (difficulty) {
            case "Easy" -> 0.35;
            case "Medium" -> 0.50;
            case "Hard" -> 0.65;
            default -> 0.50;
        };

        int cellsToRemove = (int) (removalPercentage * GRID_SIZE * GRID_SIZE);

        /** Randomly remove numbers from the puzzle*/
        for (int i = 0; i < cellsToRemove; i++) {
            int row = random.nextInt(GRID_SIZE);
            int col = random.nextInt(GRID_SIZE);

            if (puzzle[row][col] != 0) {
                puzzle[row][col] = 0;
            } else {
                i--;
            }
        }

        return puzzle;
    }

    /**
     * Method to update the UI grid with the current puzzle
     */
    private void updateGrid() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (puzzle[i][j] == 0) {
                    grid[i][j].setText("");
                    grid[i][j].setEditable(true);
                } else {
                    grid[i][j].setText(String.valueOf(puzzle[i][j]));
                    grid[i][j].setEditable(false);
                }
            }
        }
    }

    /**
     * Method to instantly solve the game and display the solution
     */
    private void solveGame() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j].setText(String.valueOf(solution[i][j]));
                grid[i][j].setEditable(false);
                grid[i][j].setBackground(Color.WHITE); // Reset background color
            }
        }
    }

    /**
     * Method to check the user's solution
     */
    private void checkSolution(boolean congrats) {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j].isEditable()) {
                    try {
                        int value = Integer.parseInt(grid[i][j].getText());
                        if (value != solution[i][j]) {
                            /** Highlight incorrect cells in red*/
                            grid[i][j].setBackground(Color.RED);
                            congrats = false;
                        } else {
                            /** Reset background color for correct cells*/
                            grid[i][j].setBackground(Color.WHITE);
                        }
                    } catch (NumberFormatException e) {
                        /** Handle non-integer input (e.g., empty cell)*/
                        grid[i][j].setBackground(Color.WHITE);
                        congrats = false;
                    }
                }
            }
        }
        if (congrats) {
            JOptionPane.showMessageDialog(this,
                    "Congratulations! You've completed the Sudoku puzzle!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            saveTimeRecord();
        }
    }

    private void saveTimeRecord() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("SudokuGame\\src\\" + difficulty + " Records", true))) {
            /** Write the time to the file depends on difficulty*/
            writer.write("Game Time: " + countMinutes + " min " + countSeconds + " sec" + "\n");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving time record.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SudokuGame().setVisible(true));
    }
}