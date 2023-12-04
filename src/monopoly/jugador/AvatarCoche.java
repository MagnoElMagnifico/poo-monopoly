package monopoly.jugador;

import monopoly.Juego;
import monopoly.Tablero;
import monopoly.casilla.especial.CasillaCarcel;
import monopoly.casilla.especial.CasillaSalida;
import monopoly.casillas.Casilla;
import monopoly.error.ErrorComandoAvatar;
import monopoly.error.ErrorComandoFortuna;
import monopoly.error.ErrorFatal;
import monopoly.utils.Dado;
import monopoly.utils.Consola;

public class AvatarCoche extends Avatar {
    private boolean puedeComprar; /* No permite comprar más una vez por turno */
    private int penalizacion;     /* No puede tirar en los dos siguientes turnos si saca < 4 */

    public AvatarCoche(char id, CasillaSalida salida) {
        super(id, salida);

        this.puedeComprar = true;
        this.penalizacion = 0;
    }

    public void mover(Juego juego, Dado dado) throws ErrorFatal, ErrorComandoFortuna, ErrorComandoAvatar {
        // Penalización de turnos para el coche
        if (penalizacion != 0) {
            throw new ErrorComandoAvatar("Restaurando avatar: espera %d turno(s) para poder moverte".formatted(penalizacion), this);
        }
        super.mover(juego, dado);
    }

    @Override
    public int moverEspecial(Dado dado, Casilla carcel) {
        Casilla casilla =getCasilla();
        // Si ha salido un dado doble, se mueve de forma básica
        if (getDoblesSeguidos() != 0) {
            return irCarcelDadosDobles(dado, carcel) ? Integer.MAX_VALUE : casilla.getPosicion() + dado.getValor();
        }

        // Penalización por sacar menos de un 4
        if (dado.getValor() < 4) {
            // Dos turnos de penalización en los que no se puede lanzar
            // (se restará 1 al cambiar de turno).
            penalizacion = 3;
            // Se ponen los lanzamientos restantes a 0, indicando que debe terminar el turno
            setLanzamientosRestantes(0);

            System.out.printf("Se aplica una penalización de %s por sacar un valor tan bajo.\n", Consola.fmt("2 turnos", Consola.Color.Azul));

            // Se retrocede el valor de los dados
            // Aunque sea la última tirada no se tienen en cuenta los dados dobles
            // porque se ha aplicado una penalización
            return casilla.getPosicion() - dado.getValor();
        }

        // En caso de que sea la última tirada, se tiene en cuenta los dados dobles
        // Cuando se tire otra vez, también pasará por aquí
        if (getLanzamientosRestantes() == 0 && irCarcelDadosDobles(dado, carcel)) {
            return Integer.MAX_VALUE;
        }

        return casilla.getPosicion() + dado.getValor();
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

    @Override
    public boolean acabarTurno() {
        int lanzamientosRestantes= getLanzamientosRestantes();
        Jugador jugador = getJugador();
        if (lanzamientosRestantes > 0 && penalizacion == 0) {
            Consola.error("A %s aún le quedan %d tiros".formatted(jugador.getNombre(), lanzamientosRestantes));
            return false;
        }

        if (isMovimientoEspecial() && !isEncerrado()) {
            setLanzamientosRestantes(4);
        } else {
            setLanzamientosRestantes(1);
        }

        if (penalizacion != 0) {
            penalizacion--;
        }

        setDoblesSeguidos(0);
        puedeComprar = true;

        return true;
    }

    @Override
    public int moverEspecial(Dado dado, CasillaCarcel carcel) {
        return 0;
    }

    public boolean isPuedeComprar() {
        return puedeComprar;
    }

    public void noPuedeComprar() {
        if (isMovimientoEspecial()) {
            this.puedeComprar = false;
        }
    }
}
