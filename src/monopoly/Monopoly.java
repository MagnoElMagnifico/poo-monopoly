package monopoly;

import java.util.Scanner;
import monopoly.utilidades.Consola;

/**
 * Clase principal del juego del Monopoly.
 *
 * @author Marcos Granja Grille
 * @date 25-09-2023
 */
public class Monopoly {
    private Scanner scanner;

    public Monopoly() {
        scanner = new Scanner(System.in);
    }

    /**
     * Inicia la consola del juego del Monopoly.
     * Muestra el Prompt ("$>") y permite al usuario escribir un comando.
     */
    public void iniciarConsola() {
        while(true) {
            System.out.print("$> ");
            String input = scanner.nextLine();
            Consola.println(input, Consola.Estilo.Negrita, Consola.Estilo.FuenteRojo);
        }
    }
}
