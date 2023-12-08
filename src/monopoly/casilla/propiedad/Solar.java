package monopoly.casilla.propiedad;

import monopoly.Juego;
import monopoly.JuegoConsts;
import monopoly.casilla.edificio.*;
import monopoly.error.ErrorComando;
import monopoly.error.ErrorComandoEdificio;
import monopoly.error.ErrorComandoFortuna;
import monopoly.error.ErrorFatalLogico;
import monopoly.jugador.Jugador;
import monopoly.utils.Dado;

import java.util.ArrayList;

public class Solar extends Propiedad {
    private final ArrayList<Edificio> edificios;
    private long precio;
    private final long alquilerTotalCobrado;

    public Solar(int posicion, Grupo grupo, String nombre, Jugador banca) {
        super(posicion, grupo, nombre, banca);

        precio = (long) (0.3 * grupo.getNumero() * JuegoConsts.PRECIO_SOLAR1 + JuegoConsts.PRECIO_SOLAR1);
        alquilerTotalCobrado = 0;
        edificios = new ArrayList<>();
    }

    @Override
    public String toString() {
        try {
            // @formatter:off
            return """
                    {
                        tipo: Solar
                        nombre: %s
                        grupo: %s
                        precio: %s
                        alquiler: %s
                        propietario: %s
                        edificios: %s
                        hipotecada?: %s
                        ================================
                        valor casa: %s
                        valor hotel: %s
                        valor piscina: %s
                        valor pista de deporte: %s
                        --------------------------------
                        alquiler una casa: %s
                        alquiler dos casas: %s
                        alquiler tres casas: %s
                        alquiler cuatro casas: %s
                        alquiler hotel: %s
                        alquiler piscina: %s
                        alquiler pista de deporte: %s
                    }""".formatted(
                        getNombre(),
                        getGrupo().getNombre(),
                        Juego.consola.num(getPrecio()),
                        Juego.consola.num(getAlquiler()),
                        getPropietario().getNombre(),
                        Juego.consola.listar(getEdificios(), Edificio::getNombreFmt),
                        isHipotecada()? "Sí" : "No",
                        // ==========================================================
                        Juego.consola.num(Casa.getValor(this)),
                        Juego.consola.num(Hotel.getValor(this)),
                        Juego.consola.num(Piscina.getValor(this)),
                        Juego.consola.num(PistaDeporte.getValor(this)),
                        // ----------------------------------------------------------
                        Juego.consola.num(Casa.getAlquiler(this, 1)),
                        Juego.consola.num(Casa.getAlquiler(this, 2)),
                        Juego.consola.num(Casa.getAlquiler(this, 3)),
                        Juego.consola.num(Casa.getAlquiler(this, 4)),
                        Juego.consola.num(Hotel.getAlquiler(this)),
                        Juego.consola.num(Piscina.getAlquiler(this)),
                        Juego.consola.num(PistaDeporte.getAlquiler(this)));
            // @formatter:on
        } catch (ErrorFatalLogico e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getPrecio() {
        return precio;
    }

    @Override
    public long getAlquiler() throws ErrorFatalLogico {
        long alquilerSolar = precio / 10;

        // Alquiler extra por edificios
        long alquilerEdificios = 0;
        int nCasas = 0;

        for (Edificio e : edificios) {
            if (e.getClass().getName().equals("Casa")) {
                nCasas++;
            } else {
                alquilerEdificios += e.getAlquiler();
            }
        }

        // Las casas se aplican por separado
        alquilerEdificios += Casa.getAlquiler(this, nCasas);

        if (getGrupo().isMonopolio(getPropietario())) {
            alquilerSolar *= 2;
        }

        return alquilerSolar + alquilerEdificios;
    }

    @Override
    public void hipotecar() throws ErrorComandoFortuna, ErrorFatalLogico, ErrorComandoEdificio {
        if (!edificios.isEmpty()) {
            throw new ErrorComandoEdificio("No se puede hipotecar una propiedad con edificios");
        }

        super.hipotecar();
    }

    @Override
    public long getAlquiler(Jugador jugador, Dado dado) throws ErrorFatalLogico {
        return getAlquiler();
    }

    public void factorPrecio(float factor) throws ErrorFatalLogico {
        if (factor <= 0.0) {
            throw new ErrorFatalLogico("El factor no puede ser negativo o nulo");
        }

        precio = (long) ((float) precio * factor);
    }

    @Override
    public long getAlquilerTotalCobrado() {
        return alquilerTotalCobrado;
    }

    /**
     * <b>NOTA</b>: requerida por la especificación de la entrega 3.
     */
    public void edificar(Edificio e) {
        edificios.add(e);
    }

    public void demoler(String tipo) throws ErrorComando {
        for (int ii = 0; ii < edificios.size(); ii++) {
            if (edificios.get(ii).getClass().getName().equals(tipo)) {
                edificios.remove(ii);
                return;
            }
        }

        throw new ErrorComando("\"%s\" tipo de edificio no encontrado");
    }

    public ArrayList<Edificio> getEdificios() {
        return edificios;
    }

    public int contarEdificios(String tipo) {
        int numero = 0;

        for (Edificio e : edificios) {
            if (e.getClass().getName().equals(tipo)) {
                numero++;
            }
        }

        return numero;
    }
}
