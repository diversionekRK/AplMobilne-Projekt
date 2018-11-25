package com.example.ja.gra_projekt;

import android.graphics.RectF;
import android.util.Log;

public class Missile {
    float width; //szerokość pocisku
    float height; //wysokość (długość)
    float xPosition; //położenie w osi X
    float yPosition; //położenie w osi Y
    float speed; //szybkość
    int direction; //kierunek (góra lub dół, bo strzela gracz i wróg)
    boolean isVisible; //czy dalej wyświetlać
    RectF bounds; //granice do wykrywania kolizji

    public final static int DIRECTION_UP = 100; //kierunek góra
    public final static int DIRECTION_DOWN = 200; //kierunek dół

    public Missile(int displayHeight) {
        width = 1;
        height = displayHeight / 25;
        speed = 300;
        isVisible = false;
        bounds = new RectF();
    }

    //uaktualnienie pozycji pocisku
    public void updatePosition(int framerPerSecond){
        switch (direction) {
            case DIRECTION_UP: //jeśli leci w górę
                yPosition -= speed / framerPerSecond;
                break;
            case DIRECTION_DOWN: //jeśli leci w dół
                yPosition += speed / framerPerSecond;
                break;
        }

        bounds.top = yPosition; //aktualizacja granic pocisku
        bounds.bottom = yPosition + height;
        bounds.left = xPosition;
        bounds.right = xPosition + width;
    }

    //inicjalizacja strzału pocisku
    public boolean fire(float initialX, float initialY, int direction) {
        if (!isVisible) {
            xPosition = initialX; //pozycja z której wystrzelono
            yPosition = initialY;
            this.direction = direction; //kierunek
            isVisible = true;
            return true;
        }
        return false;
    }

    //gettery i settery
    public boolean isVisible() {
        return isVisible;
    } //czy widoczny

    public RectF getBounds() {
        return bounds;
    } //granice

    public void setVisible(boolean visible) {
        isVisible = visible;
    } //ustaw widoczność

    public float getHeadY() { //granica - góra dla lotu w górę, dół dla lotu w dół
        if(direction == DIRECTION_UP)
            return yPosition;
        return yPosition + height;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getxPosition() {
        return xPosition;
    }

    public float getyPosition() {
        return yPosition;
    }

    public float getSpeed() {
        return speed;
    }

    public int getDirection() {
        return direction;
    }
}
