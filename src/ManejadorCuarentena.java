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
        System.out.println(getName() + " iniciado");
        boolean fin = false;
        int ciclos = 0;

        while (!fin) {
            ciclos++;
            System.out.println(getName() + " - Ciclo " + ciclos + " (cada segundo)");

            BuzonCuarentena.ProcesamientoCuarentena proc = cuarentena.procesarUnaVuelta();

            for (Mensaje m : proc.paraEntrega) {
                System.out.println(getName() + " enviando mensaje liberado a entrega: " + m);
                while (!entrega.offerSemiactiva(m)) {
                    Thread.yield();
                }
            }

            if (proc.fin) {
                System.out.println(getName() + " recibiA3 FIN - terminando");
                fin = true;
            }
            if (control.produccionTerminadaYVacios()) {
                control.despertarFiltros();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println(getName() + " interrumpido");
                break;
            }
        }

        System.out.println(getName() + " finalizado despuAcs de " + ciclos + " ciclos");
    }
}

