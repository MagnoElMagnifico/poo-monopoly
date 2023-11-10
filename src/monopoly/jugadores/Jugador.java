package monopoly.jugadores;

import monopoly.Calculadora;
import monopoly.casillas.Casilla;
import monopoly.casillas.Edificio;
import monopoly.casillas.Edificio.TipoEdificio;
import monopoly.casillas.Grupo;
import monopoly.casillas.Propiedad;
import monopoly.jugadores.Avatar.TipoAvatar;
import monopoly.utilidades.Consola;
import monopoly.utilidades.Dado;

import java.util.HashSet;

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
    private final boolean banca;
    private final HashSet<Propiedad> propiedades;
    private long fortuna;
    private long gastos;

    /**
     * Crea el jugador especial Banca
     */
    public Jugador() {
        this.nombre = "Banca";
        this.avatar = null;
        this.banca = true;
        this.fortuna = 0;
        this.gastos = 0;
        this.propiedades = new HashSet<>(28);
    }

    /**
     * Crea un Jugador dado su nombre, tipo de avatar e id
     */
    public Jugador(String nombre, TipoAvatar tipo, char id, Casilla casillaInicial, long fortuna) {
        this.avatar = new Avatar(tipo, id, this, casillaInicial);
        this.nombre = nombre;
        this.banca = false;
        this.fortuna = fortuna;
        this.gastos = 0;
        this.propiedades = new HashSet<>();
    }

    private String listarEdificios() {
        StringBuilder edificios = new StringBuilder();
        edificios.append('[');

        // Si es el primer elemento que se añade a la lista,
        // no se añade coma; pero sí en el resto.
        boolean primero = true;
        for (Propiedad p : propiedades) {
            if (p.getTipo() == Propiedad.TipoPropiedad.Solar) {
                for (Edificio e : p.getEdificios()) {
                    if (primero) {
                        primero = false;
                    } else {
                        edificios.append(", ");
                    }

                    edificios.append(e.getNombreFmt());
                }
            }
        }

        edificios.append(']');
        return edificios.toString();
    }

    @Override
    public String toString() {
        if (banca) {
            return "Jugador Especial: Banca\n";
        }

        // TODO: hipotecas
        // @formatter:off
        return """
                {
                    nombre: %s
                    avatar: %c
                    fortuna: %s
                    gastos: %s
                    propiedades: %s
                    edificios: %s
                }""".formatted(nombre,
                               avatar.getId(),
                               Consola.num(fortuna),
                               Consola.num(gastos),
                               Consola.listar(propiedades.iterator(), (p) -> p.getCasilla().getNombreFmt()),
                               listarEdificios());
        // @formatter:on
    }

    /**
     * Devuelve un String con información sobre la fortuna, gastos y propiedades del jugador
     */
    public String describirTransaccion() {
        return """
                {
                    fortuna: %s
                    gastos: %s
                    propiedades: %s
                }
                """.formatted(Consola.num(fortuna), Consola.num(gastos), Consola.listar(propiedades.iterator(), (p) -> p.getCasilla().getNombreFmt()));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof Jugador && ((Jugador) obj).getAvatar().equals(avatar);
    }

    /**
     * Hace que el jugador compre la propiedad a la banca
     *
     * @return True cuando la operación resultó exitosa, false en otro caso.
     */
    public boolean comprar(Propiedad p) {
        // Comprobar que el jugador no haya comprado ya la casilla
        if (propiedades.contains(p)) {
            Consola.error("El jugador %s ya ha comprado la casilla %s.".formatted(nombre, p.getCasilla().getNombreFmt()));
            return false;
        }

        // Comprobar que no sea propiedad de otro jugador
        if (!p.getPropietario().isBanca()) {
            Consola.error("No se pueden comprar propiedades de otro jugador");
            System.out.printf("%s pertenece a %s\n", p.getCasilla().getNombreFmt(), Consola.fmt(p.getPropietario().getNombre(), Consola.Color.Azul));
            return false;
        }

        // Comprobar que el jugador tiene fortuna suficiente
        if (!cobrar(p.getPrecio())) {
            Consola.error("%s no dispone de suficiente dinero para comprar %s"
                    .formatted(nombre, p.getCasilla().getNombreFmt()));
            return false;
        }

        p.getPropietario().quitarPropiedad(p);
        anadirPropiedad(p);
        p.setPropietario(this);

        System.out.printf("""
                El jugador %s ha comprado la casilla %s por %s
                Ahora tiene una fortuna de %s
                """, nombre, p.getCasilla().getNombreFmt(), Consola.num(p.getPrecio()), Consola.num(fortuna));

        // Actualizar los precios de los alquileres si se acaba de
        // completar un Monopolio
        boolean tenerMonopolio = true;
        for (Casilla c : p.getCasilla().getGrupo().getCasillas()) {
            // Si se encuentra una propiedad cuyo propietario no es este jugador,
            // es que no tiene el monopolio
            if (!c.getPropiedad().getPropietario().equals(this)) {
                tenerMonopolio = false;
                break;
            }
        }

        if (tenerMonopolio) {
            for (Casilla c : p.getCasilla().getGrupo().getCasillas()) {
                Propiedad propiedad = c.getPropiedad();
                propiedad.actualizarAlquiler();
            }

            Grupo g = p.getCasilla().getGrupo();
            System.out.printf("""
                    Con esta casilla, %s completa el Monopolio de %s!
                    Ahora los alquileres de ese grupo valen el doble.
                    """, Consola.fmt(nombre, Consola.Color.Azul), Consola.fmt(g.getNombre(), g.getCodigoColor()));
        }

        return true;
    }

    /** Comprueba las restricciones de construcción */
    private boolean edificable(Propiedad solar, Edificio.TipoEdificio tipo) {
        Grupo grupo = solar.getCasilla().getGrupo();
        int nCasillasGrupo = grupo.getNumeroCasillas();

        switch (tipo) {
            case Casa -> {
                if (grupo.contarEdificios(TipoEdificio.Hotel) < nCasillasGrupo) {
                    if (solar.contarEdificios(TipoEdificio.Casa) >= 4) {
                        Consola.error("No se pueden edificar más de 4 casas en un solar cuando no hay el máximo de hoteles");
                        return false;
                    }
                } else if (grupo.contarEdificios(TipoEdificio.Casa) >= nCasillasGrupo) {
                    Consola.error("No se pueden edificar más de %d casas en un grupo cuando hay el número máximo de hoteles".formatted(nCasillasGrupo));
                    return false;
                }
            }

            case Hotel -> {
                if (grupo.contarEdificios(TipoEdificio.Hotel) >= nCasillasGrupo) {
                    Consola.error("No se pueden edificar más de %d hoteles en este grupo".formatted(nCasillasGrupo));
                    return false;
                }

                if (solar.contarEdificios(TipoEdificio.Casa) < 4) {
                    Consola.error("Se necesitan 4 casas en el solar para edificar un hotel");
                    return false;
                }
            }

            case Piscina -> {
                if (grupo.contarEdificios(TipoEdificio.Piscina) >= nCasillasGrupo) {
                    Consola.error("No se pueden edificar más de %d piscinas en este grupo".formatted(nCasillasGrupo));
                    return false;
                }

                if (grupo.contarEdificios(TipoEdificio.Hotel) < 1 || grupo.contarEdificios(Edificio.TipoEdificio.Casa) < 2) {
                    Consola.error("Se necesita 1 hotel y 2 casas para edificar una piscina");
                    return false;
                }
            }

            case PistaDeporte -> {
                if (grupo.contarEdificios(TipoEdificio.PistaDeporte) >= nCasillasGrupo) {
                    Consola.error("No se pueden edificar más de %d pistas de deporte en este grupo".formatted(nCasillasGrupo));
                    return false;
                }

                if (grupo.contarEdificios(TipoEdificio.Hotel) < 2) {
                    Consola.error("Se necesitan 2 hoteles para construir una pista de deporte");
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Realiza la compra del edificio y lo construye en el solar dado.
     *
     * @return True si la operación es exitosa y false en otro caso.
     */
    public boolean comprar(Edificio e) {
        Propiedad solar = e.getSolar();
        Casilla casilla = solar.getCasilla();

        // Comprobar que se está edificando en la casilla en la que está el avatar
        if (!casilla.equals(avatar.getCasilla())) {
            Consola.error("No se puede edificar en una casilla distinta a la actual");
            return false;
        }

        if (!solar.getPropietario().equals(this)) {
            Consola.error("No se puede edificar en una propiedad que no te pertenece");
            return false;
        }

        // Calcula el número de estancias del avatar en el solar
        int nEstanciasCasilla = 0;
        for (Casilla c : avatar.getHistorialCasillas()) {
            if (c.equals(casilla)) {
                nEstanciasCasilla++;
            }
        }

        // Comprobar que el jugador tiene el monopolio o ha caído dos veces en la casilla
        if (!Calculadora.tieneGrupo(solar) && nEstanciasCasilla <= 2) {
            Consola.error("El jugador tiene que tener el Monopolio o haber pasado más de 2 veces por la casilla para poder edificar");
            return false;
        }

        if (!edificable(solar, e.getTipo())) {
            return false;
        }

        // Comprobar que tiene el dinero
        if (!cobrar(e.getValor())) {
            Consola.error("El jugador no tiene los fondos suficientes para edificar.\nNecesita %s.".formatted(Consola.num(e.getValor())));
            return false;
        }

        System.out.printf("""
                %s ha construido un/a %s en la casilla %s por %s.
                Ahora tiene una fortuna de %s.
                """, nombre, e.getTipo(), casilla.getNombreFmt(), Consola.num(e.getValor()), Consola.num(fortuna));

        // Actualizar el solar
        solar.anadirEdificio(e);
        solar.actualizarAlquiler();

        // Quitar las 4 casas requeridas por el hotel
        if (e.getTipo() == TipoEdificio.Hotel) {
            for (int ii = 0; ii < 4; ii++) {
                solar.quitarEdificio(TipoEdificio.Casa);
            }
        }

        return true;
    }

    /**
     * Hace que el jugador page el alquiler correspondiente
     * al dueño de la casilla en donde se encuentra
     */
    public void pagarAlquiler(Propiedad p, Dado dado) {
        if (p.getPropietario().isBanca() || p.getPropietario().equals(this)) {
            return;
        }

        long importe = switch (p.getTipo()) {
            case Solar, Transporte -> p.getAlquiler();
            case Servicio -> p.getAlquiler() * dado.getValor() * 4;
        };

        if (!cobrar(importe)) {
            Consola.error("El jugador no tiene suficientes fondos para pagar el alquiler");
            return;
        }

        p.getPropietario().ingresar(importe);
        System.out.printf("Se han pagado %s de alquiler a %s\n", Consola.num(p.getAlquiler()), Consola.fmt(p.getPropietario().getNombre(), Consola.Color.Azul));
    }

    /**
     * Cobra al jugador una cantidad de dinero
     *
     * @param cantidad Dinero a ingresar
     * @return True si la operación es correcta, false en otro caso
     */
    public boolean cobrar(long cantidad) {
        if (cantidad <= 0 || cantidad > fortuna) {
            return false;
        }

        fortuna -= cantidad;
        gastos += cantidad;
        return true;
    }

    /**
     * Ingresa una cantidad de dinero al jugador
     */
    public void ingresar(long cantidad) {
        if (cantidad <= 0) {
            Consola.error("[Jugador] No se puede ingresar una cantidad negativa o nula");
            return;
        }

        fortuna += cantidad;
    }

    public boolean isBanca() {
        return banca;
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

    public HashSet<Propiedad> getPropiedades() {
        return propiedades;
    }

    public void anadirPropiedad(Propiedad p) {
        propiedades.add(p);
    }

    public void quitarPropiedad(Propiedad p) {
        propiedades.remove(p);
    }
}
