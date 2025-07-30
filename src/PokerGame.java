import java.util.*;

public class PokerGame {
    private final List<Card> deck = Card.getStandardDeck();
    private int playerCount;
    private int cardsInHand;
    private List<PokerHand> pokerHands;
    private List<Card> remainingCards;

    public PokerGame(int playerCount, int cardsInHand) {
        this.playerCount = playerCount;
        this.cardsInHand = cardsInHand;
        pokerHands = new ArrayList<>(playerCount);
    }

    public void startPlay() {
        Collections.shuffle(deck);
        Card.printDeck(deck);
        int rnd = new Random().nextInt(15,35);
        Collections.rotate(deck, rnd);
        Card.printDeck(deck);

        deal();
        System.out.println("---------------------");
        pokerHands.forEach(PokerHand::evalHand);
        PokerHand.registerGame(pokerHands);
        pokerHands.forEach(System.out::println);

        int dealt = playerCount*cardsInHand;
        remainingCards = new ArrayList<>(deck.subList(dealt, deck.size()));
        Card.printDeck(remainingCards, "Remaining Cards", 2);
    }

    private void deal() {
        for (int p=1; p<=playerCount; p++) {
            List<Card> hand = new ArrayList<>();
            for (int i=0; i<cardsInHand; i++) {
                hand.add(deck.remove(0));
            }
            pokerHands.add(new PokerHand(hand, p));
        }
    }
}