package monopoly.utilidades;

import java.util.Random;

/**
 * Clase de ayuda para simular el lanzamiento de un dado y
 * otras operaciones con números aleatorios.
 *
 * @author Marcos Granja Grille
 * @date 25-09-2023
 */
public class Dado {
    private final int dado1;
    private final int dado2;

    /**
     * Crea un nuevo Dado aleatorio.
     */
    public Dado() {
        Random rand = new Random();
        dado1 = rand.nextInt(6) + 1;
        dado2 = rand.nextInt(6) + 1;
    }

    /**
     * Crea un nuevo dado trucado con los valores dados.
     */
    public Dado(int v1, int v2) {
        dado1 = v1;
        dado2 = v2;
    }

    @Override
    public String toString() {
        return "(%d: %d, %d)".formatted(dado1 + dado2, dado1, dado2);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof Dado && ((Dado) obj).dado1 == dado1 && ((Dado) obj).dado2 == dado2;
    }

    public int getDado1() {
        return dado1;
    }

    public int getDado2() {
        return dado2;
    }

    /**
     * Devuelve la suma de los valores de los dos dados
     */
    public int getValor() {
        return dado1 + dado2;
    }

    /**
     * Devuelve true si el tiro ha sido doble (mismo número en ambos)
     */
    public boolean isDoble() {
        return dado1 == dado2;
    }
}
