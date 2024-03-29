package monopoly.error;

import monopoly.jugador.Avatar;

/**
 * Cualquier error relacionado con el movimiento de los avatares.
 *
 * <li>Aún quedan tiros
 * <li>No quedan lanzamientos
 * <li>Comando siguiente estando encerrado
 * <li>Comando siguiente después de dados dobles
 * <li>No puedes usar el comando siguiente
 * <li>No está en la cárcel
 * <li>Penalización
 * <li>Cambiar de modo en mitad de un movimiento
 */
public class ErrorComandoAvatar extends ErrorComando {
    /**
     * El avatar que ha causado el error
     */
    private final Avatar avatar;

    public ErrorComandoAvatar(String mensaje, Avatar avatar) {
        super(mensaje);
        this.avatar = avatar;
    }

    public Avatar getAvatar() {
        return avatar;
    }
}
