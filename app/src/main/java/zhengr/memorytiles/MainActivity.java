package zhengr.memorytiles;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.player_name_et)
    EditText playername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.start_button)
    public void onClickStart() {
        startGame();
    }

    @OnClick(R.id.to_leaderboard)
    public void onClickLeaderBoard() {
        startLeaderBoard();
    }

    private void startLeaderBoard() {
        Intent intent = new Intent(MainActivity.this, LeaderBoardActivity.class);
        startActivity(intent);
        finish();
    }

    private void startGame() {
        Intent intent = new Intent(this, GameActivity.class);


        final Context c = this;

        // check if first player's name is entered
        if (playername.getText().toString().isEmpty()) {
            Toast.makeText(c, R.string.playerempty, Toast.LENGTH_LONG).show();
        }
        // check for white space
        else if (playername.getText().toString().indexOf(' ') >= 0) {
            Toast.makeText(c, R.string.playerspace, Toast.LENGTH_LONG).show();
        }
        // check for length
        else if (playername.getText().toString().length() > 14) {
            Toast.makeText(c, R.string.nameTooLong, Toast.LENGTH_LONG).show();
        }

        else {
            Player player = new Player(playername.getText().toString(), 0);
            intent.putExtra(getString(R.string.player), player);
            startActivity(intent);
            finish();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.leader_menu) {
            startLeaderBoard();
        }

        if (item.getItemId() == R.id.start_menu) {
            startGame();
        }
        return super.onOptionsItemSelected(item);
    }
}
