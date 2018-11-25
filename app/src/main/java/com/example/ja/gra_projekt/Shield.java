package com.example.ja.gra_projekt;

import android.graphics.RectF;
import android.util.Log;

public class Shield {
    boolean isAlive; //czy tarcza jest widoczna (czy nie została trafiona)
    RectF bounds; //granice do wykrywania kolizji

    public final static int HORIZONTAL_RATIO = 110; //współczynnik rozmiaru poziomego tarczy w stosunku do ekranu
    public final static int VERTICAL_RATIO = 40; //współczynnik rozmiaru pionowego tarczy w stosunku do ekranu

    public Shield(int rowNumber, int columnNumber, int blockNumber, int displayWidth, int displayHeight, int shieldsBlocks) {
        //inicjalizacja
        int width = displayWidth / HORIZONTAL_RATIO;
        int height = displayHeight / VERTICAL_RATIO;
        isAlive = true;

        //odstępy pomiędzy blokami tarcz
        int spaceBetweenBlocks = displayWidth / (shieldsBlocks * 2 + 1);
        //górna granica bloku
        int shieldBlockTopPosition = displayHeight - (displayHeight / 4);

        //granice dla pojedynczych tarcz
            //pierwszy odstęp + liczba odstępów między blokami + liczba bloków przed + szerokość poprzedzających tarcz
        int leftBound = spaceBetweenBlocks + spaceBetweenBlocks * blockNumber * 2 + columnNumber * width;
        int topBound =  shieldBlockTopPosition + rowNumber * height;
        int rightBound = spaceBetweenBlocks + spaceBetweenBlocks * blockNumber * 2 + columnNumber * width + width;
        int bottomBound = shieldBlockTopPosition + rowNumber * height + height;
        bounds = new RectF(leftBound, topBound, rightBound, bottomBound);
    }

    //gettery i settery
    public boolean isAlive() {
        return isAlive;
    }

    public RectF getBounds() {
        return bounds;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }
}
