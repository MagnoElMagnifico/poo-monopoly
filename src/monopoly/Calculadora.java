package monopoly.casillas;

import java.util.ArrayList;

import monopoly.casillas.Propiedad.Tipo;

public class Calculadora {
    private int sumaSolares;
    private int nSolares;

    public Calculadora(ArrayList<Casilla> casillas) {
        sumaSolares = 0;
        nSolares = 0;

        for (Casilla c : casillas) {
            if (c.isPropiedad() && c.getPropiedad().getTipo() == Tipo.Solar) {
                sumaSolares += calcularPrecio(c.getPropiedad());
                nSolares++;
            }
        }
    }

    public int calcularFortunaInicial() {
        return sumaSolares / 3;
    }

    public int calcularAbonoSalida() {
        return sumaSolares / nSolares;
    }

    public int calcularPrecio(Propiedad p) {
        Grupo g = p.getCasilla().getGrupo();
        int precioGrupo =  switch (p.getTipo()) {
            case Solar -> (int)(g.getNumeroSolar() * 0.3);
            case Transporte -> calcularAbonoSalida();
            case Servicio -> (int)(calcularAbonoSalida() * 0.75);
        };

        return precioGrupo / g.getNumeroCasillas();
    }

    /*
    public calcularPrecio(Edificacion e) {
        // TODO
    }
    */

    /*
    public int calcularPrecioHipoteca(Propiedad p) {

    }
    */

    /*
    public String valoresPropiedad(Propiedad solar){
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
