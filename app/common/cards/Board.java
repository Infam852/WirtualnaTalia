package com.google.ar.core.examples.java.common.cards;

import com.google.ar.core.examples.java.common.cards.Card;

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

    public ArrayList<Card> getCards(){
        return cards;
    }

    public int getNumberOfCards(){
        return cards.size();
    }
}
