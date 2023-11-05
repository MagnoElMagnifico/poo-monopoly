package monopoly.casillas;

import monopoly.utilidades.Consola;

public class Carta {
    private final int id;
    private final TipoCarta tipo;
    private final String descripcion;

    public Carta(int id, TipoCarta tipo, String descripcion) {
        // Comprobación del ID
        this.id = id;
        this.tipo = tipo;
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        // TODO?: Mejorar presentación de la carta
        return "%s: %s\n".formatted(Consola.fmt(tipo == TipoCarta.Suerte ? "Carta de Suerte" : "Carta de Comunidad", Consola.Color.Azul), descripcion);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Carta carta)) {
            return false;
        }

        return carta.id == this.id && carta.tipo == this.tipo;
    }

    public void accionCarta() {
        switch (tipo) {
            case Suerte -> accionSuerte();
            case Comunidad -> accionComunidad();
        }
    }

    private void accionSuerte() {
        // TODO: implementar acción para cada tipo de carta
        switch (id) {
            case 1 -> System.out.printf("TODO: acción de carta %d con descripción %s\n", id, descripcion);
            default -> Consola.error("[Carta] ID no soportado para realizar acción");
        }
    }

    private void accionComunidad() {
        // TODO: implementar acción para cada tipo de carta
        switch (id) {
            case 1 -> System.out.println(descripcion);
            default -> Consola.error("[Carta] ID no soportado para realizar acción");
        }
    }

    public enum TipoCarta {
        Suerte, Comunidad
    }
}
