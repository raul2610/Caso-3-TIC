import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class BuzonCuarentena {
    private final List<Mensaje> lista = new LinkedList<>();
    private final ControlEstado control;

    public BuzonCuarentena(ControlEstado control) {
        this.control = control;
    }

    public boolean offerSemiactiva(Mensaje m) {
        synchronized (this) {
            lista.add(m);
        }
        return true;
    }

    public synchronized boolean estaVacio() { return lista.isEmpty(); }

    public ProcesamientoCuarentena procesarUnaVuelta() {
        List<Mensaje> paraEntrega = new LinkedList<>();
        boolean fin = false;
        synchronized (this) {
            ListIterator<Mensaje> it = lista.listIterator();
            while (it.hasNext()) {
                Mensaje m = it.next();
                if (m.tipo == Mensaje.Tipo.FIN) {
                    fin = true;
                    it.remove();
                    continue;
                }
                int r = 1 + (int) (Math.random() * 21);
                if (r % 7 == 0) {
                    it.remove();
                    continue;
                }
                if (m.cuarentenaContador > 0) m.cuarentenaContador -= 1000; // interpretado como ms
                if (m.cuarentenaContador <= 0) {
                    it.remove();
                    paraEntrega.add(m);
                }
            }
        }
        return new ProcesamientoCuarentena(paraEntrega, fin);
    }

    public static class ProcesamientoCuarentena {
        public final List<Mensaje> paraEntrega;
        public final boolean fin;
        public ProcesamientoCuarentena(List<Mensaje> paraEntrega, boolean fin) {
            this.paraEntrega = paraEntrega;
            this.fin = fin;
        }
    }
}