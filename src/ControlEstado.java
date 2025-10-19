public class ControlEstado {
    private final int totalClientes;
    private final int totalServidores;
    private int finesClientesRecibidos = 0;
    private int finesFiltrosRecibidos = 0;
    private boolean finEntregaEmitido = false;
    private boolean finCuarentenaEmitido = false;
    private boolean sistemaTerminado = false;

    private BuzonEntrada entrada;
    private BuzonCuarentena cuarentena;
    private BuzonEntrega entrega;

    public ControlEstado(Config cfg) {
        this.totalClientes = cfg.clientesEmisores;
        this.totalServidores = cfg.servidoresEntrega;
    }

    public void setBuzones(BuzonEntrada e, BuzonCuarentena q, BuzonEntrega d) {
        this.entrada = e; this.cuarentena = q; this.entrega = d;
    }

    public synchronized void registrarFinCliente() {
        finesClientesRecibidos++;
        System.out.println("FIN de cliente registrado. Total: " + finesClientesRecibidos + "/" + totalClientes);
    }

    public synchronized void registrarFinFiltro() {
        finesFiltrosRecibidos++;
        System.out.println("FIN de filtro registrado. Total: " + finesFiltrosRecibidos + "/" + totalClientes);
    }

    public synchronized int getFinesClientesRecibidos() { return finesClientesRecibidos; }
    public synchronized int getFinesFiltrosRecibidos() { return finesFiltrosRecibidos; }
    public synchronized int getTotalClientes() { return totalClientes; }
    public synchronized int getTotalServidores() { return totalServidores; }

    public synchronized boolean finEntregaEmitido() { return finEntregaEmitido; }
    public synchronized boolean finCuarentenaEmitido() { return finCuarentenaEmitido; }
    public synchronized boolean sistemaTerminado() { return sistemaTerminado; }

    public synchronized boolean debeEmitirseFinEntrega() {
        if (finEntregaEmitido) return false;
        
        boolean condicion = finesClientesRecibidos >= totalClientes
                && entrada != null && cuarentena != null
                && entrada.estaVacio() && cuarentena.estaVacio();
        
        if (condicion) {
            finEntregaEmitido = true;
            System.out.println("FIN de entrega será emitido por un filtro");
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
            System.out.println("FIN de cuarentena será emitido por un filtro");
            return true;
        }
        return false;
    }

    public synchronized boolean puedeTerminarFiltro() {
        return finesFiltrosRecibidos >= totalClientes && 
               finEntregaEmitido && finCuarentenaEmitido;
    }

    public synchronized boolean produccionTerminadaYVacios() {
        return finesClientesRecibidos >= totalClientes &&
                entrada != null && entrada.estaVacio() &&
                cuarentena != null && cuarentena.estaVacio();
    }

    public synchronized void marcarSistemaTerminado() {
        sistemaTerminado = true;
        System.out.println("Sistema marcado como terminado");
    }

    public void despertarFiltros() {
        if (entrada == null) return;
        synchronized (entrada) {
            entrada.notifyAll();
        }
    }
}