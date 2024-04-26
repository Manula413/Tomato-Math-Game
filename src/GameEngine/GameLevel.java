package GameEngine;

import java.util.List;

public class GameLevel {
    private int answerCount;
    private int timeLimitInSeconds;
    private int correctAnswersToPass; // New variable

    public GameLevel(int answerCount, int timeLimitInSeconds, int correctAnswersToPass) {
        this.answerCount = answerCount;
        this.timeLimitInSeconds = timeLimitInSeconds;
        this.correctAnswersToPass = correctAnswersToPass;
    }

    public int getAnswerCount() {
        return answerCount;
    }

    public int getTimeLimitInSeconds() {
        return timeLimitInSeconds;
    }

    public int getCorrectAnswersToPass() {
        return correctAnswersToPass;
    }
}
