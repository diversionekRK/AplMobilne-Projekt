package com.example.ja.gra_projekt;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends Activity {

    GameView gameView; //widok z grą (osobny wątek do rysowania)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //pobranie rozmiarów wyświetlacza
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);

        //przekazanie kontekstu oraz rozmiarów ekranu do widoku gry
        gameView = new GameView(displaySize.x, displaySize.y, this);

        //ustawienie widoku
        setContentView(gameView);

    }

    class GameView extends SurfaceView implements Runnable {
        Bitmap titleBitmap;
        Context context; //do wykorzystania w metodach pobierających zasoby (bitmapy)
        int displayWidth; //szerokość ekranu
        int displayHeight; //wysokość ekranu
        SurfaceHolder surfaceHolder; //do zablokowania powierzchni do rysowania w bieżącym wątku (zapobiega konfliktom)
        Paint painter; //obiekt do rysowania
        Canvas canvas; //"płótno"
        volatile boolean isGameOn; //volatile - bo może być zmieniana przez inny wątek (zapobiega błędom dostępu do pamięci)
        boolean isOver; //czy bieżąca gra zakończona
        Thread graphicThread; //wątek do rysowania
        int framesPerSecond; //do zachowania podobnej płynności na różnych urządzeniach
        int numberOfEnemies; //liczba wrogów
        int nextMissile = 0; //zmień
        int maxImperialBullets = 25; //maks. liczba pocisków wystrzelonych naraz przez wrogów
        int numberOfShields; //liczba osłon
        int chances = 3; //ilość trafień jakie można otrzymać
        int gameStatus = STATUS_GAME_STARTED;
        int deadEnemies = 0; //liczba trafionych wrogów

        FalconMillenium falcon; //obiekt statku gracza
        Missile missile; //obiekt pocisku gracza
        Missile[] enemyBullets = new Missile[maxImperialBullets]; //obiekty pocisków wrogów
        ImperialShip[] imperials = new ImperialShip[ENEMIES_IN_ROW * ENEMIES_ROWS]; //obiekty statków wrogów
        Shield[] shields = new Shield[SHIELDS_IN_ROW * SHIELDS_ROWS * SHIELDS_BLOCKS]; //obiekty osłon

        public static final int ENEMIES_IN_ROW = 4; //liczba wrogów w jednym wierszu
        public static final int ENEMIES_ROWS = 4; //liczba wierszy z wrogami
        public static final int SHIELDS_IN_ROW = 10; //liczba tarczy w jednym wierszu
        public static final int SHIELDS_ROWS = 4; //liczba wierszy z tarczami
        public static final int SHIELDS_BLOCKS = 5; //liczba bloków tarcz
        public static final int STATUS_GAME_STARTED = 100; //liczba bloków tarcz
        public static final int STATUS_GAME_LOST = 200; //liczba bloków tarcz
        public static final int STATUS_GAME_WON = 300; //liczba bloków tarcz
        public static final int TEXT_SIZE = 50; //liczba bloków tarcz

        GameView(int displayWidth, int displayHeight, Context context) {
            //inicjalizacja pól
            super(context);
            this.context = context;
            this.displayWidth = displayWidth;
            this.displayHeight = displayHeight;
            surfaceHolder = getHolder();
            painter = new Paint();
            isOver = true;

            titleBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.background_bitmap);
            titleBitmap = Bitmap.createScaledBitmap(titleBitmap, (int) (displayWidth), (int) (displayHeight), false);

            //rozpoczęcie gry po przygotowaniu całej konfiguracji
            initializeGame();
        }

        private void initializeGame() {
            // utworzenie obiektu statku gracza
            falcon = new FalconMillenium(displayWidth, displayHeight, context);
            // utworzenie obiektu pocisku gracza
            missile = new Missile(displayHeight);
            // utworzenie obiektów wroga
            numberOfEnemies = 0;
            for(int columnNumber = 0; columnNumber < ENEMIES_IN_ROW; columnNumber ++ ){
                for(int rowNumber = 0; rowNumber < ENEMIES_ROWS; rowNumber ++ ){
                    imperials[numberOfEnemies] = new ImperialShip(displayWidth, displayHeight, context, rowNumber, columnNumber);
                    numberOfEnemies ++;
                }
            }
            // utworzenie obiektów pocisków wroga
            for(int i = 0; i < enemyBullets.length; i++)
                enemyBullets[i] = new Missile(displayHeight);
            // utworzenie osłon
            numberOfShields = 0;
            for(int blockNumber = 0; blockNumber < SHIELDS_BLOCKS; blockNumber++){
                for(int columnNumber = 0; columnNumber < SHIELDS_IN_ROW; columnNumber ++ ) {
                    for (int rowNumber = 0; rowNumber < SHIELDS_ROWS; rowNumber++) {
                        shields[numberOfShields] = new Shield(rowNumber, columnNumber, blockNumber, displayWidth, displayHeight, SHIELDS_BLOCKS);
                        numberOfShields++;
                    }
                }
            }
        }

        //metoda wywoływana przez wątek w czasie jego pracy
        @Override
        public void run() {
            //wątek pracuje dopóki nie zostanie zatrzymany lub nie zakończy się wykonywanie funkcji run (dlatego pętla)
            while(isGameOn) {
                long initialTime = System.currentTimeMillis();
                if(isOver == false) {
                    updateView();
                    paint();
                } else {
                    paintInfo();
                }


                long delay = System.currentTimeMillis() - initialTime; //czas wykonania jednego obrotu pętli rysującej
                if(delay != 0)
                    framesPerSecond = (int)(1000 / delay);
            }
        }

        //tutaj odbywa się aktualizacja wyświetlanych elementów
        private void updateView() {
            // czy wrogi statek doleciał do krawędzi (jeśli tak, to odwrócenie kierunku ruchu)
            boolean hasReachedEdge = false;

            // Has the player lost
            boolean lost = false;

            // aktualizacja pozycji statku gracza
            falcon.updatePosition(framesPerSecond);

            // aktualizacja pozycji statków wroga
            for(int i = 0; i < numberOfEnemies; i++){
                if(imperials[i].isAlive()) {
                    imperials[i].updatePosition(framesPerSecond);
                    //sprawdzenie, czy jest przy krawędzi ekranu
                    if (imperials[i].getXPosition() > displayWidth - imperials[i].getWidth() || imperials[i].getXPosition() < 0){
                        hasReachedEdge = true;
                    }
                    //sprawdzene, czy wystrzelić pocisk przez wroga
                    if(imperials[i].isGoingToFire(falcon.getXPosition(), falcon.getWidth())){

                        //wystrzał
                        if(enemyBullets[nextMissile].fire(imperials[i].getXPosition()
                                        + imperials[i].width / 2,
                                imperials[i].getYPosition(), missile.DIRECTION_DOWN)) {
                            nextMissile++;

                            if (nextMissile == maxImperialBullets) {
                                nextMissile = 0;
                            }
                        }
                    }
                }
            }

            // aktualizacja pozycji pocisków wroga
            for(int i = 0; i < enemyBullets.length; i++){
                if(enemyBullets[i].isVisible()) {
                    enemyBullets[i].updatePosition(framesPerSecond);
                }
            }

            // zmiana kierunku ruchu wrogów jeśli dolecieli do krawędzi
            if(hasReachedEdge){
                for(int i = 0; i < numberOfEnemies; i++){
                    imperials[i].changeDirection();
                    if(imperials[i].getYPosition() > displayHeight - displayHeight / 10){
                        //koniec gry, wróg doleciał do gracza
                        isOver = true;
                        gameStatus = STATUS_GAME_LOST;
                        initializeGame();
                    }
                }
            }

            // jeśli gra skończona
            if(isOver == true) {
                initializeGame();
            }

            // aktualizacja pozycji pocisku gracza
            if(missile.isVisible()){
                missile.updatePosition(framesPerSecond);
            }

            // deaktywacja pocisku gracza jeśli wyleciał za ekran
            if(missile.getHeadY() < 0){
                missile.setVisible(false);
            }

            // deaktywacja pocisków wroga jeśli wyleciały za ekran
            for(int i = 0; i < enemyBullets.length; i++){
                if(enemyBullets[i].getHeadY() > displayHeight){
                    enemyBullets[i].setVisible(false);
                }
            }

            // sprawdzenie, czy gracz trafił wroga
            checkIfEnemyHasBeenHit();

            // sprawdzenie, czy wróg trafił tarczę
            checkIfShieldHitByEnemy();

            // sprawdzenie, czy gracz trafił tarczę
            checkIfShieldHitByPlayer();

            // sprawdzenie, czy wróg trafił gracza
            checkIfPlayerHasBeenHit();
        }

        private void checkIfEnemyHasBeenHit() {
            if(missile.isVisible()) {
                for (int i = 0; i < numberOfEnemies; i++) {
                    if (imperials[i].isAlive()) {
                        if (RectF.intersects(missile.getBounds(), imperials[i].getBounds())) {
                            deadEnemies++;
                            imperials[i].setAlive(false);
                            missile.setVisible(false);
                            // tutaj sprawdzamy, czy ostatni zginął i ekran zwycięstwa
                            if(deadEnemies == ENEMIES_IN_ROW * ENEMIES_ROWS) {
                                deadEnemies = 0; //przegrana
                                isOver = true;
                                gameStatus = STATUS_GAME_WON;
                                initializeGame();
                            }
                        }
                    }
                }
            }
        }

        private void checkIfShieldHitByEnemy() {
            for(int i = 0; i < enemyBullets.length; i++){
                if(enemyBullets[i].isVisible()){
                    for(int j = 0; j < numberOfShields; j++){
                        if(shields[j].isAlive()){
                            if(RectF.intersects(enemyBullets[i].getBounds(), shields[j].getBounds())){
                                enemyBullets[i].setVisible(false);
                                shields[j].setAlive(false);
                            }
                        }
                    }
                }
            }
        }

        private void checkIfShieldHitByPlayer() {
            if(missile.isVisible()){
                for(int i = 0; i < numberOfShields; i++){
                    if(shields[i].isAlive()){
                        if(RectF.intersects(missile.getBounds(), shields[i].getBounds())){
                            missile.setVisible(false);
                            shields[i].setAlive(false);
                        }
                    }
                }
            }
        }

        private void checkIfPlayerHasBeenHit() {
            for(int i = 0; i < enemyBullets.length; i++){
                if(enemyBullets[i].isVisible()){
                    if(RectF.intersects(falcon.getBounds(), enemyBullets[i].getBounds())){
                        enemyBullets[i].setVisible(false);
                        chances--;
                        if(chances == 0){
                            isOver = true; //przegrana
                            chances = 3;
                            initializeGame();
                        }
                    }
                }
            }
        }

        //tutaj odbywa się odświeżanie widoku
        private void paint() {
            if (surfaceHolder.getSurface().isValid()) {
                canvas = surfaceHolder.lockCanvas(); //zablokowanie powierzchni do rysowania dla wątku
                canvas.drawColor(Color.rgb(22, 22, 22)); //malowanie tła
                painter.setColor(Color.rgb(255, 255, 255)); //kolor pędzla

                // rysowanie statku gracza
                canvas.drawBitmap(falcon.getFalconBitmap(), falcon.getXPosition(), displayHeight - falcon.getHeight(), painter);
                // rysowanie statków wroga
                for(int i = 0; i < numberOfEnemies; i++){
                    if(imperials[i].isAlive()) {
                        canvas.drawBitmap(
                                imperials[i].getImperialBitmap(),
                                imperials[i].getXPosition(),
                                imperials[i].getYPosition(),
                                painter);
                    }
                }

                painter.setColor(Color.rgb(222, 188, 0));
                for(int i = 0; i < numberOfShields; i++){ //rysowanie osłon
                    if(shields[i].isAlive) {
                        canvas.drawRect(shields[i].getBounds(), painter);
                    }
                }

                painter.setColor(Color.rgb(0, 255, 0));
                if(missile.isVisible()){ //rysowanie pocisku gracza
                    canvas.drawRect(missile.getBounds(), painter);
                }

                painter.setColor(Color.rgb(255, 0, 0));
                for(int i = 0; i < enemyBullets.length; i++){ //rysowanie pocisków wroga
                    if(enemyBullets[i].isVisible()) {
                        canvas.drawRect(enemyBullets[i].getBounds(), painter);
                    }
                }

                painter.setColor(Color.rgb(222, 222, 25));
                painter.setTextSize(50);
                canvas.drawText("Życia: " + chances, displayWidth - 200,45, painter); //wypisywanie liczby żyć

                surfaceHolder.unlockCanvasAndPost(canvas); //odblokowanie powierzchni po uprzednim odświeżeniu
            }
        }

        private void paintInfo() {
            if (surfaceHolder.getSurface().isValid()) {
                canvas = surfaceHolder.lockCanvas(); //zablokowanie powierzchni do rysowania dla wątku
                canvas.drawColor(Color.rgb(22, 22, 22)); //malowanie tła
                painter.setTextSize(TEXT_SIZE * 2);
                painter.setColor(Color.rgb(222, 222, 25));
                switch (gameStatus) {
                    case STATUS_GAME_STARTED:
                        canvas.drawBitmap(
                                titleBitmap,
                                0,
                                0,
                                painter);
                        canvas.drawText("Klewek Rafał - gra", displayWidth / 2 - displayWidth / 4,displayHeight / 2, painter);
                        break;
                    case STATUS_GAME_LOST:
                        painter.setColor(Color.rgb(255, 0, 0));
                        canvas.drawText("PRZEGRANA", displayWidth / 2 - displayWidth / 5,displayHeight / 2, painter);
                        break;
                    case STATUS_GAME_WON:
                        painter.setColor(Color.rgb(0, 255, 0));
                        canvas.drawText("WYGRANA", displayWidth / 2 - displayWidth / 5,displayHeight / 2, painter);
                        break;
                }
                painter.setColor(Color.rgb(222, 111, 0));
                canvas.drawText("Naciśnij aby grać...", displayWidth / 2 - displayWidth / 4,displayHeight / 8 * 7, painter);
                surfaceHolder.unlockCanvasAndPost(canvas); //odblokowanie powierzchni po uprzednim odświeżeniu
            }
        }

        public void stop() { //zatrzymanie wątku z grą
            isGameOn = false;
            try {
                graphicThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void start() { //rozpoczęcie wątku z grą
            isGameOn = true;
            graphicThread = new Thread(this);
            graphicThread.start();
        }

        @Override //obsługa gestów
        public boolean onTouchEvent(MotionEvent motionEvent) {
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN: //jeśli dotknięto ekran
                    //sterowanie statkiem gracza (lewo i prawo)
                    if(motionEvent.getY() > displayHeight - displayHeight / 8 && !isOver) {
                        if (motionEvent.getX() > displayWidth / 2) {
                            falcon.setDirection(FalconMillenium.DIRECTION_RIGHT);
                        } else {
                            falcon.setDirection(FalconMillenium.DIRECTION_LEFT);
                        }

                    } //strzelanie
                    if(motionEvent.getY() < displayHeight - displayHeight / 8 && !isOver) {
                        missile.fire(
                                falcon.getXPosition() + falcon.getWidth() / 2,
                                displayHeight - falcon.getHeight(),
                                Missile.DIRECTION_UP);
                    }
                    break;

                case MotionEvent.ACTION_UP: //jeśli puszczono ekran
                    isOver = false; //rozpoczęcie, jeśli gra była wstrzymana
                    falcon.setDirection(FalconMillenium.DIRECTION_NONE); //zatrzymanie ruchu statku
                    break;
            }
            return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //rozpoczęcie gry
        gameView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        //zakończenie gry
        gameView.stop();
    }

}