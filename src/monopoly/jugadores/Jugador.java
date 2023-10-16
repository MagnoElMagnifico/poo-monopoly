package monopoly.jugadores;

import monopoly.casillas.Propiedad;
import monopoly.jugadores.Avatar.TipoAvatar;
import monopoly.casillas.Casilla;
import monopoly.utilidades.Formatear;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Clase que representa un Jugador. Almacena su información sobre su fortuna y propiedades.
 * Además, tiene un Avatar asociado.
 *
 * @date 2-10-2023
 * @see Avatar
 */
public class Jugador {
    private final String nombre;
    private final Avatar avatar;
    private long fortuna;
    private long gastos;
    private ArrayList<Propiedad> propiedades;

    /**
     * Crea el jugador especial Banca
     */
    public Jugador() {
        this.nombre = "Banca";
        this.avatar = null;
        this.fortuna = 0;
        this.gastos = 0;
        this.propiedades = new ArrayList<>(28);
    }

    /**
     * Crea un Jugador dado su nombre, tipo de avatar e id
     */
    public Jugador(String nombre, TipoAvatar tipo, char id, Casilla casillaInicial, long fortuna) {
        this.avatar = new Avatar(tipo, id, this, casillaInicial);
        this.nombre = nombre;
        this.fortuna = fortuna;
        this.gastos = 0;
        this.propiedades = new ArrayList<>();
    }

    private String listaPropiedades() {
        StringBuilder propiedadesStr = new StringBuilder();

        propiedadesStr.append('[');
        Iterator<Propiedad> iter = propiedades.iterator();

        while (iter.hasNext()) {
            Casilla c = iter.next().getCasilla();
            propiedadesStr.append(Formatear.con(c.getNombre(), (byte) c.getGrupo().getCodigoColor()));

            if (iter.hasNext()) {
                propiedadesStr.append(", ");
            }
        }

        propiedadesStr.append(']');

        return propiedadesStr.toString();
    }

    @Override
    public String toString() {
        if (avatar == null) {
            return "Jugador Especial: Banca\n";
        }

        return """
                {
                    nombre: %s
                    avatar: %c
                    fortuna: %s
                    gastos: %s
                    propiedades: %s
                }""".formatted(nombre, avatar.getId(), Formatear.num(fortuna), Formatear.num(gastos), listaPropiedades());
        // TODO: hipotecas
        // TODO: edificios
    }

    public String describirTransaccion() {
        return """
                {
                    fortuna: %s
                    gastos: %s
                    propiedades: %s
                }
                """.formatted(Formatear.num(fortuna), Formatear.num(gastos), listaPropiedades());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Jugador && ((Jugador) obj).getAvatar() == avatar;
    }

    /**
     * Cobra al jugador una cantidad de dinero
     */
    public void cobrar(long cantidad) {
        if (cantidad > 0 && cantidad >= fortuna) {
            fortuna -= cantidad;
            gastos += cantidad;
        }
        // TODO: lanzar error de lo contrario
        // TODO: lanzar error en caso de que no tenga suficientes fondos
    }

    /**
     * Ingresa una cantidad de dinero al jugador
     */
    public void ingresar(long cantidad) {
        if (cantidad > 0) {
            fortuna += cantidad;
        }
        // TODO: lanzar error de lo contrario
    }

    public String getNombre() {
        return nombre;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public long getFortuna() {
        return fortuna;
    }

    public long getGastos() {
        return gastos;
    }

    public ArrayList<Propiedad> getPropiedades() {
        return propiedades;
    }

    public void anadirPropiedad(Propiedad p) {
        propiedades.add(p);
    }

    public void quitarPropiedad(Propiedad p) {
        propiedades.remove(p);
    }
}