package monopoly.casillas.edificios;

/**
 * La clase PistaDeporte representa un edificio de tipo pista de deporte en el juego de Monopoly.
 * Esta clase hereda de la clase Edificio.
 * 
 * @see Edificio
 */
public class PistaDeporte extends Edificio {
    /**
     * Calcula el precio de construcción de un edificio de tipo pista de deporte en un solar dado.
     * El precio se calcula como el 40% del precio del solar.
     * 
     * @param solar el solar en el que se construirá el edificio
     * @param cantidad la cantidad de edificios de este tipo que se construirán
     * @return el precio de construcción del edificio
     */
    @Override
    public long precioEdificio(Solar solar, int cantidad) {
        long precioSolar = solar.getPrecio();
        return (long) (0.4 * precioSolar);
    }

    /**
     * Calcula el alquiler de un edificio de tipo pista de deporte en un solar dado.
     * El alquiler se calcula como 25 veces el alquiler del solar multiplicado por la cantidad de edificios de este tipo.
     * 
     * @param solar el solar en el que se encuentra el edificio
     * @param cantidad la cantidad de edificios de este tipo que se han construido en el solar
     * @return el alquiler del edificio
     */
    @Override
    public long alquilerEdificio(Solar solar, int cantidad) {
        long alquilerSolar = solar.getAlquiler();
        return 25 * alquilerSolar * cantidad;
    }
}
