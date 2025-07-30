import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PokerHand {
    private List<Card> hand;
    private List<Card> keepers;
    private List<Card> discards;
    private Ranking score = Ranking.NONE;
    private int playerNo;

    private static int totalGames = 0;
    private static Map<Integer,Integer> winCounts = new HashMap<>();

    public PokerHand(List<Card> hand, int playerNo) {
        hand.sort(Card.sortReversedSuit());
        this.hand = hand;
        this.playerNo = playerNo;
        this.keepers = new ArrayList<>(hand.size());
        this.discards = new ArrayList<>(hand.size());
    }

    public static void registerGame(List<PokerHand> hands) {
        totalGames++;
        Ranking best = hands.stream()
                .map(PokerHand::getScore)
                .max(Comparator.comparingInt(Ranking::ordinal))
                .orElse(Ranking.NONE);
        hands.stream()
                .filter(h -> h.score == best)
                .forEach(h -> winCounts.merge(h.playerNo, 1, Integer::sum));
    }

    public static double getWinningPercentage(int pNo) {
        return totalGames>0 && winCounts.containsKey(pNo)
                ? winCounts.get(pNo)*100.0/totalGames
                : 0.0;
    }

    public Ranking getScore() { return score; }
    public int getPlayerNo() { return playerNo; }

    @Override
    public String toString() {
        Card best = Collections.max(hand, Comparator.comparingInt(Card::rank));
        Card worst = Collections.min(hand, Comparator.comparingInt(Card::rank));
        String disc = discards.isEmpty() ? "" : " Discards: "+discards;
        return String.format(
                "%d. %-15s Rank:%d Hand: %-40s Best:%s Worst:%s%s Win%%: %.2f%%",
                playerNo,
                score,
                score.ordinal(),
                hand,
                best,
                worst,
                disc,
                getWinningPercentage(playerNo)
        );
    }

    public void evalHand() {
        keepers.clear(); discards.clear(); score = Ranking.NONE;
        boolean isFlush = hand.stream().map(Card::suit).distinct().count()==1;
        List<Integer> ranks = hand.stream().map(Card::rank).sorted().collect(Collectors.toList());
        boolean isStraight = IntStream.range(0, ranks.size()-1)
                .allMatch(i -> ranks.get(i+1)-ranks.get(i)==1);

        if (isStraight && isFlush) {
            score = (ranks.get(0)==8) ? Ranking.ROYAL_FLUSH : Ranking.STRAIGHT_FLUSH;
            keepers.addAll(hand);
        } else if (isFlush) {
            score = Ranking.FLUSH; keepers.addAll(hand);
        } else if (isStraight) {
            score = Ranking.STRAIGHT; keepers.addAll(hand);
        } else {
            Map<String,List<Card>> groups = hand.stream().collect(Collectors.groupingBy(Card::face));
            groups.values().stream()
                    .filter(g -> g.size()>1)
                    .sorted((a,b)->b.size()-a.size())
                    .forEach(g -> { setRank(g.size()); keepers.addAll(g); });
            if (score==Ranking.NONE) {
                score = Ranking.HIGH_CARD;
                keepers.add(Collections.max(hand, Comparator.comparingInt(Card::rank)));
            }
        }
        pickDiscards();
    }

    private void setRank(int cnt) {
        switch(cnt) {
            case 4 -> score = Ranking.FOUR_OF_A_KIND;
            case 3 -> score = (score==Ranking.ONE_PAIR)?Ranking.FULL_HOUSE:Ranking.THREE_OF_A_KIND;
            case 2 -> score = (score==Ranking.ONE_PAIR)?Ranking.TWO_PAIR:Ranking.ONE_PAIR;
        }
    }

    private void pickDiscards() {
        List<Card> temp = new ArrayList<>(hand);
        temp.removeAll(keepers);
        Collections.reverse(temp);
        for (Card c : temp) {
            if (discards.size()<3 && (keepers.size()>2 || c.rank()<9)) {
                discards.add(c);
            }
        }
    }
}
