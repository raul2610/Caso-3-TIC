public class BuzonCuarentena {
    private final java.util.List<Mensaje> lista = new java.util.LinkedList<>();

    public BuzonCuarentena() {
    }

    public boolean offerSemiactiva(Mensaje m) {
        synchronized (this) {
            lista.add(m);
        }
        return true;
    }

    public synchronized boolean estaVacio() { return lista.isEmpty(); }

    public ProcesamientoCuarentena procesarUnaVuelta() {
        java.util.List<Mensaje> paraEntrega = new java.util.LinkedList<>();
        boolean fin = false;
        synchronized (this) {
            java.util.ListIterator<Mensaje> it = lista.listIterator();
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
                if (m.cuarentenaContador > 0) m.cuarentenaContador -= 1000; // 1s por ciclo
                if (m.cuarentenaContador <= 0) {
                    it.remove();
                    paraEntrega.add(m);
                }
            }
        }
        return new ProcesamientoCuarentena(paraEntrega, fin);
    }

    public static class ProcesamientoCuarentena {
        public final java.util.List<Mensaje> paraEntrega;
        public final boolean fin;
        public ProcesamientoCuarentena(java.util.List<Mensaje> paraEntrega, boolean fin) {
            this.paraEntrega = paraEntrega;
            this.fin = fin;
        }
    }
}