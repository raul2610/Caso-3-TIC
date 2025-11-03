public class ServidorEntrega extends Thread {
    private final BuzonEntrega entrega;
    private int procesados = 0;

    public ServidorEntrega(String nombre, BuzonEntrega entrega) {
        super(nombre);
        this.entrega = entrega;
    }

    @Override
    public void run() {
        System.out.println("[" + getName() + "] Servidor iniciado - esperando mensaje INICIO");
        boolean terminar = false;
        while (!terminar) {
            Mensaje m;
            // Espera activa: giramos hasta que haya un mensaje disponible.
            while ((m = entrega.pollActiva()) == null) {
            }
            switch (m.tipo) {
                case INICIO:
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