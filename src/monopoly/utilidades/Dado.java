package monopoly.utilidades;

import java.util.Random;

public class Dado {
    private Random rand;

    public Dado() {
        rand = new Random();
    }

    public int lanzar() {
        return rand.nextInt(6) + 1;
    }

    public int lanzar2Dados() {
        return this.lanzar() + this.lanzar();
    }
}
