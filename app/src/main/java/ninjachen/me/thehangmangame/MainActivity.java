package ninjachen.me.thehangmangame;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
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
    TextView score;

    @InjectView(R.id.currentWord)
    TextView currentWord;

    @InjectView(R.id.guessLetter)
    EditText guessLetter;

    @InjectView(R.id.start)
    Button start;

    @InjectView(R.id.guess)
    Button guess;

    HangManGame hangManGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        StartGameTask startGameTask = new StartGameTask();
        startGameTask.execute(HangManGame.PLAYER_ID);
    }

    @OnClick(R.id.guess)
    public void guess() {
        if (hangManGame == null) {
            Toast.makeText(this, "服务器还没准备妥当，请稍等片刻。", Toast.LENGTH_LONG).show();
        }
        GuessWordTask guessWordTask = new GuessWordTask();
        //todo
        String guessWord = "";
        guessWordTask.execute(guessWord);
    }


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
        if (id == R.id.action_settings) {
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
            try {
                String playId = params[0];
                JSONObject requestParams = new JSONObject();
                requestParams.put("playerId", playId);
                requestParams.put("action", "startGame");

                String result = HttpUtils.callInHTTPPost(HangManGame.REQUEST_URL, requestParams);
                if (result == null) {
                    Toast.makeText(MainActivity.this, getString(R.string.server_error_message), Toast.LENGTH_LONG).show();
                } else {
                    hangManGame = HangManGame.instance(result);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
            return null;
        }
    }

    /**
     * request a new word
     * post word to server ,and show the result
     */
    public class NextWordTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String guessWord = params[0];
            boolean isRequestOK = hangManGame.requestNewWord();
            if(isRequestOK)
                return hangManGame.getWord();
            else
                return null;
        }

        @Override
        protected void onPostExecute(String word) {
            super.onPostExecute(word);
            if(word == null)
                Toast.makeText(MainActivity.this, getString(R.string.server_error_message), Toast.LENGTH_LONG).show();
            else
                currentWord.setText(word);
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
        String lastWord;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String guessChar = params[0];
            //todo validate the word
            boolean isValidated = HangManGame.validateWord(guessChar);
            isInputInvalid = !isValidated;
            if (isValidated) {
                hangManGame.guessWord(guessChar);
                isCurrentFailed = hangManGame.isCurrentWordFailed();
                isCurrentHit = hangManGame.isCurrentWordHit();
                if(isCurrentFailed || isCurrentHit){
                    lastWord = hangManGame.getWord();
                    hangManGame.requestNewWord();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(isInputInvalid){
                Toast.makeText(MainActivity.this, "only CAPITAL letter is permitted", Toast.LENGTH_LONG).show();
                Log.i(TAG, "only CAPITAL letter is permitted" + currentWord);
            }else if(isCurrentFailed){
                hangManGame.requestNewWord();
                Toast.makeText(MainActivity.this, "word " + lastWord + "guess wrong", Toast.LENGTH_LONG).show();
                Log.i(TAG,"word " + lastWord + "guess wrong");
            }else if(isCurrentHit){
                Toast.makeText(MainActivity.this, "word " + lastWord + " hit", Toast.LENGTH_LONG).show();
                Log.i(TAG, "word " + lastWord + " hit");
            }
        }
    }
}
