package zhengr.memorytiles;

import java.util.Comparator;

public class CustomComparator implements Comparator<Player> {
    @Override
    public int compare(Player p1, Player p2) {
        Integer p1Score = p1.getScore();
        Integer p2Score = p2.getScore();
        return p1Score.compareTo(p2Score);
    }
}
