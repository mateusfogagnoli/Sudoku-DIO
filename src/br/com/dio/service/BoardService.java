package br.com.dio.service;

import br.com.dio.model.Board;
import br.com.dio.model.GameStatusEnum;
import br.com.dio.model.Space;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BoardService {

    private final static int BOARD_LIMIT = 9;

    private Board board;
    private final List<Integer> templateExpected = new ArrayList<>();
    private final List<Boolean> templateFixed = new ArrayList<>();
    private int currentFixedCount = 0;

    public BoardService(final Map<String, String> gameConfig) {
        initTemplate(gameConfig);
        // default fixed count: if template provided use its fixed count, otherwise 30
        var initialFixedCount = (int) templateFixed.stream().filter(Boolean::booleanValue).count();
        if (initialFixedCount == 0) initialFixedCount = 30;
        shuffle(initialFixedCount);
    }

    public List<List<Space>> getSpaces(){
        return board.getSpaces();
    }

    public void reset(){
        board.reset();
    }

    public boolean hasErrors(){
        return board.hasErrors();
    }

    public GameStatusEnum getStatus(){
        return board.getStatus();
    }

    public boolean gameIsFinished(){
        return board.gameIsFinished();
    }

    private List<List<Space>> initBoard(final Map<String, String> gameConfig) {
        // not used anymore; kept for compatibility
        return Collections.emptyList();
    }

    private void initTemplate(final Map<String, String> gameConfig){
        templateExpected.clear();
        templateFixed.clear();
        for (int i = 0; i < BOARD_LIMIT; i++) {
            for (int j = 0; j < BOARD_LIMIT; j++) {
                var positionConfig = gameConfig.get("%s,%s".formatted(i, j));
                int expected = 0;
                boolean fixed = false;
                if (positionConfig != null && positionConfig.contains(",")){
                    try {
                        expected = Integer.parseInt(positionConfig.split(",")[0]);
                    } catch (NumberFormatException e){
                        expected = 0;
                    }
                    fixed = Boolean.parseBoolean(positionConfig.split(",")[1]);
                }
                templateExpected.add(expected);
                templateFixed.add(fixed);
            }
        }
    }

    public void shuffle(final int fixedCount){
        this.currentFixedCount = fixedCount;
        // generate a full valid solution first
        int[][] solution = generateFullSolution();

        // choose fixed positions randomly
        var positions = IntStream.range(0, BOARD_LIMIT * BOARD_LIMIT).boxed().collect(Collectors.toList());
        Collections.shuffle(positions);
        Set<Integer> fixedPositions = positions.stream()
                .limit(Math.max(0, Math.min(fixedCount, positions.size())))
                .collect(Collectors.toCollection(HashSet::new));

        List<List<Space>> spaces = new ArrayList<>();
        var idx = 0;
        for (int i = 0; i < BOARD_LIMIT; i++){
            spaces.add(new ArrayList<>());
            for (int j = 0; j < BOARD_LIMIT; j++){
                var expected = solution[j][i]; // note: board uses spaces.get(col).get(row)
                var fixed = fixedPositions.contains(idx);
                spaces.get(i).add(new Space(expected, fixed));
                idx++;
            }
        }

        this.board = new Board(spaces);
    }

    public int getCurrentFixedCount(){
        return currentFixedCount;
    }

    private int[][] generateFullSolution(){
        int[][] board = new int[BOARD_LIMIT][BOARD_LIMIT];
        fillBoard(board, 0, 0);
        return board;
    }

    private boolean fillBoard(int[][] board, int row, int col){
        if (row == BOARD_LIMIT) return true;
        int nextRow = (col == BOARD_LIMIT - 1) ? row + 1 : row;
        int nextCol = (col == BOARD_LIMIT - 1) ? 0 : col + 1;

        List<Integer> nums = IntStream.rangeClosed(1, 9).boxed().collect(Collectors.toList());
        Collections.shuffle(nums);
        for (int num : nums){
            if (isSafe(board, row, col, num)){
                board[row][col] = num;
                if (fillBoard(board, nextRow, nextCol)) return true;
                board[row][col] = 0;
            }
        }
        return false;
    }

    private boolean isSafe(int[][] board, int row, int col, int num){
        // row
        for (int c = 0; c < BOARD_LIMIT; c++) if (board[row][c] == num) return false;
        // col
        for (int r = 0; r < BOARD_LIMIT; r++) if (board[r][col] == num) return false;
        // box
        int boxStartRow = row - row % 3;
        int boxStartCol = col - col % 3;
        for (int r = boxStartRow; r < boxStartRow + 3; r++){
            for (int c = boxStartCol; c < boxStartCol + 3; c++){
                if (board[r][c] == num) return false;
            }
        }
        return true;
    }
}
