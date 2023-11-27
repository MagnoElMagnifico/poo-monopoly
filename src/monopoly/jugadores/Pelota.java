package monopoly.jugadores;

import monopoly.Tablero;
import monopoly.casillas.Casilla;
import monopoly.utilidades.Consola;
import monopoly.utilidades.Dado;

public class Pelota extends Avatar{

    private Dado pelotaDado;            /* Solo para la pelota: guarda el dado usado en el tiro inicial (solo para calcular el alquiler de los transportes) */
    private int pelotaPosFinal;         /* Solo para la pelota: guarda la posición final a la que se tiene que llegar */


    public Pelota(char id, Jugador jugador, Casilla casillaInicial) {
        super(id, jugador, casillaInicial);
        pelotaDado =null;
        pelotaPosFinal=0;
    }

    public boolean mover(Dado dado, Tablero tablero) {
        if (dado == null && !isMovimientoEspecial()) {
            Consola.error("No puedes usar el comando siguiente si no estás usando el avatar Pelota");
            return false;
        }

        if (dado != null && pelotaDado != null) {
            Consola.error("No puedes lanzar más dados. Prueba con el comando siguiente");
            return false;
        }
        return(super.mover(dado, tablero, pelotaDado));
    }

    @Override
    public int moverEspecial(Dado dado, Casilla carcel) {
        return 0;
    }

    @Override
    public void cambiarModo() {
        // Mismo razonamiento que antes pero con la pelota: no se puede cambiar al
        // modo básico si todavía no se lanzó (pelotaDado es null, todavía no se
        // asignó) o si no quedan lanzamientos.
        if (isMovimientoEspecial() && pelotaDado != null && getLanzamientosRestantes() != 0) {
            Consola.error("No puedes cambiar de modo en mitad de un movimiento especial");
            return;
        }
        super.cambiarModo();
    }
}
