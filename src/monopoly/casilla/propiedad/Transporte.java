package monopoly.casilla.propiedad;

import monopoly.error.ErrorFatalLogico;
import monopoly.jugador.Jugador;
import monopoly.utils.Dado;

public class Transporte extends Propiedad {
    private long precio;
    private long alquilerTotalCobrado;

    public Transporte(int posicion, Grupo grupo, String nombre, Jugador propietario) {
        super(posicion, grupo, nombre, propietario);

        precio = -1;
        alquilerTotalCobrado = 0;
    }


    public void setPrecio(long precio) {
        this.precio = precio;
    }

    @Override
    public long getPrecio() throws ErrorFatalLogico {
        if (precio < 0) {
            throw new ErrorFatalLogico("No se puede obtener el precio sin antes haberlo asignado");
        }

        return precio;
    }

    @Override
    public long getAlquiler() throws ErrorFatalLogico {
        long nTransportesPosee = this.getGrupo().contarPropiedades(this.getPropietario());
        long nTransportes = this.getGrupo().getNumeroPropiedades();

        // El alquiler es el factor de transporte por el
        // porcentaje que el jugador posea.
        return (long) ((float) getPrecio() * (float) nTransportesPosee  / (float) nTransportes);
    }

    @Override
    public long getAlquiler(Jugador jugador, Dado dado) throws ErrorFatalLogico {
        return getAlquiler();
    }

    @Override
    public long getAlquilerTotalCobrado() {
        return alquilerTotalCobrado;
    }
}
