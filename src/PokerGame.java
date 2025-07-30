import java.util.*;

public class PokerGame {
    private final List<Card> deck = Card.getStandardDeck();
    private final int playerCount;
    private final int cardsInHand;
    private final List<PokerHand> pokerHands = new ArrayList<>();
    private List<Card> remainingCards;

    public PokerGame(int playerCount, int cardsInHand) {
        this.playerCount = playerCount;
        this.cardsInHand = cardsInHand;
    }

    public void startPlay() {
        Collections.shuffle(deck);
        Card.printDeck(deck);
        int randomMiddle = new Random().nextInt(15, 35);
        Collections.rotate(deck, randomMiddle);
        Card.printDeck(deck);

        deal();   // <-- no removals from deck
        System.out.println("---------------------");
        pokerHands.forEach(PokerHand::evalHand);
        PokerHand.registerGame(pokerHands);
        pokerHands.forEach(System.out::println);

        int dealtCards = playerCount * cardsInHand;
        remainingCards = deck.subList(dealtCards, deck.size());
        Card.printDeck(remainingCards, "Remaining Cards", 2);
    }

    private void deal() {
        // Prepare empty hands
        List<List<Card>> hands = new ArrayList<>(playerCount);
        for (int p = 0; p < playerCount; p++) {
            hands.add(new ArrayList<>(cardsInHand));
        }

        // Deal round-robin without removing from deck
        int idx = 0;
        for (int i = 0; i < cardsInHand; i++) {
            for (int p = 0; p < playerCount; p++) {
                hands.get(p).add(deck.get(idx++));
            }
        }

        // Wrap into PokerHand objects
        for (int p = 0; p < playerCount; p++) {
            pokerHands.add(new PokerHand(hands.get(p), p + 1));
        }
    }
}
