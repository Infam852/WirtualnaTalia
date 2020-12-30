package com.example.wirtualnatalia.common.cards;

import java.util.ArrayList;

public class Board {
    public static final String TAG = "CardsHelper";

    private ArrayList<Card> cards;  // list of cards on board

    public static final int MAX_CARDS = 5;

    public Board(){
        cards = new ArrayList<>();
    }

    public Board(ArrayList<Card> cardsOnBoard){
        this.cards = cardsOnBoard;
    }

    public void addCard(Card card){
        if (cards.size() < MAX_CARDS){
            cards.add(card);
        }
    }

    public void removeCard(int idx){
        if (idx < cards.size()){
            cards.remove(idx);
        }
    }

    public void mergeCards(ArrayList<Card> cards){
        for(Card card: cards){
            if (cardNotPresent(card.getId())){
                this.cards.add(card);
            }
        }
    }

    private boolean cardNotPresent(String id){
        for(Card card: this.cards) {
            if (card.getId().equals(id)){
                return false;
            }
        }
        return true;
    }

    public ArrayList<Card> getCards(){
        return cards;
    }

    public int getNumberOfCards(){
        return cards.size();
    }
}
