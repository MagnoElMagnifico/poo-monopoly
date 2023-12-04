package monopoly;

public interface JuegoConsts {
    long PRECIO_SOLAR1 = 1_000_000;

    int N_CASILLAS = 40;
    int N_GRUPOS = 10; // Hay 8 grupos de solares, 1 de transporte y 1 de servicios,
    int N_CARTAS_SUERTE = 6;
    int N_CARTAS_COMUNIDAD = 6;
    int N_IMPUESTOS = 2;


    String CONFIG_CASILLAS = "src/casillas.txt";
    String CONFIG_CARTAS = "src/cartas.txt";
    String CONFIG_AYUDA = "src/ayuda.txt";

    char[] AVATARES_ID = {'A', 'B', 'C', 'D', 'E', 'F'};
    int MAX_JUGADORES = AVATARES_ID.length;
    int MIN_JUGADORES = 2;
}
