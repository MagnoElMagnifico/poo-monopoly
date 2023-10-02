package monopoly.utilidades;

import java.util.Random;

/**
 * Clase de ayuda para simular el lanzamiento de un dado.
 *
 * @author Marcos Granja Grille
 * @date 25-09-2023
 */
public class Dado {
    private final Random rand;

    /**
     * Crea un nuevo Dado.
     */
    public Dado() {
        rand = new Random();
    }

    /**
     * Lanza un dado y devuelve el resultado.
     *
     * @return Número aleatorio del 1 al 6 (ambos incluidos).
     */
    public int lanzar() {
        return rand.nextInt(6) + 1;
    }

    /**
     * Lanza dos dados y devuelve el resultado
     * <p>
     * Equivalente a Dado.lanzar() + Dado.lanzar()
     *
     * @return Número aleatorio del 2 al 12 (ambos includidos).
     */
    public int lanzar2Dados() {
        return this.lanzar() + this.lanzar();
    }

    /**
     * Genera una letra mayúscula aleatoria. Útil para generar un ID para un avatar.
     */
    public char letraAleatoria() {
        return (char)(rand.nextInt((int) 'Z' - (int) 'A' + 1) + (int) 'A');
    }
}
