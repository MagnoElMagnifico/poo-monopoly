package monopoly.casilla.carta;

import monopoly.Juego;
import monopoly.error.ErrorFatal;
import monopoly.jugador.Jugador;

/** Es una clase final porque ya implementa todo lo necesario y no tiene sentido crear subtipos. */
public final class CartaComunidad extends Carta {
    private final Juego juego;

    public CartaComunidad(int id, String descripcion, Juego juego) {
        super(id, descripcion);
        this.juego = juego;
    }

    @Override
    public String toString() {
        return "Carta de Comunidad: " + super.getDescripcion();
    }

    @Override
    public void accionCarta() throws ErrorFatal {
        Jugador jugadorTurno = juego.getJugadorTurno();

        // Se ingresa si la cantidad es positiva y se cobra si es negativa
        long cantidad = 0;

        switch (super.getId()) {
            case 4 -> cantidad = 2_000_000L;
            case 6 -> cantidad = 500_000L;
            case 9 -> cantidad = 1_000_000L;
            case 1 -> cantidad = -150_000L;
            case 5 -> cantidad = -1_000_000;
            case 8 -> {
                long cantidadPorJugador = 250_000L;
                jugadorTurno.cobrar(cantidadPorJugador * (juego.getJugadores().size() - 1), juego.getBanca());

                // NOTA: el pago a otros jugadores no se considera una tasa.
                // No hace falta actualizar las estadísticas del jugadorTurno

                // NOTA: se está ingresando el dinero a cada jugador incluso si el jugador no puede pagarlo

                for (Jugador j : juego.getJugadores()) {
                    // No ingresar la cantidad a sí mismo
                    if (j.equals(jugadorTurno)) {
                        continue;
                    }

                    j.ingresar(cantidadPorJugador);
                    j.getEstadisticas().anadirPremio(cantidadPorJugador);
                }

                return;
            }

            default -> throw new ErrorFatal("ID no soportado");
        }

        super.aplicarCantidad(cantidad, jugadorTurno, juego.getBanca());
    }
}
