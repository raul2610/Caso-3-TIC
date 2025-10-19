public class ManejadorCuarentena extends Thread {
    private final BuzonCuarentena cuarentena;
    private final BuzonEntrega entrega;
    private final ControlEstado control;

    public ManejadorCuarentena(BuzonCuarentena cuarentena, BuzonEntrega entrega, ControlEstado control) {
        super("ManejadorCuarentena");
        this.cuarentena = cuarentena;
        this.entrega = entrega;
        this.control = control;
    }

    @Override
    public void run() {
        boolean fin = false;
        while (!fin) {
            // Procesamiento semiactivo: cada segundo
            BuzonCuarentena.ProcesamientoCuarentena proc = cuarentena.procesarUnaVuelta();
            for (Mensaje m : proc.paraEntrega) {
                while (!entrega.offerSemiactiva(m)) {
                    Thread.yield();
                }
            }
            if (proc.fin) fin = true;

            try { Thread.sleep(1000); } catch (InterruptedException e) { break; }
        }
    }
}

