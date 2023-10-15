package monopoly;

import monopoly.jugadores.Jugador;

import java.util.ArrayList;

import monopoly.casillas.Casilla;
import monopoly.casillas.Grupo;
import monopoly.casillas.Propiedad;
import monopoly.casillas.Propiedad.Tipo;
import monopoly.utilidades.Formatear;
import monopoly.utilidades.Formatear.Color;

public class Calculadora {
    public static final long PRECIO_GRUPO1 = 100000;
    private int sumaSolares;
    private int nSolares;
    private Jugador banca;

    public Calculadora(ArrayList<Casilla> casillas, Jugador banca) {
        this.banca = banca;
        sumaSolares = 0;
        nSolares = 0;

        for (Casilla c : casillas) {
            if (c.isPropiedad()) {
                // Asignar propiedades a la Banca
                Propiedad p = c.getPropiedad();
                banca.anadirPropiedad(p);

                // Contar los solares y su precio total
                if (p.getTipo() == Tipo.Solar) {
                    sumaSolares += calcularPrecio(c.getPropiedad());
                    nSolares++;
                }
            }
        }

        // Ahora hay que asignar los precios a cada casilla
        for (Casilla c : casillas) {
            c.setPrecio(calcularPrecio(c));
        }
    }

    public long calcularFortunaInicial() {
        return sumaSolares / 3;
    }

    public long calcularAbonoSalida() {
        return sumaSolares / nSolares;
    }

    public long calcularFortuna() {
        return sumaSolares / 3;
    }

    public long calcularPrecio(Casilla c) {
        if (c.isPropiedad()) {
            return calcularPrecio(c.getPropiedad());
        }

        return switch (c.getNombre()) {
            case "Salida" -> calcularAbonoSalida(); // Cantidad que recibe al pasar por Salida
            case "Impuesto1" -> calcularAbonoSalida();
            case "Impuesto2" -> calcularAbonoSalida() / 2;
            case "Parking" -> banca.getFortuna(); // La fortuna de la banca representa este bote
            case "Cárcel" -> calcularAbonoSalida() / 4;
            default -> -1;
        };
    }

    public long calcularPrecio(Propiedad p) {
        Grupo g = p.getCasilla().getGrupo();
        long precioGrupo =  switch (p.getTipo()) {
            case Solar -> (long)(0.3 * g.getNumeroSolar() * PRECIO_GRUPO1 + PRECIO_GRUPO1);
            case Transporte -> calcularAbonoSalida();
            case Servicio -> (long)(0.75 * calcularAbonoSalida());
        };

        return precioGrupo / g.getNumeroCasillas();
    }
    // TODO: calcularPrecio(Edificacion)
    // TODO: calcularHipoteca(Propiedad)

    public static long calcularAlquiler(Propiedad p) {
        return p.getPrecio() / 10;
    }

    public String comprar(Propiedad solar, Jugador jugador){
        if(solar.getPrecio() > jugador.getFortuna()) {
            return Formatear.con("%s no dispone de suficiente dinero para comprar %s\n"
                    .formatted(jugador.getNombre(), solar.getCasilla().getNombre()), Color.Rojo);
        }

        jugador.cobrar(solar.getPrecio());
        jugador.anadirPropiedad(solar);
        banca.quitarPropiedad(solar);
        solar.setPropietario(jugador);

        return "El jugador %s ha comprado la casilla %s por %s\n"
                .formatted(jugador.getNombre(), Formatear.casillaNombre(solar.getCasilla()), Formatear.num(solar.getPrecio()));
    }

    public String pagarAlquiler(Propiedad solar, Jugador jugador){
        if (solar.getPropietario() == banca) return "";
        if (solar.getPropietario() == jugador) return "";

        jugador.cobrar(solar.getAlquiler());
        return "Se han pagado %s de alquiler".formatted(Formatear.num(solar.getAlquiler()));
    }

    /*
    No es para esta entrega:
    public static String valoresPropiedad(Propiedad solar){
        return """
                        Nombre:%s
                        propietario: %s
                        Precio: %d
                        alquiler: %d
                        valor hotel: %d
                        valor casa: %d
                        valor piscina: %d
                        valor pista de deporte: %d
                        alquiler una casa: %d
                        alquiler dos casas: %d
                        alquiler tres casas: %d
                        alquiler hotel: %d
                        alquiler piscina: %d
                        alquiler pista de deporte: %d
                """.formatted(solar.getCasilla().getNombre(),solar.getPropietario(),solar.getPrecio(),solar.getAlquiler(),
                (int)(solar.getPrecio()*0.6),(int)(solar.getPrecio()*0.6),(int)(solar.getPrecio()*0.4),(int)(solar.getPrecio()*1.25),
                solar.getPrecio()*5, solar.getPrecio()*15,solar.getPrecio()*30,solar.getPrecio()*70,solar.getPrecio()*25,solar.getPrecio()*25);
    }
    */
}
