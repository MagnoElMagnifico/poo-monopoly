package monopoly.casilla;

import monopoly.Juego;
import monopoly.JuegoConsts;
import monopoly.casilla.carta.*;
import monopoly.casilla.especial.CasillaCarcel;
import monopoly.casilla.especial.CasillaIrCarcel;
import monopoly.casilla.especial.CasillaSalida;
import monopoly.casilla.propiedad.*;
import monopoly.error.ErrorFatalConfig;
import monopoly.error.ErrorFatalLogico;
import monopoly.jugador.Banca;
import monopoly.jugador.Jugador;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Clase de ayuda que lee de un archivo una lista de casillas
 * y crea los objetos acordes.
 * <p>
 * Formato esperado:
 *
 * <pre>
 *     grupo: Nombre, CodColor    # Grupo de propiedades (grupos especiales: Transporte, Servicios)
 *     CasillaEspecial            # Salida, Cárcel, IrCárcel, Parking, Impuestos
 *     Nombre, NúmeroGrupo        # Propiedad: solar (se empieza a contar desde 1)
 *     Nombre, Transporte         # Propiedad: transporte
 *     Nombre, Servicio           # Propiedad: servicio
 *     ...
 * </pre>
 *
 * <p>
 * También se usa para leer el nombre y descripción de las
 * Cartas de Comunidad y Suerte.
 * <p>
 * Formato:
 *
 * <pre>
 *     S:X:descripción
 *     C : X : descripción
 *     ...
 * </pre>
 *
 * Finalmente, termina asignando los valores requeridos a cada
 * objeto.
 *
 * @see Casilla
 * @see monopoly.Juego
 */
public class Lector {
    private final ArrayList<Casilla> casillas;
    private final ArrayList<Grupo> grupos;
    private final ArrayList<CartaComunidad> cartasComunidad;
    private final ArrayList<CartaSuerte> cartasSuerte;

    // Información útil
    private int nSolares;
    private long sumaPrecioSolares;
    private final long fortunaInicial;
    private final ArrayList<CasillaImpuesto> impuestos;

    // Casillas especiales
    private CasillaSalida salida;
    private CasillaCarcel carcel;
    private CasillaIrCarcel irCarcel;
    private CasillaParking parking;

    // Grupos especiales
    private Grupo transportes;
    private Grupo servicios;

    public Lector(Juego juego) throws ErrorFatalConfig, ErrorFatalLogico {
        // @formatter:off
        casillas        = new ArrayList<>(JuegoConsts.N_CASILLAS);
        grupos          = new ArrayList<>(JuegoConsts.N_GRUPOS);
        cartasComunidad = new ArrayList<>(JuegoConsts.N_CARTAS_COMUNIDAD);
        cartasSuerte    = new ArrayList<>(JuegoConsts.N_CARTAS_SUERTE);
        impuestos       = new ArrayList<>(JuegoConsts.N_IMPUESTOS);
        // @formatter:on

        nSolares = 0;
        sumaPrecioSolares = 0;
        salida = null;
        carcel = null;
        irCarcel = null;
        parking = null;
        transportes = null;
        servicios = null;

        leerCartas(juego);
        leerCasillas(juego.getBanca());

        long abonoSalida = sumaPrecioSolares / nSolares;
        fortunaInicial = sumaPrecioSolares / 3;

        // Terminar de asignar todos los valores
        irCarcel.setCarcel(carcel);
        salida.setAbonoSalida(abonoSalida);
        carcel.setFianza(abonoSalida / 4);
        for (CasillaImpuesto i : impuestos) {
            i.setImpuestos(abonoSalida);
        }

        // Precios que dependen del abono de salida
        for (Propiedad s : servicios.getPropiedades()) {
            ((Servicio) s).setPrecio((long) (0.75 * (float) abonoSalida));
        }

        for (Propiedad t : transportes.getPropiedades()) {
            ((Transporte) t).setPrecio(abonoSalida);
        }
    }

    public long getFortunaInicial() {
        return fortunaInicial;
    }

    public ArrayList<CartaComunidad> getCartasComunidad() {
        return cartasComunidad;
    }

    public ArrayList<CartaSuerte> getCartasSuerte() {
        return cartasSuerte;
    }

    public ArrayList<Casilla> getCasillas() {
        return casillas;
    }

    public CasillaSalida getSalida() {
        return salida;
    }

    public CasillaCarcel getCarcel() {
        return carcel;
    }

    public CasillaIrCarcel getIrCarcel() {
        return irCarcel;
    }

    public CasillaParking getParking() {
        return parking;
    }

    public ArrayList<Grupo> getGrupos() {
        return grupos;
    }

    public Grupo getServicios() {
        return servicios;
    }

    public Grupo getTransportes() {
        return transportes;
    }

    /**
     * Función de ayuda que abre un Scanner para leer un archivo.
     * @throws ErrorFatalConfig Si no se encuentra el archivo.
     */
    private static Scanner abrirArchivo(String path) throws ErrorFatalConfig {
        Scanner scanner;

        try {
            scanner = new Scanner(new File(path));
        } catch (FileNotFoundException e) {
            throw new ErrorFatalConfig("No se ha encontrado", path, 0);
        }

        return scanner;
    }

    /**
     * Función de ayuda que ignora líneas en blanco y comentarios
     */
    private static boolean ignorarLinea(String linea) {
        return linea.isBlank() || linea.stripLeading().startsWith("#");
    }

    /**
     * Lee el archivo de configuración de cartas del archivo de
     * configuración especificado en <code>JuegoConsts.CONFIG_CARTAS</code>.
     * @throws ErrorFatalConfig Si no se encuentra el archivo o si el
     *                          formato no es correcto.
     */
    public void leerCartas(Juego juego) throws ErrorFatalConfig {
        try (Scanner scanner = abrirArchivo(JuegoConsts.CONFIG_CARTAS)) {
            for (int nLinea = 0; scanner.hasNextLine(); nLinea++) {
                String linea = scanner.nextLine().strip();

                // Se ignoran los comentarios y líneas en blanco
                if (ignorarLinea(linea)) {
                    continue;
                }

                // Se limpia la línea y se separa en campos
                String[] campos = linea.strip().replaceAll(" *: *", ":").replaceAll("  +", " ").split(":");

                if (campos.length != 3) {
                    scanner.close();
                    throw new ErrorFatalConfig("Número de campos incorrecto", JuegoConsts.CONFIG_CARTAS, nLinea);
                }

                try {
                    switch (campos[0]) {
                        case "C" -> cartasComunidad.add(new CartaComunidad(Integer.parseInt(campos[1]), campos[2], juego));
                        case "S" -> cartasSuerte.add(new CartaSuerte(Integer.parseInt(campos[1]), campos[2], juego));
                        default -> {
                            scanner.close();
                            throw new ErrorFatalConfig("\"%s\": tipo de carta desconocido".formatted(campos[0]), JuegoConsts.CONFIG_CARTAS, nLinea);
                        }
                    }
                } catch (NumberFormatException e) {
                    throw new ErrorFatalConfig("Número no válido: " + e, JuegoConsts.CONFIG_CASILLAS, nLinea);
                }
            } // for
        } // try
    } // método

    /**
     * Lee el archivo de configuración de Casillas del archivo de
     * configuración especificado en <code>JuegoConsts.CONFIG_CASILLAS</code>.
     * @throws ErrorFatalConfig Si no se encuentra el archivo o si el
     *                          formato no es correcto.
     */
    private void leerCasillas(Banca banca) throws ErrorFatalConfig, ErrorFatalLogico {
        // https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
        try (Scanner scanner = abrirArchivo(JuegoConsts.CONFIG_CASILLAS)) {
            for (int nLinea = 1; scanner.hasNextLine(); nLinea++) {
                String linea = scanner.nextLine();

                // Se ignoran los comentarios y líneas en blanco
                if (ignorarLinea(linea)) {
                    continue;
                }

                // Se quitan los espacios y se separa por las comas
                String[] campos = linea.strip().replaceAll(" +", "").split(",");

                // Casillas especiales, acción e impuestos
                if (campos.length == 1) {
                    declaracionEspecial(campos, nLinea, banca);
                    continue;
                }

                if (campos[0].startsWith("grupo:") || campos[0].startsWith("Grupo:")) {
                    declaracionGrupo(campos, nLinea);
                    continue;
                }

                declaracionPropiedad(campos, banca, nLinea);
            }

            // Añadir los grupos de transportes y servicios al final si existen
            if (transportes != null) {
                grupos.add(transportes);
            }
            if (servicios != null) {
                grupos.add(servicios);
            }
        }
    }

    private void declaracionEspecial(String[] campos, int nLinea, Banca banca) throws ErrorFatalConfig {
        switch (campos[0]) {
            case "Salida", "salida" -> {
                salida = new CasillaSalida(casillas.size());
                casillas.add(salida);
            }
            case "Carcel", "carcel", "Cárcel", "cárcel" -> {
                carcel = new CasillaCarcel(casillas.size());
                casillas.add(carcel);
            }
            case "IrCarcel", "irCarcel", "IrCárcel", "irCárcel" -> {
                irCarcel = new CasillaIrCarcel(casillas.size());
                casillas.add(irCarcel);
            }
            case "Parking", "parking" -> {
                parking = new CasillaParking(casillas.size(), banca);
                casillas.add(parking);
            }
            case "Impuestos", "Impuesto", "impuestos", "impuesto" -> {
                CasillaImpuesto i = new CasillaImpuesto(casillas.size(), banca);
                impuestos.add(i);
                casillas.add(i);
            }
            case "Suerte", "suerte" -> casillas.add(new CasillaSuerte(casillas.size(), cartasSuerte));
            case "Comunidad", "comunidad" -> casillas.add(new CasillaComunidad(casillas.size(), cartasComunidad));
            default -> throw new ErrorFatalConfig("Casilla especial, acción o impuestos desconocida: " + campos[0], JuegoConsts.CONFIG_CASILLAS, nLinea);
        }
    }

    private void declaracionGrupo(String[] campos, int nLinea) throws ErrorFatalConfig {
        try {
            if (campos.length != 3) {
                throw new ErrorFatalConfig(
                        "Declaración de grupo incorrecta: se esperaban 3 parámetros, se recibieron %s".formatted(campos.length),
                        JuegoConsts.CONFIG_CASILLAS,
                        nLinea);
            }

            // Declaración de un Grupo: "grupo: Nombre, codColor"
            // Se quita la etiqueta del grupo, son 6 caracteres
            String nombre = campos[0].substring(6);
            int codigoColor = Integer.parseInt(campos[1]);

            switch (nombre) {
                case "Transporte", "transporte", "Transportes", "transportes" -> transportes = new Grupo(grupos.size(), nombre, codigoColor);
                case "Servicio", "servicio", "Servicios", "servicios" -> servicios = new Grupo(grupos.size(), nombre, codigoColor);
                default -> grupos.add(new Grupo(grupos.size(), nombre, codigoColor));
            }
        } catch (NumberFormatException e) {
            throw new ErrorFatalConfig("Número no válido: " + e, JuegoConsts.CONFIG_CASILLAS, nLinea);
        }
    }

    private void declaracionPropiedad(String[] campos, Jugador banca, int nLinea) throws ErrorFatalConfig, ErrorFatalLogico {
        // Declaración de un Transporte o Servicio: "Nombre, Grupo"
        switch (campos[1]) {
            case "T", "Transporte", "transporte", "Transportes", "transportes" -> {
                if (transportes == null) {
                    throw new ErrorFatalConfig("Transporte sin primero declarar un grupo de transportes", JuegoConsts.CONFIG_CASILLAS, nLinea);
                }

                Propiedad p = new Transporte(casillas.size(), transportes, campos[0], banca);
                transportes.anadirPropiedad(p);
                casillas.add(p);
                return;
            }
            case "S", "Servicio", "servicio", "Servicios", "servicios" -> {
                if (servicios == null) {
                    throw new ErrorFatalConfig("Servicio sin primero declarar un grupo de servicios", JuegoConsts.CONFIG_CASILLAS, nLinea);
                }

                Propiedad p = new Servicio(casillas.size(), servicios, campos[0], banca);
                servicios.anadirPropiedad(p);
                casillas.add(p);
                return;
            }
        }

        // Declaración de un Solar: "Nombre, numGrupo"
        try {
            int nGrupo = Integer.parseInt(campos[1]) + 1;

            if (nGrupo > grupos.size()) {
                throw new ErrorFatalConfig("Número de grupo demasiado grande", JuegoConsts.CONFIG_CASILLAS, nLinea);
            }

            Propiedad p = new Solar(casillas.size(), grupos.get(nGrupo), campos[0], banca);
            casillas.add(p);
            grupos.get(nGrupo).anadirPropiedad(p);
            nSolares++;
            sumaPrecioSolares += p.getPrecio();
        } catch (NumberFormatException e) {
            throw new ErrorFatalConfig("Número no válido: " + e, JuegoConsts.CONFIG_CASILLAS, nLinea);
        }
    }
}
