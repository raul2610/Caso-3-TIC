public class FiltroSpam extends Thread {
    private final BuzonEntrada entrada;
    private final BuzonCuarentena cuarentena;
    private final BuzonEntrega entrega;
    private final ControlEstado control;

    public FiltroSpam(String nombre, BuzonEntrada entrada, BuzonCuarentena cuarentena,
                      BuzonEntrega entrega, ControlEstado control) {
        super(nombre);
        this.entrada = entrada;
        this.cuarentena = cuarentena;
        this.entrega = entrega;
        this.control = control;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Mensaje m = entrada.extraer(); // espera pasiva
                if (m == null) {
                    // No habrá más producción; intentar emitir FIN si aplica y salir
                    intentarEmitirFines();
                    break;
                }

                switch (m.tipo) {
                    case INICIO:
                        // INICIO nunca es spam, va a entrega (semiactiva)
                        while (!entrega.offerSemiactiva(m)) {
                            Thread.yield();
                        }
                        break;
                    case FIN:
                        // FIN de cliente: registrar; no se envía a entrega
                        control.registrarFinCliente();
                        intentarEmitirFines();
                        break;
                    case DATA:
                        if (m.spam) {
                            // Asignar contador 10000..20000
                            m.cuarentenaContador = 10000 + (int) (Math.random() * 10001);
                            cuarentena.offerSemiactiva(m);
                        } else {
                            while (!entrega.offerSemiactiva(m)) {
                                Thread.yield();
                            }
                        }
                        intentarEmitirFines();
                        break;
                }
            }
        } catch (InterruptedException e) {
            // salida silenciosa
        }
    }

    private void intentarEmitirFines() {
        if (control.debenEmitirseFines()) {
            // Enviar un único FIN a entrega y uno a cuarentena
            // Ambos en semiactiva
            while (!entrega.offerSemiactiva(Mensaje.finSistema())) {
                Thread.yield();
            }
            cuarentena.offerSemiactiva(Mensaje.finSistema());
            // Despertar posibles filtros bloqueados en entrada
            synchronized (entrada) {
                entrada.notifyAll();
            }
        }
    }
}

