package ninjachen.me.thehangmangame;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import ninjachen.me.thehangmangame.model.HangManGame;
import ninjachen.me.thehangmangame.utils.HttpUtils;


public class MainActivity extends ActionBarActivity {
    private String TAG = MainActivity.class.getSimpleName();

    @InjectView(R.id.score)
    TextView scoreTV;

    @InjectView(R.id.currentWord)
    TextView currentWordTV;

    @InjectView(R.id.wrongGuessCountOfCurrentWord)
    TextView wrongGuessCountOfCurrentWordTV;

    @InjectView(R.id.nummberOfGuessAllowedForEachWord)
    TextView nummberOfGuessAllowedForEachWordTv;

    @InjectView(R.id.guessLetter)
    EditText guessLetterET;

    @InjectView(R.id.start)
    Button startBTN;

    @InjectView(R.id.guess)
    Button guessBTN;

    @InjectView(R.id.getScore)
    Button getScoreBTN;

    @InjectView(R.id.submitScore)
    Button submitScoreBTN;

    @InjectView(R.id.image)
    ImageView imageView;

    @InjectView(R.id.guessLettersLayout)
    ViewGroup guessLettersLayout;

    //the game core
    HangManGame hangManGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hangManGame == null)
            startNewGame();
    }

    @OnClick(R.id.start)
    public void startNewGame() {
        StartGameTask startGameTask = new StartGameTask();
        startGameTask.execute(HangManGame.PLAYER_ID);
    }

    @OnClick(R.id.nextWord)
    public void nextWord() {
        NextWordTask nextWordTask = new NextWordTask();
        nextWordTask.execute();
    }

    @OnClick(R.id.getScore)
    public void getScore() {
        GetScoreTask getScoreTask = new GetScoreTask();
        getScoreTask.execute();
    }

    @OnClick(R.id.submitScore)
    public void submitScore() {
        SubmitScoreTask submitScoreTask = new SubmitScoreTask();
        submitScoreTask.execute();
    }

//    @OnClick(R.id.guessLettersLayout)
    public void guessLetter(View view){
        if (hangManGame == null) {
            Toast.makeText(this, getString(R.string.wait_for_the_game_init), Toast.LENGTH_LONG).show();
        }
        if(view instanceof TextView){
            TextView textView = (TextView) view;
            String letter = textView.getText().toString();
            //add a EXCLUSIVE,and disable the textview
            SpannableString spannableString = new SpannableString(letter);
            spannableString.setSpan(new StrikethroughSpan(), 0, letter.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(spannableString);
            textView. setEnabled(false);
            GuessWordTask guessWordTask = new GuessWordTask();
            guessWordTask.execute(letter);
        }
    }

//    @OnClick(R.id.guess)
//    public void guess() {
//        if (hangManGame == null) {
//            Toast.makeText(this, getString(R.string.wait_for_the_game_init), Toast.LENGTH_LONG).show();
//        }
//        GuessWordTask guessWordTask = new GuessWordTask();
//        String guessWord = guessLetterET.getText().toString().toUpperCase();
//        guessWordTask.execute(guessWord);
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menuStartGame) {
            startNewGame();
            return true;
        } else if (id == R.id.menuNextWord) {
            nextWord();
            return true;
        } else if (id == R.id.menuGetMyScore) {
            getScore();
            return true;
        } else if (id == R.id.menuSubmitScore) {
            submitScore();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * task to request a new game
     * usage: startGameTask.execute(HangManGame.PLAYER_ID);
     */
    public class StartGameTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String result = null;
            try {
                String playId = params[0];
                JSONObject requestParams = new JSONObject();
                requestParams.put("playerId", playId);
                requestParams.put("action", "startGame");
                result = HttpUtils.callInHTTPPost(HangManGame.REQUEST_URL, requestParams);
                if (result != null) {
                    hangManGame = HangManGame.instance(result);
                    hangManGame.requestNewWord();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null)
                Toast.makeText(MainActivity.this, getString(R.string.server_error_message), Toast.LENGTH_LONG).show();
            nummberOfGuessAllowedForEachWordTv.setText(String.valueOf(hangManGame.NUMBER_OF_GUESS_ALLOWED_FOR_EACH_WORD));
            currentWordTV.setText(hangManGame.getWord());
            wrongGuessCountOfCurrentWordTV.setText(String.valueOf(hangManGame.getWrongGuessCountOfCurrentWord()));
        }
    }

    /**
     * request a new word
     * post word to server ,and show the result
     */
    public class NextWordTask extends AsyncTask<String, Void, String> {
        boolean isHitMaxWordCount = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hangManGame.clearWord();
            cleanup();
        }

        @Override
        protected String doInBackground(String... params) {
            isHitMaxWordCount = hangManGame.isHitMaxWordCount();
            if (isHitMaxWordCount) {
                return null;
            }
            boolean isRequestOK = hangManGame.requestNewWord();
            if (isRequestOK)
                return hangManGame.getWord();
            else
                return null;
        }

        @Override
        protected void onPostExecute(String word) {
            super.onPostExecute(word);
            if (isHitMaxWordCount)
                Toast.makeText(MainActivity.this, getString(R.string.hit_max_word_count), Toast.LENGTH_LONG).show();
            if (word == null)
                Toast.makeText(MainActivity.this, getString(R.string.server_error_message), Toast.LENGTH_LONG).show();
            else
                currentWordTV.setText(word);
        }
    }

    /**
     * get score
     */
    public class GetScoreTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {
            return hangManGame.requestScore();
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response == null) {
                Toast.makeText(MainActivity.this, getString(R.string.server_error_message), Toast.LENGTH_LONG).show();
                Log.e(TAG, getString(R.string.server_error_message));
            } else {
                ((View)scoreTV.getParent()).setVisibility(View.VISIBLE);
                scoreTV.setText(String.valueOf(hangManGame.getScore()));
            }
        }
    }

    /**
     * submit score
     */
    public class SubmitScoreTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {
            String message = hangManGame.submitScore();
            return message;
        }

        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        }
    }

    //cleanup the UI, after call the nextWord()
    private void cleanup() {
        ((View)scoreTV.getParent()).setVisibility(View.INVISIBLE);
        currentWordTV.setText("");
        wrongGuessCountOfCurrentWordTV.setText("");
        imageView.setImageResource(R.mipmap.g0);

        int childCount = guessLettersLayout.getChildCount();
        for(int i =0; i < childCount; i++){
            View v = guessLettersLayout.getChildAt(i);
            if(v instanceof ViewGroup){
                for(int j=0; j< ((ViewGroup)v).getChildCount();j++){
                    View letterView = ((ViewGroup)v).getChildAt(j);
                    if(letterView instanceof TextView){
                        TextView textView = (TextView) letterView;
                        textView.setEnabled(true);
                        textView.setText(new SpannableString(textView.getText().toString()));
                    }
                }
            }
        }
    }


    /**
     * play the game
     * post word to server ,and show the result
     */
    public class GuessWordTask extends AsyncTask<String, Void, String> {
        boolean isCurrentHit = false;
        boolean isCurrentFailed = false;
        boolean isInputInvalid = false;
        boolean isServerError = false;
        String lastWord;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String guessChar = params[0];
            boolean isValidated = HangManGame.validateWord(guessChar);
            isInputInvalid = !isValidated;
            if (isValidated) {
                if (hangManGame.guessWord(guessChar)) {
                    isCurrentFailed = hangManGame.isCurrentWordFailed();
                    isCurrentFailed = hangManGame.isCurrentWordFailed();
                    isCurrentHit = hangManGame.isCurrentWordHit();
                    if (isCurrentFailed || isCurrentHit) {
                        lastWord = hangManGame.getWord();
                    }
                } else {
                    isServerError = true;
                }
            }
            return null;
        }



        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (isServerError) {
                Toast.makeText(MainActivity.this, getString(R.string.server_error_message), Toast.LENGTH_LONG).show();
                Log.i(TAG, getString(R.string.server_error_message));
            } else if (isInputInvalid) {
                Toast.makeText(MainActivity.this, "only CAPITAL letter is permitted", Toast.LENGTH_LONG).show();
                Log.i(TAG, "only CAPITAL letter is permitted" + currentWordTV);
            } else{
                currentWordTV.setText(hangManGame.getWord());
                wrongGuessCountOfCurrentWordTV.setText(String.valueOf(hangManGame.getWrongGuessCountOfCurrentWord()));
                updateImageByWrongCount(hangManGame.getWrongGuessCountOfCurrentWord());
                if (isCurrentFailed) {
                    Toast.makeText(MainActivity.this, "word " + lastWord + "guess wrong", Toast.LENGTH_LONG).show();
                    Log.i(TAG, "word " + lastWord + "guess wrong");
                } else if (isCurrentHit) {
                    Toast.makeText(MainActivity.this, "word " + lastWord + " hit", Toast.LENGTH_LONG).show();
                    Log.i(TAG, "word " + lastWord + " hit");
                }
            }
        }

        /**
         * updateImageByWrongCount
         *
         * @param wrongCount
         */
        void updateImageByWrongCount(int wrongCount) {
            switch (wrongCount) {
                case 0:
                    imageView.setImageResource(R.mipmap.g0);
                    break;
                case 1:
                    imageView.setImageResource(R.mipmap.g1);
                    break;
                case 2:
                    imageView.setImageResource(R.mipmap.g2);
                    break;
                case 3:
                    imageView.setImageResource(R.mipmap.g3);
                    break;
                case 4:
                    imageView.setImageResource(R.mipmap.g4);
                    break;
                case 5:
                    imageView.setImageResource(R.mipmap.g5);
                    break;
                case 6:
                    imageView.setImageResource(R.mipmap.g6);
                    break;
                case 7:
                    imageView.setImageResource(R.mipmap.g7);
                    break;
                case 8:
                    imageView.setImageResource(R.mipmap.g8);
                    break;
                case 9:
                    imageView.setImageResource(R.mipmap.g9);
                    break;
                case 10:
                    imageView.setImageResource(R.mipmap.g10);
                    break;
                default:
                    //error
            }
        }
    }
}
