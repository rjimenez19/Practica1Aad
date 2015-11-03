package dam.practica1aad.datos;

import java.util.Comparator;

public class OrdenarLista implements Comparator<Contacto> {
    @Override
    public int compare(Contacto c1, Contacto c2) {
        if(c1.getNombre().compareToIgnoreCase(c2.getNombre())>0)
            return 1;
        if(c1.getNombre().compareToIgnoreCase(c2.getNombre())<0)
            return -1;
        return 0;
    }
}
