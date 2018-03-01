package zhengr.memorytiles;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaderBoardActivity extends AppCompatActivity {
    ArrayList<Player> arrayList = new ArrayList<Player>();
    private CustomAdapter adapter;
    private SharedPreferences settings;
    private Context c = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view_layout);

        ArrayList<Player> arrayList = loadArray();

        RecyclerView listView = (RecyclerView) findViewById(R.id.list_view);

        adapter = new CustomAdapter(arrayList);
        RecyclerView.LayoutManager linearLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        listView.setAdapter(adapter);
        listView.setLayoutManager(linearLayoutManager);

    }

    private ArrayList<Player> loadArray() {
        // create new array
        ArrayList<Player> arrayList = new ArrayList<>();


        settings = getSharedPreferences(getString(R.string.leaderboard),
                Context.MODE_PRIVATE);

        // retrieve all maps in a sorted tree map
        TreeMap<String, ?> map = new TreeMap<String, Object>(settings.getAll());

        // loop through map and add to arrayList
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            arrayList.add(new Player(entry.getKey(),
                    Integer.parseInt(entry.getValue().toString())));
        }

        // sort array list by ascending value
        Collections.sort(arrayList, new CustomComparator());
        // reverse the list so that the array is sorted by descending value
        Collections.reverse(arrayList);

        return arrayList;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.leader_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        final Context c = this;

        if (item.getItemId() == R.id.clear_menu) {
            AlertDialog.Builder builder = new AlertDialog.Builder(c);
            builder.setTitle(getString(R.string.message)).setMessage(
                    getString(R.string.leaderclearconfirm))
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = settings.edit();
                            editor.clear();
                            editor.apply();

                            Toast.makeText(c, getString(R.string.leadercleared), Toast.LENGTH_LONG)
                                    .show();
                            adapter.updateList(loadArray());
                            adapter.notifyDataSetChanged();
                        }})
                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(c, getString(R.string.leadernotclear), Toast.LENGTH_SHORT).show();
                        }}).show();
        }

        if (item.getItemId() == R.id.upload) {
            //upload data
            for (Player p: loadArray()) {
                final String name = p.getName();
                final int score = p.getScore();

                // check if current score is higher than global's score
                RedisService.Service service = RedisService.getService();
                service.getScore(name).enqueue(new Callback<RedisService.GetResponse>() {
                    @Override
                    public void onResponse(Call<RedisService.GetResponse> call, Response<RedisService.GetResponse> response) {
                        int onlineScore = response.body().request.score;
                        // If online score is lower than current score, update it
                        if (onlineScore < score) {
                            RedisService.Service service = RedisService.getService();
                            service.setScore(name, new RedisService.SetScoreRequest(score)).enqueue(new Callback<RedisService.SetResponse>() {
                                @Override
                                public void onResponse(Call<RedisService.SetResponse> call, Response<RedisService.SetResponse> response) {

                                }

                                @Override
                                public void onFailure(Call<RedisService.SetResponse> call, Throwable t) {
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<RedisService.GetResponse> call, Throwable t) {
                        // if name does not already exist, upload new entry
                        RedisService.Service service = RedisService.getService();
                        service.setScore(name, new RedisService.SetScoreRequest(score)).enqueue(new Callback<RedisService.SetResponse>() {
                            @Override
                            public void onResponse(Call<RedisService.SetResponse> call, Response<RedisService.SetResponse> response) {

                            }

                            @Override
                            public void onFailure(Call<RedisService.SetResponse> call, Throwable t) {
                            }
                        });
                    }
                });
            }
            Toast.makeText(c, getString(R.string.uploaded), Toast.LENGTH_LONG).show();
        }

        if (item.getItemId() == R.id.download) {
            AlertDialog.Builder builder = new AlertDialog.Builder(c);
            builder.setTitle(getString(R.string.message)).setMessage(
                    getString(R.string.downloadconfirm))
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = settings.edit();
                            //clear current data
                            editor.clear();
                            editor.apply();
                            downloadData();
                            arrayList = loadArray();
                            Toast.makeText(c, getString(R.string.downloaded), Toast.LENGTH_LONG)
                                    .show();
                        }})
                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(c, getString(R.string.notdownloaded), Toast.LENGTH_SHORT).show();
                        }}).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void downloadData() {
        final Context c = this;
        RedisService.Service service = RedisService.getService();
        service.allKeys("").enqueue(new Callback<RedisService.KeysResponse>() {
            @Override
            public void onResponse(Call<RedisService.KeysResponse> call, Response<RedisService.KeysResponse> response) {
                ArrayList<String> keys = response.body().keys;

                for (final String player : keys) {
                    RedisService.Service service = RedisService.getService();
                    service.getScore(player).enqueue(new Callback<RedisService.GetResponse>() {
                        @Override
                        public void onResponse(Call<RedisService.GetResponse> call, Response<RedisService.GetResponse> response) {
                            int score = response.body().request.score;
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putInt(player, score);
                            editor.apply();
                            if (!arrayList.contains(player)) {
                                arrayList.add(new Player(player, score));
                                Collections.sort(arrayList, new CustomComparator());
                                // reverse the list so that the array is sorted by descending value
                                Collections.reverse(arrayList);
                                adapter.updateList(arrayList);
                                adapter.notifyDataSetChanged();
                            }
                            //deleteEntry(player);
                        }

                        @Override
                        public void onFailure(Call<RedisService.GetResponse> call, Throwable t) {
                            Toast.makeText(c, "Failed!", Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }

            @Override
            public void onFailure(Call<RedisService.KeysResponse> call, Throwable t) {
            }
        });
    }

    // method to clear global entries, this is for internal use only!
    private void deleteEntry(String key) {
        RedisService.Service service = RedisService.getService();
        service.deletePost(key).enqueue(new Callback<RedisService.DelResponse>() {
            @Override
            public void onResponse(Call<RedisService.DelResponse> call, Response<RedisService.DelResponse> response) {
            }

            @Override
            public void onFailure(Call<RedisService.DelResponse> call, Throwable t) {
                Toast.makeText(c, "Failed!", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
