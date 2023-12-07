package monopoly.casilla.propiedad;

import monopoly.Juego;
import monopoly.casilla.Casilla;
import monopoly.casilla.edificio.*;
import monopoly.error.ErrorFatalLogico;
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

    public void listarEdificios() throws ErrorFatalLogico {
        for (Propiedad p : propiedades) {
            if (p instanceof Solar) {
                ArrayList<Edificio> edificios = ((Solar) p).getEdificios();

                Juego.consola.imprimir("""
                                {
                                    propiedad: %s
                                    casas: %s
                                    hoteles: %s
                                    piscinas: %s
                                    pistas de deporte: %s
                                    alquiler: %s
                                }
                                """.formatted(p.getNombre(),
                        Juego.consola.listar(edificios, (e) -> e instanceof Casa ? e.getNombreFmt() : null),
                        Juego.consola.listar(edificios, (e) -> e instanceof Hotel ? e.getNombreFmt() : null),
                        Juego.consola.listar(edificios, (e) -> e instanceof Piscina ? e.getNombreFmt() : null),
                        Juego.consola.listar(edificios, (e) -> e instanceof PistaDeporte ? e.getNombreFmt() : null),
                        Juego.consola.num(p.getAlquiler())));
            }
        }
    }

    public int contarEdificios(String tipo) {
        int numero = 0;

        for (Propiedad p : propiedades) {
            if (p instanceof Solar) {
                numero += ((Solar) p).contarEdificios(tipo);
            }
        }

        return numero;
    }

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
        // Contar cuantas propiedades posee el jugador
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

    public String getNombreFmt() {
        return Juego.consola.fmt(getNombre(), codigoColor);
    }

    public int getCodigoColor() {
        return codigoColor;
    }

    public ArrayList<Propiedad> getPropiedades() {
        return propiedades;
    }
}
