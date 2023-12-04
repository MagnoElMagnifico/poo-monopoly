package monopoly.casilla.propiedad;

import monopoly.Calculadora;
import monopoly.Juego;
import monopoly.JuegoConsts;
import monopoly.casilla.edificio.Edificio;
import monopoly.error.ErrorFatalLogico;
import monopoly.jugador.Avatar;
import monopoly.jugador.Jugador;
import monopoly.utils.Dado;

import java.util.ArrayList;

/**
 *
 */
public class Solar extends Propiedad {
    private long precio;
    private long alquilerTotalCobrado;

    public Solar(int posicion, Grupo grupo, String nombre, Jugador banca) {
        super(posicion, grupo, nombre, banca);

        precio = (long) (0.3 * grupo.getNumero() * JuegoConsts.PRECIO_SOLAR1 + JuegoConsts.PRECIO_SOLAR1);
        alquilerTotalCobrado = 0;
    }

    @Override
    public String toString() {
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
                    Juego.consola.num(Calculadora.precio(Edificio.TipoEdificio.Casa, this)),
                    Juego.consola.num(Calculadora.precio(Edificio.TipoEdificio.Hotel, this)),
                    Juego.consola.num(Calculadora.precio(Edificio.TipoEdificio.Piscina, this)),
                    Juego.consola.num(Calculadora.precio(Edificio.TipoEdificio.PistaDeporte, this)),
                    // ----------------------------------------------------------
                    Juego.consola.num(Calculadora.alquilerEdificio(this, Edificio.TipoEdificio.Casa, 1)),
                    Juego.consola.num(Calculadora.alquilerEdificio(this, Edificio.TipoEdificio.Casa, 2)),
                    Juego.consola.num(Calculadora.alquilerEdificio(this, Edificio.TipoEdificio.Casa, 3)),
                    Juego.consola.num(Calculadora.alquilerEdificio(this, Edificio.TipoEdificio.Casa, 4)),
                    Juego.consola.num(Calculadora.alquilerEdificio(this, Edificio.TipoEdificio.Hotel, 1)),
                    Juego.consola.num(Calculadora.alquilerEdificio(this, Edificio.TipoEdificio.Piscina, 1)),
                    Juego.consola.num(Calculadora.alquilerEdificio(this, Edificio.TipoEdificio.PistaDeporte, 1)));
        // @formatter:on

    }

    @Override
    public long getPrecio() {
        return precio;
    }

    @Override
    public long getAlquiler() {
        long alquilerSolar = precio / 10;

        // TODO: edificios
        long alquilerEdificios = 0;

        if (getGrupo().isMonopolio(getPropietario())) {
            alquilerSolar *= 2;
        }

        return alquilerSolar + alquilerEdificios;
    }

    @Override
    public long getAlquiler(Jugador jugador, Dado dado) {
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

    /*
    public void anadirEdificio(Edificio e) {
        edificios.add(e);
    }

    public boolean quitarEdificio(Edificio.TipoEdificio tipo) {
        for (int ii = 0; ii < edificios.size(); ii++) {
            if (edificios.get(ii).getTipo() == tipo) {
                edificios.remove(ii);
                return true;
            }
        }

        return false;
    }

    public int contarEdificios(Edificio.TipoEdificio tipo) {
        int numero = 0;
        for (Edificio e : edificios) {
            if (e.getTipo() == tipo) {
                numero++;
            }
        }

        return numero;
    }

    public ArrayList<Edificio> getEdificios() {
        if (this.tipo != TipoPropiedad.Solar) {
            Consola.error("[Propiedad] %s no tiene edificios".formatted(tipo));
            return null;
        }

        return edificios;
    }
    */
}
