import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record Card(Suit suit, String face, int rank) {
    public enum Suit {
        CLUB, DIAMOND, HEART, SPADE;
        public char getImage() {
            return (new char[]{9827, 9830, 9829, 9824})[this.ordinal()];
        }
    }

    public static Comparator<Card> sortReversedSuit() {
        return Comparator.comparing(Card::rank).reversed().thenComparing(Card::suit);
    }

    @Override
    public String toString() {
        int idx = face.equals("10") ? 2 : 1;
        String f = face.substring(0, idx);
        return "%s%c(%d)".formatted(f, suit.getImage(), rank);
    }

    public static Card getNumericCard(Suit suit, int num) {
        if (num > 1 && num < 11) {
            return new Card(suit, String.valueOf(num), num - 2);
        }
        System.out.println("Invalid Numeric card selected");
        return null;
    }

    public static Card getFaceCard(Suit suit, char abbrev) {
        int idx = "JQKA".indexOf(abbrev);
        if (idx > -1) {
            return new Card(suit, String.valueOf(abbrev), idx + 9);
        }
        System.out.println("Invalid Face Card Selected");
        return null;
    }

    public static List<Card> getStandardDeck() {
        List<Card> deck = new ArrayList<>(52);
        for (Suit s : Suit.values()) {
            for (int i = 2; i <= 10; i++) deck.add(getNumericCard(s, i));
            for (char c : new char[]{'J','Q','K','A'}) deck.add(getFaceCard(s, c));
        }
        return deck;
    }

    public static void printDeck(List<Card> deck) {
        printDeck(deck, "Current Deck", 4);
    }
    public static void printDeck(List<Card> deck, String desc, int rows) {
        System.out.println("--------------------");
        if (desc != null) System.out.println(desc);
        int perRow = deck.size()/rows;
        for (int i = 0; i < rows; i++) {
            deck.subList(i*perRow, i*perRow + perRow).forEach(c -> System.out.print(c+" "));
            System.out.println();
        }
    }
}