package monopoly.casilla.carta;

import monopoly.Juego;
import monopoly.JuegoConsts;
import monopoly.casilla.Casilla;
import monopoly.jugador.Jugador;
import monopoly.utils.Consola;

import java.util.Collections;
import java.util.List;

public abstract class CasillaAccion extends Casilla {
    public CasillaAccion(int posicion) {
        super(posicion);
    }

    /**
     * Reordena aleatoriamente las cartas
     */
    public static <T> void barajar(List<T> cartas) {
        Collections.shuffle(cartas);
    }

    /**
     * Función de ayuda que pregunta y devuelve una elección de carta al usuario
     */
    public static int preguntarEleccion(Jugador jugador, int max) {
        int eleccion = -1;

        while (eleccion <= 0 || eleccion > max) {
            String respuesta = Juego.consola.leer("%s, elige carta (1-%d): ".formatted(jugador.getNombre(), max));

            try {
                eleccion = Integer.parseInt(respuesta);
            } catch (NumberFormatException e) {
                Juego.consola.imprimir("Por favor, introduce un número válido\n");
                eleccion = -1;
            }
        }

        return eleccion;
    }

    @Override
    public String listar() {
        return '\n' + getNombreFmt() + '\n';
    }

    @Override
    public int codColorRepresentacion() {
        return JuegoConsts.COD_COLOR_ACCION;
    }

    @Override
    public Consola.Estilo estiloRepresentacion() {
        return JuegoConsts.EST_ACCION;
    }
}
