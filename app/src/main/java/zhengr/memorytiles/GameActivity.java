package zhengr.memorytiles;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

import static java.sql.Types.NULL;

public class GameActivity extends AppCompatActivity {

    @BindView(R.id.player_score)
    TextView playerscore;
    @BindView(R.id.player_name)
    TextView playername;
    @BindView(R.id.timer)
    TextView timer;
    @BindViews({ R.id.b0, R.id.b1 , R.id.b2, R.id.b3,
            R.id.b4, R.id.b5, R.id.b6, R.id.b7,
            R.id.b8, R.id.b9, R.id.b10, R.id.b11})
    List<ImageButton> buttonViews;



    private Player player;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private boolean gameOver;
    private CountDownTimer c;
    private int imgRes[] = {R.drawable.ic_0, R.drawable.ic_1, R.drawable.ic_2, R.drawable.ic_3,
            R.drawable.ic_4, R.drawable.ic_5, R.drawable.ic_6,};

    // associates a button with a specific image
    private Map<ImageButton, Integer> map;
    private Map<ImageButton, ImageButton> pair;
    private boolean pressed[];
    private boolean finished[];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        ButterKnife.bind(this);
        settings = getSharedPreferences(getString(R.string.leaderboard),
                Context.MODE_PRIVATE);
        editor = settings.edit();

        player = (Player) getIntent().getParcelableExtra(getString(R.string.player));
        resetGame();
    }

    private void resetGame() {
        player.setScore(0);
        gameOver = false;
        resetBoard();
        displayScore();
        saveScore();
        if (c != null) {
            c.cancel();
        }
        c = new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                timer.setText("Seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                timer.setText("GameOver!");
                gameOver = true;
                saveScore();
            }
        }.start();
    }

    private void resetBoard() {
        map = new HashMap<ImageButton, Integer>();
        pair = new HashMap<ImageButton, ImageButton>();
        pressed = new boolean[12];
        finished = new boolean[12];
        for (int i = 0; i < 6; i++) {
            // assign i to two random buttons
            Random randomizer = new Random();
            ImageButton button1 = buttonViews.get(randomizer.nextInt(buttonViews.size()));
            ImageButton button2 = buttonViews.get(randomizer.nextInt(buttonViews.size()));

            // if button1 or 2 are already assigned to an int, generate the next random pair
            while (map.containsKey(button1)) {
                button1 = buttonViews.get(randomizer.nextInt(buttonViews.size()));
            }
            button1.setTag(i);
            map.put(button1, i);

            while (map.containsKey(button2)) {
                button2 = buttonViews.get(randomizer.nextInt(buttonViews.size()));
            }

            button2.setTag(i);
            map.put(button2, i);

            pressed[buttonViews.indexOf(button1)] = false;
            pressed[buttonViews.indexOf(button2)] = false;
            finished[buttonViews.indexOf(button1)] = false;
            finished[buttonViews.indexOf(button2)] = false;

            pair.put(button1, button2);
            pair.put(button2, button1);
            hideImage(button1);
            hideImage(button2);
        }
    }

    private void checkButton(ImageButton b) {
        ImageButton pairedButton = pair.get(b);
        // if the paired button is pressed and this button was not already paired, add score
        if (pressed[buttonViews.indexOf(pairedButton)] && !finished[buttonViews.indexOf(b)]) {
            b.setImageResource(imgRes[map.get(b)]);
            player.setScore(player.getScore() + 1);
            displayScore();
            finished[buttonViews.indexOf(pairedButton)] = true;
            finished[buttonViews.indexOf(b)] = true;

            //if player score is a multiple of 6 then the player has cleared the board
            if (player.getScore() > 0 && player.getScore() % 6 == 0) {
                resetBoard();
            }
        } else {
            pressed[buttonViews.indexOf(b)] = true;
            startImageTimer(b);
        }
    }

    private void startImageTimer(final ImageButton b) {
        showImage(b);
        new CountDownTimer(500, 500) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                hideImage(b);
            }
        }.start();
    }

    private void showImage(ImageButton b) {
        b.setImageResource(imgRes[map.get(b)]);
    }

    private void hideImage(ImageButton b) {
        // if the image was not paired by the user, hide it
        if (!finished[buttonViews.indexOf(b)]) {
            b.setImageResource(android.R.color.transparent);
            pressed[buttonViews.indexOf(b)] = false;
        }
    }

    public void buttonOnClick(final View view)
    {
        final ImageButton b = (ImageButton) view;
        if (!gameOver) {
            checkButton(b);
        }
    }

    private void displayScore() {
        playername.setText(getString(R.string.name_display) + player.getName());
        playerscore.setText(getString(R.string.score_display) + player.getScore());
    }



    private void saveScore() {
        int highscore = settings.getInt(player.getName(), NULL);
        editor.putInt(player.getName(), Math.max(highscore, player.getScore()));
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.game_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.new_game) {
            saveScore();
            resetGame();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        saveScore();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }



}