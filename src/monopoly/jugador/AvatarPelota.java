package monopoly.jugador;

import monopoly.Juego;
import monopoly.casilla.Casilla;
import monopoly.casilla.especial.CasillaCarcel;
import monopoly.casilla.especial.CasillaSalida;
import monopoly.error.ErrorComandoAvatar;
import monopoly.error.ErrorComandoFortuna;
import monopoly.error.ErrorFatal;
import monopoly.utils.Dado;

public class AvatarPelota extends Avatar {

    private Dado pelotaDado;            /* Solo para la pelota: guarda el dado usado en el tiro inicial (solo para calcular el alquiler de los transportes) */
    private int pelotaPosFinal;         /* Solo para la pelota: guarda la posición final a la que se tiene que llegar */


    public AvatarPelota(char id, CasillaSalida salida) {
        super(id, salida);
        pelotaDado = null;
        pelotaPosFinal = 0;
    }

    public void mover(Juego juego, Dado dado) throws ErrorComandoAvatar, ErrorFatal, ErrorComandoFortuna {
        if (dado == null && !isMovimientoEspecial()) {
            throw new ErrorComandoAvatar("No puedes usar el comando siguiente si no estás usando el avatar Pelota", this);
        }

        if (dado != null && pelotaDado != null) {
            throw new ErrorComandoAvatar("No puedes lanzar más dados. Prueba con el comando siguiente", this);
        }
        super.mover(juego, dado == null ? pelotaDado : dado);
    }

    @Override
    public int moverEspecial(Dado dado, CasillaCarcel carcel) throws ErrorComandoAvatar {
        Casilla casilla = getCasilla();
        // Si ha salido un dado doble, se mueve de forma básica
        if (getDoblesSeguidos() != 0) {
            return super.irCarcelDadosDobles(dado, carcel) ? Integer.MAX_VALUE : casilla.getPosicion() + dado.getValor();
        }

        // Primera tirada
        if (dado != null) {
            int valorDado = dado.getValor();

            // Si es menor que 4 se mueve hacia atrás
            if (valorDado < 4) {
                // Como es la última tirada, se tiene en cuenta si los dados han sido dobles
                return irCarcelDadosDobles(dado, carcel) ? Integer.MAX_VALUE : casilla.getPosicion() - valorDado;
            }

            // Si el resultado es 4 o 5, como no hay impares de por medio
            // se realiza un movimiento normal (no hay que hacer ningún salto)
            if (valorDado <= 5) {
                // Como es la última tirada, se tiene en cuenta si los dados han sido dobles
                return irCarcelDadosDobles(dado, carcel) ? Integer.MAX_VALUE : casilla.getPosicion() + valorDado;
            }

            // En otro caso, guardamos la posición final que marcan los dados
            pelotaPosFinal = getCasilla().getPosicion() + valorDado;
            pelotaDado = dado;

            // Movemos 5 posiciones dado que ese siempre será el primer salto
            setLanzamientosRestantes(1);
            return getCasilla().getPosicion() + 5;
        }

        // La cantidad de casillas que me tengo que mover es 2 o 1.
        // Esto funciona porque el primer salto cae en una casilla impar
        // y luego se va sumando 2 (o 1) hasta llegar a la casilla final.
        int paso = Math.min(pelotaPosFinal - casilla.getPosicion(), 2);

        if (paso <= 0) {
            throw new ErrorComandoAvatar("[Avatar Pelota] Paso negativo o nulo", this);
        }

        // Si me muevo según el paso calculado y termino en la casilla
        // que me interesa, debo terminar el turno.
        // Como es la última tirada, hay que tener en cuenta si los dados
        // fueron dobles, por eso uso movimientoBasico()
        if (casilla.getPosicion() + paso == pelotaPosFinal) {
            setLanzamientosRestantes(0);

            // Como es la última tirada, se tiene en cuenta si los dados han sido dobles
            if (irCarcelDadosDobles(pelotaDado, carcel)) {
                // Se borra el dado del turno anterior para que se pueda volver a usar lanzar
                pelotaDado = null;

                return Integer.MAX_VALUE;
            }

            // Se borra el dado del turno anterior para que se pueda volver a usar lanzar
            pelotaDado = null;

            return casilla.getPosicion() + paso;
        }

        setLanzamientosRestantes(1);
        return casilla.getPosicion() + paso;
    }


    public void cambiarModo() throws ErrorComandoAvatar {
        // Mismo razonamiento que antes pero con la pelota: no se puede cambiar al
        // modo básico si todavía no se lanzó (pelotaDado es null, todavía no se
        // asignó) o si no quedan lanzamientos.
        if (isMovimientoEspecial() && pelotaDado != null && getLanzamientosRestantes() != 0) {
            throw new ErrorComandoAvatar("No puedes cambiar de modo en mitad de un movimiento especial", this);
        }
        super.cambiarModo();
    }

    @Override
    public boolean acabarTurno() throws ErrorComandoAvatar {
        int lanzamientosRestantes = getLanzamientosRestantes();
        Jugador jugador = getJugador();
        if (lanzamientosRestantes > 0) {
            throw new ErrorComandoAvatar("A %s aún le quedan %d tiros".formatted(jugador.getNombre(), lanzamientosRestantes), this);
        }
        setLanzamientosRestantes(1);
        setDoblesSeguidos(0);

        pelotaDado = null;
        pelotaPosFinal = 0;

        return true;
    }
}
