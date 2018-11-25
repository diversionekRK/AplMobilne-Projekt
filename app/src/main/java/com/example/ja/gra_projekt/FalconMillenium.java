package com.example.ja.gra_projekt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

public class FalconMillenium {
    Bitmap falconBitmap; //bitmapa z obrazem statku
    float width; //szerokość pocisku
    float height; //wysokość (długość)
    float xPosition; //położenie w osi X
    float yPosition; //położenie w osi Y
    float speed; //szybkość
    RectF bounds; //granice do wykrywania kolizji
    int shipDirection; //kierunek
    int displayWidth; //szerokość ekranu

    public final static int DIRECTION_LEFT = 100; //kierunek lewo
    public final static int DIRECTION_RIGHT = 200; //kierunek prawo
    public final static int DIRECTION_NONE = 300; //brak ruchu
    public final static int WIDTH_RATIO = 10; //współczynnik do wyznaczania szerokości statku gracza
    public final static int HEIGHT_RATIO = 10; //współczynnik do wyznaczania wysokości statku gracza

    public FalconMillenium(int displayWidth, int displayHeight, Context context) {
        //inicjalizacja pól
        width = displayWidth / WIDTH_RATIO;
        height = displayHeight / HEIGHT_RATIO;
        this.displayWidth = displayWidth;
        xPosition = displayWidth / 2 - width / 2;
        yPosition = displayHeight - height;
        speed = displayWidth / 2;
        shipDirection = DIRECTION_NONE;
        bounds = new RectF();
        bounds.top = yPosition;
        bounds.bottom = yPosition + height;

        falconBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.falconmillenium);
        falconBitmap = Bitmap.createScaledBitmap(falconBitmap, (int)width, (int)height, false);
    }

    //uaktualnienie pozycji statku
    public void updatePosition(int framerPerSecond) {
        switch (shipDirection) {
            case DIRECTION_LEFT: //jeśli przesuwa się w lewo
                if(xPosition - speed / framerPerSecond > 0)
                    xPosition -= speed / framerPerSecond;
                break;
            case DIRECTION_RIGHT: //jeśli przesuwa się w prawo
                if(xPosition + width + speed / framerPerSecond <= displayWidth)
                    xPosition += speed / framerPerSecond;
                break;
        }

        bounds.left = xPosition; //aktualizacja granic
        bounds.right = xPosition + width;
    }

    //gettery i settery
    public RectF getBounds(){
        return bounds;
    }

    public Bitmap getFalconBitmap(){
        return falconBitmap;
    }

    public float getXPosition(){
        return xPosition;
    }

    public float getYPosition(){
        return yPosition;
    }

    public float getHeight() {
        return height;
    }

    public float getWidth(){
        return width;
    }

    public float getSpeed() {
        return speed;
    }

    public int getShipDirection() {
        return shipDirection;
    }

    public void setDirection(int direction){
        shipDirection = direction;
    }
}
