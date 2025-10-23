public class ServidorEntrega extends Thread {
    private final BuzonEntrega entrega;
    @SuppressWarnings("unused")
    private boolean iniciado = false;
    private int procesados = 0;

    public ServidorEntrega(String nombre, BuzonEntrega entrega) {
        super(nombre);
        this.entrega = entrega;
    }

    @Override
    public void run() {
        boolean terminar = false;
        while (!terminar) {
            Mensaje m = entrega.pollActiva();
            if (m == null) {
                Thread.yield();
                continue;
            }
            switch (m.tipo) {
                case INICIO:
                    iniciado = true;
                    System.out.println("[" + getName() + "] Servidor iniciado - esperando mensaje INICIO");
                    System.out.println("[" + getName() + "] INICIO recibido. Listo para procesar mensajes.");
                    break;
                case DATA:
                    procesados++;
                    simularProcesamiento();
                    break;
                case FIN:
                    terminar = true;
                    System.out.println("[" + getName() + "] FIN recibido. Terminando despues de procesar " + procesados + " mensajes.");
                    break;
            }
        }
        System.out.println("[" + getName() + "] Servidor finalizado");
    }

    private void simularProcesamiento() {
        int t = 5 + (int)(Math.random() * 25);
        try { Thread.sleep(t); } catch (InterruptedException ignored) {}
    }
}