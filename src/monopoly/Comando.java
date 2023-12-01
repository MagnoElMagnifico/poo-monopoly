package monopoly;

public interface Comando {
    /**
     * Muestra el mensaje de ayuda de todos los comandos
     */
    void ayuda();

    /**
     * Inicia la partida
     */
    void iniciar();

    /**
     * Muestra el tablero del juego
     */
    void verTablero();

    /**
     * Muestra el jugador que tiene el turno
     */
    void jugador();

    /**
     * Saca al jugador de la cárcel pagando la fianza
     */
    void salirCarcel();

    /**
     * Cambia el modo del avatar del jugador actual de básico a avanzado y viceversa
     */
    void cambiarModo();

    /**
     * Genera un dado aleatorio y mueve el avatar actual
     */
    void lanzar();

    /**
     * Avanza las posiciones correspondientes cuando se usa el avatar Pelota
     */
    void siguiente();

    /**
     * Termina el turno del jugador actual
     */
    void acabarTurno();

    /**
     * Declara al jugador actual en bancarrota
     */
    void bancarrota();

    /**
     * Añade un jugador a la partida
     * <pre>
     *     crear jugador {nombre} { c, coche | e, esfinge | s, sombrero | p, pelota }
     * </pre>
     */
    void crearJugador(String[] args);

    /**
     * Permite al jugador actual comprar una propiedad
     * <pre>
     *     comprar {propiedad}
     * </pre>
     */
    void comprar(String[] args);

    /**
     * Permite al jugador actual edificar en el solar donde se encuentra
     * <pre>
     *     edificar {tipo edificio} [cantidad]
     * </pre>
     */
    void edificar(String[] args);

    /**
     * Permite vender al jugador actual los edificios previamente construidos
     * <pre>
     *     vender {tipo edificio} {solar} [cantidad]
     * </pre>
     */
    void vender(String[] args);

    /**
     * Permite hipotecar una propiedad
     * <pre>
     *      hipotecar {propiedad}
     * </pre>
     */
    void hipotecar(String[] args);

    /**
     * Permite deshipotecar una propiedad
     * <pre>
     *      deshipotecar {propiedad}
     * </pre>
     */
    void deshipotecar(String[] args);

    /**
     * Permite crear un trato entre varios jugadores
     * <pre>
     *     trato { nombre jugador }: cambiar { propiedad } por { propiedad }
     *     trato { nombre jugador }: cambiar { propiedad } por { cantidad }
     *     trato { nombre jugador }: cambiar { cantidad } por { propiedad }
     *     trato { nombre jugador }: cambiar { propiedad } por { propiedad } y { cantidad }
     *     trato { nombre jugador }: cambiar { propiedad } y { cantidad } por { propiedad }
     *     trato { nombre jugador }: cambiar { propiedad } por { propiedad } y noalquiler { propiedad } durante { numero turnos }
     * </pre>
     */
    void trato(String[] args);

    /**
     * Permite a un jugador aceptar un trato
     * <pre>
     *     aceptar { ID trato }
     * </pre>
     */
    void aceptar(String[] args);

    /**
     * Permite al jugador que propuso un trato, eliminarlo.
     * <pre>
     *     eliminar { ID trato }
     * </pre>
     */
    void eliminar(String[] args);

    /**
     * Muestra la información completa sobre el jugador, casilla o avatar dado
     * <pre>
     *     describir {casilla}
     *     describir jugador {jugador}
     *     describir avatar {avatar}
     * </pre>
     */
    void describir(String[] args);

    /**
     * Permite mostrar información sobre ciertos elementos del juego
     * <pre>
     *     listar { casillas | jugadores | enventa | avatares | tratos }
     *     listar edificios { nombre grupo }
     * </pre>
     * @see Listable
     */
    void listar(String[] args);

    /**
     * Calcula unas estadísticas sobre la partida actual o sobre
     * los jugadores de la misma.
     * <pre>
     *     estadisticas
     *     estadisticas { nombre jugador }
     * </pre>
     */
    void estadisticas(String[] args);

    // ==== COMANDOS DEBUG ============================================================

    /**
     * Ejecuta los comandos línea a línea del archivo dado por parámetro
     * <p>
     * NOTA: solo DEBUG.
     * <pre>
     *     exec {nombre archivo}
     * </pre>
     */
    void ejecutarArchivo(String[] args);

    /**
     * Cobra o ingresa la cantidad dada al jugador actual
     * <pre>
     *     fortuna {jugador} {cantidad}
     * </pre>
     * Si cantidad > 0 se ingresa, si cantidad < 0 se cobra
     */
    void fortuna(String[] args);

    /**
     * Mueve el avatar actual con el número de posiciones del dado
     * <p>
     * NOTA: solo DEBUG.
     * <pre>
     *     mover {dado 1} [dado2]
     * </pre>
     */
    void mover(String[] args);
}
