package zhengr.memorytiles;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

class Player implements Parcelable {
    private String name;
    private int score;

    Player(String name, int score) {
        this.name = name;
        this.score = score;
    }

    Player(Parcel in) {
        String buffer = in.readString();
        name = buffer.substring(0, buffer.indexOf(" "));
        score = Integer.parseInt(buffer.substring(buffer.indexOf(" ") + 1));
    }

    public int getScore() {
        return score;
    }

    public String getName() {
        return name;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static final Parcelable.Creator<Player> CREATOR =
            new Parcelable.Creator<Player>(){
                public Player createFromParcel(Parcel in) {
                    return new Player(in);
                }
                public Player[] newArray(int size) {
                    return new Player[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name + " " + score);
    }
}

