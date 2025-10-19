public class ServidorEntrega extends Thread {
    private final BuzonEntrega entrega;
    private boolean iniciado = false;
    private int mensajesProcesados = 0;

    public ServidorEntrega(String nombre, BuzonEntrega entrega) {
        super(nombre);
        this.entrega = entrega;
    }

    @Override
    public void run() {
        log("Servidor iniciado - esperando mensaje INICIO");
        boolean terminar = false;
        int intentosActivos = 0;
        
        while (!terminar) {
            Mensaje m = entrega.pollActiva(); // espera activa
            intentosActivos++;
            
            if (m == null) {
                // Espera activa
                Thread.yield();
                continue;
            }
            intentosActivos = 0;
            
            switch (m.tipo) {
                case INICIO:
                    iniciado = true;
                    log("INICIO recibido. Listo para procesar mensajes.");
                    break;
                case DATA:
                    if (iniciado) {
                        mensajesProcesados++;
                        log("Procesando mensaje DATA #" + mensajesProcesados + ": " + m);
                        simularProcesamiento();
                    } else {
                        log("Mensaje DATA recibido antes de INICIO - procesando de todas formas: " + m);
                        mensajesProcesados++;
                        simularProcesamiento();
                    }
                    break;
                case FIN:
                    terminar = true;
                    log("FIN recibido. Terminando después de procesar " + mensajesProcesados + " mensajes.");
                    break;
            }
        }
        log("Servidor finalizado");
    }

    private void simularProcesamiento() {
        int t = 10 + (int)(Math.random() * 50); // 10-60ms
        try { 
            Thread.sleep(t); 
        } catch (InterruptedException e) {
            log("Interrumpido durante procesamiento");
        }
    }

    private void log(String s) {
        System.out.println("[" + getName() + "] " + s);
    }
}