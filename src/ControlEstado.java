/**
 * Monitor de estado global para coordinación de FIN y terminaciones.
 */
public class ControlEstado {
    private final int totalClientes;
    private final int totalServidores;
    private int finesClientesRecibidos = 0;
    private boolean finEntregaEmitido = false;
    private boolean finCuarentenaEmitido = false;

    // Referencias para consultas de vacíos
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
    }

    public synchronized int getFinesClientesRecibidos() { return finesClientesRecibidos; }
    public synchronized int getTotalClientes() { return totalClientes; }
    public synchronized int getTotalServidores() { return totalServidores; }

    public synchronized boolean finEntregaEmitido() { return finEntregaEmitido; }
    public synchronized boolean finCuarentenaEmitido() { return finCuarentenaEmitido; }

    /**
     * Verifica si se cumplen las condiciones para emitir FIN al buzón de entrega y a la cuarentena.
     * No realiza los envíos; solo marca y devuelve si el hilo que invoca debe emitirlos.
     */
    public synchronized boolean debenEmitirseFines() {
        if (finEntregaEmitido || finCuarentenaEmitido) return false;
        boolean condicion = finesClientesRecibidos >= totalClientes
                && entrada != null && cuarentena != null
                && entrada.estaVacio() && cuarentena.estaVacio();
        if (condicion) {
            // Reservar para que SOLO un filtro los emita
            finEntregaEmitido = true;
            finCuarentenaEmitido = true;
            return true;
        }
        return false;
    }

    // Usado por Entrada para saber si ya no habrá más mensajes
    public synchronized boolean produccionTerminadaYVacios() {
        return finesClientesRecibidos >= totalClientes &&
                entrada != null && entrada.estaVacio() &&
                cuarentena != null && cuarentena.estaVacio();
    }
}

