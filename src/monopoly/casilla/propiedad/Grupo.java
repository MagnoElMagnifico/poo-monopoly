package monopoly.casilla;

import monopoly.utilidades.Consola;

import java.util.HashSet;

/**
 * Representa un grupo lógico de casillas.
 *
 * @author Marcos Granja Grille
 * @date 10-10-2023
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
    private final HashSet<Casilla> casillas;

    public Grupo(int numero, String nombre, int codigoColor) {
        this.numero = numero;
        this.nombre = nombre;
        this.codigoColor = codigoColor;
        this.casillas = new HashSet<>(3);
    }

    @Override
    public String toString() {
        return """
                {
                    nombre: %s
                    número: %d
                    casillas: %s
                }""".formatted(Consola.fmt(nombre, codigoColor), numero, Consola.listar(casillas.iterator(), Casilla::getNombre));
    }

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof Grupo && ((Grupo) obj).nombre.equals(this.nombre);
    }

    public void anadirCasilla(Casilla casilla) {
        casillas.add(casilla);
    }

    public int getNumeroCasillas() {
        return casillas.size();
    }

    public int getNumero() {
        return numero;
    }

    /**
     * Devuelve el número de solar del grupo
     */
    public int getNumeroSolar() {
        return numero - 3;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCodigoColor() {
        return codigoColor;
    }

    public HashSet<Casilla> getCasillas() {
        return casillas;
    }
}
