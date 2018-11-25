package com.example.ja.gra_projekt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

import java.util.Random;

public class ImperialShip {
    Bitmap imperialBitmap; //bitmapa z obrazem statku
    float width; //szerokość pocisku
    float height; //wysokość (długość)
    float xPosition; //położenie w osi X
    float yPosition; //położenie w osi Y
    float speed; //szybkość
    int direction; //kierunek
    boolean isAlive; //czy widoczny (nie został trafiony)
    int displayHeight; //wysokość ekranu
    RectF bounds; //granice do wykrywania kolizji
    Random randomGenerator;

    public final static int DIRECTION_LEFT = 100; //kierunek lewo
    public final static int DIRECTION_RIGHT = 200; //kierunek prawo
    public final static int IMPERIAL_SPEED = 45; //początkowa szybkość wrogów
    public final static float ACCELERATION_FACTOR = 1.15f; //współczynnik przyspieszenia
    public final static int SPACE_RATIO = 25; //współczynnik do wyznaczania odległości między statkami
    public final static int WIDTH_RATIO = 25; //współczynnik do wyznaczania szerokości wroga
    public final static int HEIGHT_RATIO = 25; //współczynnik do wyznaczania wysokości wroga

    public ImperialShip(int displayWidth, int displayHeight, Context context, int rowNumber, int columnNumber) {
        //inicjalizacja pól
        bounds = new RectF();
        width = displayWidth / WIDTH_RATIO;
        height = displayHeight / HEIGHT_RATIO;
        isAlive = true;
        direction = DIRECTION_RIGHT;
        randomGenerator = new Random();
        speed = IMPERIAL_SPEED;
        xPosition = columnNumber * (width + displayWidth / SPACE_RATIO);
        yPosition = rowNumber * (height + displayHeight / SPACE_RATIO);

        //wczytanie bitmapy dla statku wroga
        imperialBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.imperial);
        imperialBitmap = Bitmap.createScaledBitmap(imperialBitmap, (int) (width), (int) (height), false);
    }

    //uaktualnienie pozycji wroga
    public void updatePosition(int framesPerSecond){
        switch (direction) {
            case DIRECTION_LEFT: //jeśli przesuwa się w lewo
                xPosition -= speed / framesPerSecond;
                break;
            case DIRECTION_RIGHT: //jeśli przesuwa się w prawo
                xPosition += speed / framesPerSecond;
                break;
        }

        bounds.top = yPosition; //aktualizacja granic
        bounds.bottom = yPosition + height;
        bounds.left = xPosition;
        bounds.right = xPosition + width;
    }

    //zmiana kierunku ruchu po dotknięciu krawędzi
    public void changeDirection(){
        switch (direction) {
            case DIRECTION_LEFT:
                direction = DIRECTION_RIGHT;
                break;
            case DIRECTION_RIGHT:
                direction = DIRECTION_LEFT;
                break;
        }

        //przesunięcie w dół
        yPosition += height;

        //przyspieszenie ruchu
        speed *= ACCELERATION_FACTOR;
    }

    //sprawdzenie, czy oddać strzał
    public boolean isGoingToFire(float falconXPosition, float falconWidth){
        //jeśli gracz w linii wroga - większa szansa
        if(
                (falconXPosition + falconWidth > xPosition && falconXPosition + falconWidth < xPosition + width) ||
                (falconXPosition > xPosition && falconXPosition < xPosition + width)) {
            if(randomGenerator.nextInt(200) == 100) {
                return true;
            }
        }
        //jeśli gracz nie znajduje się w linii wroga - mniejsza szansa na przypadkowy strzał
        if(randomGenerator.nextInt(2000) == 1000){
            return true;
        }

        return false;
    }

    //gettery i settery
    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getXPosition() {
        return xPosition;
    }

    public float getYPosition() {
        return yPosition;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public RectF getBounds() {
        return bounds;
    }

    public Bitmap getImperialBitmap() {
        return imperialBitmap;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

}
