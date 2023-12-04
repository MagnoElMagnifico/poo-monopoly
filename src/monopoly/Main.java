package monopoly;

import monopoly.error.ErrorJuego;

public class Main {
    public static void main(String[] args) {
        try {
            new Juego().iniciarConsola();
        } catch (ErrorJuego e) {
            e.imprimirMsg();
        }
    }
}
