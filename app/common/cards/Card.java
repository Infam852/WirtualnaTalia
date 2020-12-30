package com.google.ar.core.examples.java.common.cards;

import android.content.Context;
import android.util.Log;

import com.google.ar.core.examples.java.common.rendering.ObjectRenderer;

import java.io.IOException;

public class Card {
    public static final String TAG = "Card";

    private int suit;
    private int symbol;

    public static int
        CARD_TEMPLATE = -1,
        CARD_1 = 1;

    public static int
        SUIT_TEMPLATE = -1;

    // card position
    public final float[] anchorMatrix = new float[16];
    private final ObjectRenderer virtualCard;
    private boolean toRender = false;

    public Card(Context context, int symbol, int suit, ObjectRenderer virtualCard){
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
}
