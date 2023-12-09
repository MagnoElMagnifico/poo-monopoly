package monopoly.utils;

import monopoly.error.ErrorComandoNoEncontrado;

import java.util.Collection;
import java.util.function.Function;

public interface Buscar {
    String getNombre();

    static <T extends Buscar> T porNombre(String query, Collection<T> elementos) throws ErrorComandoNoEncontrado {
        for (T e : elementos) {
            String nombre = e.getNombre();

            if (nombre == null) {
                continue;
            }

            if (nombre.equalsIgnoreCase(query)) {
                return e;
            }
        }

        throw new ErrorComandoNoEncontrado("\"%s\": no se ha encontrado".formatted(query));
    }

    private <T> T busquedaGenerica(Collection<T> elementos, Function<T, Boolean> funcion) throws ErrorComandoNoEncontrado {
        for (T e : elementos) {
            if (funcion.apply(e)) {
                return e;
            }
        }

        throw new ErrorComandoNoEncontrado("No encontrado");
    }
}
