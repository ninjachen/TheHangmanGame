package ninjachen.me.thehangmangame.Model;

import java.util.List;

/**
 * Created by Ninja on 2015/3/15.
 */
public class HangManGame {
    static final String REQUEST_URL = "https://strikingly-hangman.herokuapp.com/game/on";
    static final String PLAYER_ID = "jiachenning@gmail.com";
    String sessionId;
    String score;
    List<Character> guessedLetters;
    String currentWord;

    public static HangManGame instance(){
        HangManGame game = new HangManGame();
        //todo
        String sessionId = "";
        game.setSessionId(sessionId);
        return game;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public List<Character> getGuessedLetters() {
        return guessedLetters;
    }

    public void setGuessedLetters(List<Character> guessedLetters) {
        this.guessedLetters = guessedLetters;
    }

    public String getCurrentWord() {
        return currentWord;
    }

    public void setCurrentWord(String currentWord) {
        this.currentWord = currentWord;
    }

}
