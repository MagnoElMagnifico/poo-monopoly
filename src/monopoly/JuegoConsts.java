package monopoly;

public interface JuegoConsts {
    int N_CASILLAS = 40;
    int N_GRUPOS = 10; // Hay 8 grupos de solares, 1 de transporte y 1 de servicios,
    int N_CARTAS_SUERTE = 6;
    int N_CARTAS_COMUNIDAD = 6;

    int MAX_JUGADORES = 6;
    int MIN_JUGADORES = 2;

    String CONFIG_CASILLAS = "src/casillas.txt";
    String CONFIG_CARTAS = "src/cartas.txt";
    String CONFIG_AYUDA = "src/ayuda.txt";
}
