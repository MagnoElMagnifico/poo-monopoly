package monopoly.casilla.propiedad;

import monopoly.error.ErrorFatalLogico;
import monopoly.jugador.Jugador;
import monopoly.utils.Dado;

public class Servicio extends Propiedad {
    private long precio;
    private long alquilerTotalCobrado;

    public Servicio(int posicion, Grupo grupo, String nombre, Jugador propietario) {
        super(posicion, grupo, nombre, propietario);

        precio = -1;
        alquilerTotalCobrado = 0;
    }

    @Override
    public long getPrecio() throws ErrorFatalLogico {
        if (precio < 0) {
            throw new ErrorFatalLogico("No se puede obtener el precio sin antes haberlo asignado");
        }

        return precio;
    }

    public void setPrecio(long precio) {
        this.precio = precio;
    }

    @Override
    public long getAlquiler() throws ErrorFatalLogico {
        throw new ErrorFatalLogico("Se necesita el dado para calcular el alquiler de un servicio");
    }

    @Override
    public long getAlquiler(Jugador jugador, Dado dado) throws ErrorFatalLogico {
        // Finalmente, si posee solo 1 servicio, se multiplica por 4.
        // Si tiene los dos, se multiplica por 10.
        long alquiler = switch (this.getGrupo().contarPropiedades(this.getPropietario())) {
            case 1 -> (long) ((float) dado.getValor() * ((float) this.getPrecio() * 2.85) / 200) * 4;
            case 2 -> (long) ((float) dado.getValor() * ((float) this.getPrecio() * 2.85) / 200) * 10;
            default -> throw new ErrorFatalLogico("nServicios no es 1 ni 2");
        };
        // El factor de servicio será 200 veces inferior a la cantidad que recibirá
        // el jugador cada vez que completa una vuelta al tablero

        alquilerTotalCobrado += alquiler;

        return alquiler;
    }

    @Override
    public long getAlquilerTotalCobrado() {
        return alquilerTotalCobrado;
    }
}