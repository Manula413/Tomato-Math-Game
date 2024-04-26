package GameEngine;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class GameEngineCampaign {

    private String thePlayer = null;
    private int counter = 0;
    private int score = 0;
    private GameServer theGames = new GameServer();
    private Game current = null;
    private int answeredQuestions = 0;
    private int totalAttempts = 0;
    private int correctAnswers = 0;

    private GameLevel[] gameLevels;
    private GameLevel currentLevel;

    public GameEngineCampaign(String player) {
        thePlayer = player;
        initializeGameLevels();
        currentLevel = gameLevels[0];

    }

    private void initializeGameLevels() {
        // Create instances of GameLevel for each level
        gameLevels = new GameLevel[]{
            new GameLevel(3, 180, 1),
            new GameLevel(5, 300, 3),
            new GameLevel(15, 420, 12),
            new GameLevel(20, 540, 16), // Add more levels as needed
        };
    }

    public BufferedImage nextGame() {
        if (answeredQuestions <= currentLevel.getAnswerCount()) {
            current = theGames.getRandomGame();
            counter++;
            //answeredQuestions++;  // Increment the count of answered questions for the current level
            return current.getImage(); // Returns the image of the next game
        } else {
            /*int nextLevelIndex = getNextLevelIndex();
            if (nextLevelIndex != -1) {
                currentLevel = gameLevels[nextLevelIndex];
                answeredQuestions = 0;  // Reset answeredQuestions for the new level
                correctAnswers = 0;     // Reset correctAnswers for the new level
                return nextGame();      // Recursively call nextGame to get the next game image
            } else {
                // No more levels, return null or a special value
                return null;
            }*/
        }
        return null;
    }

    public int getNextLevelIndex() {
        for (int i = 0; i < gameLevels.length - 1; i++) {
            if (currentLevel == gameLevels[i]) {
                return i + 1;
            }
        }
        return -1;  // Indicates that there are no more levels
    }

    public boolean checkSolution(int i) {
        if (!isGameOver()) {
            totalAttempts++;

            if (i == current.getSolution()) {
                score++;
                correctAnswers++;
                answeredQuestions++;  // Increment the count of answered questions for the current level
                return true;  // Correct answer
            } else {
                answeredQuestions++;
                return false;  // Incorrect answer
            }
        } else {
            return false;
        }
    }

    public void resetAnsweredQuestions() {
        answeredQuestions = 0;
    }

    public void resetCorrectAnswers() {
        correctAnswers = 0;
    }

    public void resetTotalAttempts() {
        totalAttempts = 0;
    }

    private boolean isGameOver() {
        return answeredQuestions >= currentLevel.getAnswerCount();
    }

    private boolean hasEnoughCorrectAnswers() {
        return correctAnswers >= currentLevel.getCorrectAnswersToPass();
    }

    public int getScore() {
        return score;
    }

    public int getGameCounter() {
        return counter;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public GameLevel getCurrentLevel() {
        return currentLevel;
    }

    public GameLevel[] getGameLevels() {
        return gameLevels;
    }

    public void setCurrentLevel(GameLevel level) {
        currentLevel = level;
    }

    public int getCurrentLevelNumber() {
        return Arrays.asList(gameLevels).indexOf(currentLevel) + 1;
    }

}
