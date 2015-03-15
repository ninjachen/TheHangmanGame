package ninjachen.me.thehangmangame.model;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import ninjachen.me.thehangmangame.utils.HttpUtils;

/**
 * Created by Ninja on 2015/3/15.
 */
public class HangManGame {
    public static final String REQUEST_URL = "https://strikingly-hangman.herokuapp.com/game/on";
    public static final String PLAYER_ID = "jiachenning@gmail.com";
    private static String TAG = HangManGame.class.getSimpleName();
    //date init with the constructor
    public final int NUMBER_OF_WORDS_TO_GUESS;
    public final int NUMBER_OF_GUESS_ALLOWED_FOR_EACH_WORD;

    String sessionId;
    String score;
    //word - gives you the word you need to guess.
    String word;
    //totalWordCount - tells you the number of words that you have tried.
    int totalWordCount;
    //wrongGuessCountOfCurrentWord - tells you the number of wrong guess you already made on this word.
    int wrongGuessCountOfCurrentWord;

    List<Character> guessedLetters;

    //should not be invoked
    private HangManGame() {
        this.NUMBER_OF_WORDS_TO_GUESS = -1;
        this.NUMBER_OF_GUESS_ALLOWED_FOR_EACH_WORD = -1;
    }

    private HangManGame(String sessionId, int numberOfWordsToGuess, int numberOfGuessAllowedForEachWord) {
        setSessionId(sessionId);
        this.NUMBER_OF_WORDS_TO_GUESS = numberOfWordsToGuess;
        this.NUMBER_OF_GUESS_ALLOWED_FOR_EACH_WORD = numberOfGuessAllowedForEachWord;
    }

    /**
     * instance by json string
     *
     * @param newgameStr expected like: {"message":"THE GAME IS ON","sessionId":"04ccda515d152cbe630e156af9095104","data":{"numberOfWordsToGuess":80,"numberOfGuessAllowedForEachWord":10}}
     * @return HangManGame;
     * if newgameStr is  not valid ,return null
     */
    public static HangManGame instance(String newgameStr) {
        if (TextUtils.isEmpty(newgameStr))
            return null;

        try {
            JSONObject jsonObject = new JSONObject(newgameStr);
            String sessionId = jsonObject.getString("sessionId");
            JSONObject data = jsonObject.getJSONObject("data");
            int numberOfWordsToGuess = data.getInt("numberOfWordsToGuess");
            int numberOfGuessAllowedForEachWord = data.getInt("numberOfGuessAllowedForEachWord");
            HangManGame game = new HangManGame(sessionId, numberOfWordsToGuess, numberOfGuessAllowedForEachWord);
            return game;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        //error
        return null;
    }

    //expected like: {"sessionId":"3f0421bb5cb56631c170a35da90161d2","data":{"word":"*****","totalWordCount":1,"wrongGuessCountOfCurrentWord":0}}
    public boolean requestNewWord(){
        try {
            JSONObject requestParams = new JSONObject();
            requestParams.put("sessionId", sessionId);
            requestParams.put("action", "nextWord");
            String response = HttpUtils.callInHTTPPost(REQUEST_URL, requestParams);
            JSONObject jsonObject = new JSONObject(response);
            JSONObject data = jsonObject.getJSONObject("data");
            setWord(data.getString("word"));
            setTotalWordCount(data.getInt("totalWordCount"));
            setWrongGuessCountOfCurrentWord(data.getInt("wrongGuessCountOfCurrentWord"));
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    //todo
    //expected like: {"sessionId":"3f0421bb5cb56631c170a35da90161d2","data":{"word":"*****","totalWordCount":1,"wrongGuessCountOfCurrentWord":0}}
    public boolean guessWord(String guessChar){
        try {
            JSONObject requestParams = new JSONObject();
            requestParams.put("sessionId", sessionId);
            requestParams.put("action", "guessWord");
            requestParams.put("guess", guessChar);
            String response = HttpUtils.callInHTTPPost(REQUEST_URL, requestParams);
            JSONObject jsonObject = new JSONObject(response);
            JSONObject data = jsonObject.getJSONObject("data");
            setWord(data.getString("word"));
            setTotalWordCount(data.getInt("totalWordCount"));
            setWrongGuessCountOfCurrentWord(data.getInt("wrongGuessCountOfCurrentWord"));
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
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

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getTotalWordCount() {
        return totalWordCount;
    }

    public void setTotalWordCount(int totalWordCount) {
        this.totalWordCount = totalWordCount;
    }

    public int getWrongGuessCountOfCurrentWord() {
        return wrongGuessCountOfCurrentWord;
    }

    public void setWrongGuessCountOfCurrentWord(int wrongGuessCountOfCurrentWord) {
        this.wrongGuessCountOfCurrentWord = wrongGuessCountOfCurrentWord;
    }

    /**
     * validate the guessWord
     * You can only guess ONE character per request and only CAPITAL letter is accepted
     *
     * @param guessChar
     * @return isValidated
     */
    public static boolean validateWord(String guessChar) {
        if (guessChar != null && guessChar.length() == 1) {
            char c = guessChar.charAt(0);
            if (c >= 'A' && c <= 'Z')
                return true;
        }
        return false;
    }

    /**
     * is current word failed
     * @return
     */
    public boolean isCurrentWordFailed() {
        return getWrongGuessCountOfCurrentWord() > NUMBER_OF_GUESS_ALLOWED_FOR_EACH_WORD;
    }

    /**
     * is current word hited
     * @return
     */
    public boolean isCurrentWordHit() {
        return getWord().indexOf("*") < 0;
    }
}
