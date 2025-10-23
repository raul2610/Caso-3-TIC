public class ControlEstado {
    private final int totalClientes;
    private final int totalServidores;
    private int finesClientesRecibidos = 0;
    private boolean finEntregaEmitido = false;
    private boolean finCuarentenaEmitido = false;

    private BuzonEntrada entrada;
    private BuzonCuarentena cuarentena;

    public ControlEstado(Config cfg) {
        this.totalClientes = cfg.clientesEmisores;
        this.totalServidores = cfg.servidoresEntrega;
    }

    public void setBuzones(BuzonEntrada e, BuzonCuarentena q) {
        this.entrada = e;
        this.cuarentena = q;
    }

    public synchronized void registrarFinCliente() {
        finesClientesRecibidos++;
        System.out.println("FIN de cliente registrado. Total: " + finesClientesRecibidos + "/" + totalClientes);
    }

    public synchronized int getFinesClientesRecibidos() { return finesClientesRecibidos; }
    public synchronized int getTotalClientes() { return totalClientes; }
    public synchronized int getTotalServidores() { return totalServidores; }

    public synchronized boolean finEntregaEmitido() { return finEntregaEmitido; }
    public synchronized boolean finCuarentenaEmitido() { return finCuarentenaEmitido; }

    public synchronized boolean debeEmitirseFinEntrega() {
        if (finEntregaEmitido) return false;
        boolean condicion = finesClientesRecibidos >= totalClientes
                && entrada != null && cuarentena != null
                && entrada.estaVacio() && cuarentena.estaVacio();
        if (condicion) {
            finEntregaEmitido = true;
            System.out.println("FIN de entrega sera emitido por un filtro");
            return true;
        }
        return false;
    }

    public synchronized boolean debeEmitirseFinCuarentena() {
        if (finCuarentenaEmitido) return false;
        boolean condicion = finesClientesRecibidos >= totalClientes
                && entrada != null && cuarentena != null
                && entrada.estaVacio() && cuarentena.estaVacio();
        if (condicion) {
            finCuarentenaEmitido = true;
            System.out.println("FIN de cuarentena sera emitido por un filtro");
            return true;
        }
        return false;
    }

    public synchronized boolean puedeTerminarFiltro() {
        return finEntregaEmitido && finCuarentenaEmitido &&
               finesClientesRecibidos >= totalClientes &&
               entrada != null && entrada.estaVacio() &&
               cuarentena != null && cuarentena.estaVacio();
    }

    public synchronized boolean produccionTerminadaYVacios() {
        return finesClientesRecibidos >= totalClientes &&
                entrada != null && entrada.estaVacio() &&
                cuarentena != null && cuarentena.estaVacio();
    }

    public void despertarFiltros() {
        if (entrada == null) return;
        synchronized (entrada) {
            entrada.notifyAll();
        }
    }
}