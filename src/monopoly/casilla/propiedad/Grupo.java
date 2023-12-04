package monopoly.casilla.propiedad;

import monopoly.Juego;
import monopoly.casilla.Casilla;
import monopoly.jugador.Banca;
import monopoly.jugador.Jugador;

import java.util.ArrayList;

/**
 * Representa un grupo lógico de casillas.
 *
 * @see Casilla
 */
public class Grupo {
    /**
     * Este es un identificador único del grupo, dado por el orden de
     * declaración en el archivo de configuración de las casillas.
     * <p>
     * Idealmente, tienen esta forma:
     * <li> 0: Casillas especiales (Cárcel, Parking, IrCárcel, Impuestos...)
     * <li> 1: Transportes
     * <li> 2: Servicios
     * <li> El resto: solares.
     */
    private final int numero;
    private final String nombre;
    private final int codigoColor;
    private final ArrayList<Propiedad> propiedades;

    public Grupo(int numero, String nombre, int codigoColor) {
        this.numero = numero;
        this.nombre = nombre;
        this.codigoColor = codigoColor;
        this.propiedades = new ArrayList<>(3);
    }

    @Override
    public String toString() {
        return """
                {
                    nombre: %s
                    número: %d
                    casillas: %s
                }""".formatted(Juego.consola.fmt(nombre, codigoColor), numero, Juego.consola.listar(propiedades, Propiedad::getNombre));
    }

    /*
    public void listarEdificios() {
        for (Casilla c : casillas) {
            if (c.isPropiedad() && c.getPropiedad().getTipo() == Propiedad.TipoPropiedad.Solar) {
                Propiedad p = c.getPropiedad();

                System.out.printf("""
                                {
                                    propiedad: %s
                                    casas: %s
                                    hoteles: %s
                                    piscinas: %s
                                    pistas de deporte: %s
                                    alquiler: %s
                                }
                                """, p.getNombre(),
                        Consola.listar(p.getEdificios().iterator(), (e) -> e.getTipo() == Edificio.TipoEdificio.Casa ? e.getNombreFmt() : null),
                        Consola.listar(p.getEdificios().iterator(), (e) -> e.getTipo() == Edificio.TipoEdificio.Hotel ? e.getNombreFmt() : null),
                        Consola.listar(p.getEdificios().iterator(), (e) -> e.getTipo() == Edificio.TipoEdificio.Piscina ? e.getNombreFmt() : null),
                        Consola.listar(p.getEdificios().iterator(), (e) -> e.getTipo() == Edificio.TipoEdificio.PistaDeporte ? e.getNombreFmt() : null),
                        Consola.num(p.getAlquiler()));
            }
        }
    }

    public int contarEdificios(Edificio.TipoEdificio tipo) {
        int numero = 0;

        for (Casilla c : casillas) {
            if (c.isPropiedad()) {
                for (Edificio e : c.getPropiedad().getEdificios()) {
                    if (e.getTipo() == tipo) {
                        numero++;
                    }
                }
            }
        }

        return numero;
    }
    */

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof Grupo && ((Grupo) obj).nombre.equals(this.nombre);
    }

    public boolean isMonopolio(Jugador jugador) {
        // Los monopolios no aplican a la banca
        if (jugador instanceof Banca) {
            return false;
        }

        return contarPropiedades(jugador) == propiedades.size();
    }

    public int contarPropiedades(Jugador jugador) {
        // Contar cuantos servicios posee el jugador
        int nPropiedades = 0;

        for (Propiedad p : propiedades) {
            if (p.perteneceAJugador(jugador)) {
                nPropiedades++;
            }
        }

        return nPropiedades;
    }

    public void anadirPropiedad(Propiedad propiedad) {
        propiedades.add(propiedad);
    }

    public int getNumeroPropiedades() {
        return propiedades.size();
    }

    /**
     * Devuelve el número de solar del grupo
     */
    public int getNumero() {
        return numero;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCodigoColor() {
        return codigoColor;
    }

    public ArrayList<Propiedad> getPropiedades() {
        return propiedades;
    }
}
