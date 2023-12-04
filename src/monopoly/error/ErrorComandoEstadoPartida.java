package monopoly.error;

/**
 * Errores relacionados con el estado de la partida:
 *
 * <li>No se inició la partida</li>
 * <li>La partida ya fue iniciada</li>
 * <li>No hay jugadores</li>
 * <li>No hay suficientes jugadores para iniciar la partida</li>
 * <li>Demasiados jugadores</li>
 */
public class ErrorComandoEstadoPartida extends ErrorComando {
    public ErrorComandoEstadoPartida(String msg) {
        super(msg);
    }
}
