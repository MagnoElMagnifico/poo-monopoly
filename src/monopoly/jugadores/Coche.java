package monopoly.jugadores;

import monopoly.Tablero;
import monopoly.casillas.Casilla;
import monopoly.utilidades.Dado;
import monopoly.utilidades.Consola;

public class Coche extends Avatar{

    private boolean puedeComprar;       /* Solo para el coche: no permite comprar más una vez por turno */
    private int penalizacion;           /* Solo para el coche: no puede tirar en los dos siguientes turnos si saca < 4 */

    public Coche(char id, Jugador jugador, Casilla casillaInicial) {
        super(id, jugador, casillaInicial);
        this.puedeComprar=true;
        this.penalizacion=0;

    }


    public boolean mover(Dado dado, Tablero tablero) {
        // Penalización de turnos para el coche
        if (penalizacion != 0) {
            Consola.error("Restaurando avatar: espera %d turno(s) para poder moverte".formatted(penalizacion));
            return false;
        }
        return super.mover(dado, tablero, null);

    }

    @Override
    public int moverEspecial(Dado dado, Casilla carcel) {
        return 0;
    }

    @Override
    public void cambiarModo() {
        // Se puede cambiar el modo de Coche a básico si todavía no se ha lanzado
        // (4 es la cantidad de lanzamientos inicial) o si ya se ha terminado de
        // lanzar (0, no quedan lanzamientos).
        if (isMovimientoEspecial() && getLanzamientosRestantes() != 4 && getLanzamientosRestantes() != 0) {
            Consola.error("No puedes cambiar de modo en mitad de un movimiento especial");
            return;
        }

        super.cambiarModo();

        // Actualizar los lanzamientos restantes.
        // De lo contrario, el avatar coche puede conseguir 4 lanzamientos
        // básicos si cambia dos veces de modo.
        // No se cambia si ya se gastaron todos los tiros en el turno.
        if (getLanzamientosRestantes() != 0) {
            setLanzamientosRestantes(isMovimientoEspecial()  ? 4 : 1);
        }
    }
}
