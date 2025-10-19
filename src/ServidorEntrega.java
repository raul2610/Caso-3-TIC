public class ServidorEntrega extends Thread {
    private final BuzonEntrega entrega;
    private boolean iniciado = false;

    public ServidorEntrega(String nombre, BuzonEntrega entrega) {
        super(nombre);
        this.entrega = entrega;
    }

    @Override
    public void run() {
        boolean terminar = false;
        while (!terminar) {
            Mensaje m = entrega.pollActiva(); // espera activa
            if (m == null) {
                Thread.yield();
                continue;
            }
            switch (m.tipo) {
                case INICIO:
                    iniciado = true;
                    log("INICIO recibido. Listo para procesar.");
                    break;
                case DATA:
                    // Si aún no ha comenzado, igual procesa para no bloquear el sistema
                    simularProcesamiento();
                    break;
                case FIN:
                    terminar = true;
                    log("FIN recibido. Terminando.");
                    break;
            }
        }
    }

    private void simularProcesamiento() {
        // Espera aleatoria corta para simular trabajo
        int t = 5 + (int)(Math.random() * 25);
        try { Thread.sleep(t); } catch (InterruptedException ignored) {}
    }

    private void log(String s) {
        System.out.println("[" + getName() + "] " + s);
    }
}

