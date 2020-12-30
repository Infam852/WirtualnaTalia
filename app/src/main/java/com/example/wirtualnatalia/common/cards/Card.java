package com.example.wirtualnatalia.common.cards;

import android.content.Context;

import com.example.wirtualnatalia.common.rendering.ObjectRenderer;

public class Card {
    public static final String TAG = "Card";

    private String suit;
    private String symbol;

    public static String
        CARD_TEMPLATE = "-1",
        CARD_1 = "1";

    public static String
        SUIT_TEMPLATE = "template";

    // card position
    public final float[] anchorMatrix = new float[16];
    private ObjectRenderer virtualCard;
    private boolean toRender = false;

    public Card(String symbol, String suit){
        this.symbol = symbol;
        this.suit = suit;
    }

    public Card(Context context, String symbol, String suit, ObjectRenderer virtualCard){
        this.symbol = symbol;
        this.suit = suit;
        this.virtualCard = virtualCard;
    }

    public float[] getAnchorMatrix() {
        return anchorMatrix;
    }

    public ObjectRenderer getVirtualCard() {
        return virtualCard;
    }

    public void setToRender(boolean toRender) {
        this.toRender = toRender;
    }

    public boolean getToRender(){
        return toRender;
    }

    public String getSymbol(){ return symbol; }
    public String getSuit(){ return suit; }

}
