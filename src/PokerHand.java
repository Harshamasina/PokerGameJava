import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PokerHand {
    private List<Card> hand;
    private List<Card> keepers;
    private List<Card> discards;
    private Ranking score = Ranking.NONE;
    private int playerNo;

    // Track total games and fractional win points
    private static int totalGames = 0;
    private static Map<Integer, Double> winPoints = new HashMap<>();

    public PokerHand(List<Card> hand, int playerNo) {
        hand.sort(Card.sortReversedSuit());
        this.hand = hand;
        this.playerNo = playerNo;
        this.keepers = new ArrayList<>(hand.size());
        this.discards = new ArrayList<>(hand.size());
    }

    /**
     * Register a completed round of games, awarding fractional wins on ties.
     */
    public static void registerGame(List<PokerHand> hands) {
        totalGames++;
        // Determine highest ranking
        int bestOrdinal = hands.stream()
                .mapToInt(h -> h.score.ordinal())
                .max().orElse(Ranking.NONE.ordinal());
        // Collect all winners (could be ties)
        List<PokerHand> winners = hands.stream()
                .filter(h -> h.score.ordinal() == bestOrdinal)
                .collect(Collectors.toList());
        // Award each winner fractional win = 1 / number of winners
        double share = 1.0 / winners.size();
        for (PokerHand w : winners) {
            winPoints.merge(w.playerNo, share, Double::sum);
        }
    }

    /**
     * @return winning percentage (0-100) for this player over all games
     */
    public static double getWinningPercentage(int pNo) {
        return totalGames > 0
                ? (winPoints.getOrDefault(pNo, 0.0) / totalGames) * 100.0
                : 0.0;
    }

    public Ranking getScore() { return score; }
    public int getPlayerNo() { return playerNo; }

    @Override
    public String toString() {
        Card best = Collections.max(hand, Comparator.comparingInt(Card::rank));
        Card worst = Collections.min(hand, Comparator.comparingInt(Card::rank));
        String disc = discards.isEmpty() ? "" : " Discards: " + discards;
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
        keepers.clear();
        discards.clear();
        score = Ranking.NONE;

        // Check for flush
        boolean isFlush = hand.stream().map(Card::suit).distinct().count() == 1;
        // Check for straight
        List<Integer> ranks = hand.stream().map(Card::rank).sorted().collect(Collectors.toList());
        boolean isStraight = IntStream.range(0, ranks.size()-1)
                .allMatch(i -> ranks.get(i+1) - ranks.get(i) == 1);

        // Straight flush / royal flush
        if (isStraight && isFlush) {
            score = (ranks.get(0) == 8) ? Ranking.ROYAL_FLUSH : Ranking.STRAIGHT_FLUSH;
            keepers.addAll(hand);
        }
        // Flush only
        else if (isFlush) {
            score = Ranking.FLUSH;
            keepers.addAll(hand);
        }
        // Straight only
        else if (isStraight) {
            score = Ranking.STRAIGHT;
            keepers.addAll(hand);
        }
        // Other combinations
        else {
            Map<String, List<Card>> groups = hand.stream()
                    .collect(Collectors.groupingBy(Card::face));
            // Evaluate pairs, trips, quads
            groups.values().stream()
                    .filter(g -> g.size() > 1)
                    .sorted((a, b) -> b.size() - a.size())
                    .forEach(g -> {
                        setRank(g.size());
                        keepers.addAll(g);
                    });
            // High card fallback
            if (score == Ranking.NONE) {
                score = Ranking.HIGH_CARD;
                keepers.add(Collections.max(hand, Comparator.comparingInt(Card::rank)));
            }
        }

        pickDiscards();
    }

    private void setRank(int cnt) {
        switch (cnt) {
            case 4 -> score = Ranking.FOUR_OF_A_KIND;
            case 3 -> score = (score == Ranking.ONE_PAIR) ? Ranking.FULL_HOUSE : Ranking.THREE_OF_A_KIND;
            case 2 -> score = (score == Ranking.ONE_PAIR) ? Ranking.TWO_PAIR : Ranking.ONE_PAIR;
        }
    }

    private void pickDiscards() {
        List<Card> temp = new ArrayList<>(hand);
        temp.removeAll(keepers);
        Collections.reverse(temp);
        for (Card c : temp) {
            if (discards.size() < 3 && (keepers.size() > 2 || c.rank() < 9)) {
                discards.add(c);
            }
        }
    }
}