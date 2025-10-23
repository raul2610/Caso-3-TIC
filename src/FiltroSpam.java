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
            System.out.println(getName() + " iniciado");

            while (true) {
                if (control.puedeTerminarFiltro()) {
                    System.out.println(getName() + " terminando - condiciones cumplidas");
                    break;
                }

                Mensaje m = entrada.extraer(); // espera pasiva
                if (m == null) {
                    intentarEmitirFines();
                    break;
                }

                switch (m.tipo) {
                    case INICIO:
                        System.out.println(getName() + " procesando INICIO de " + m.clienteId);
                        while (!entrega.offerSemiactiva(m)) {
                            Thread.yield();
                        }
                        break;
                    case FIN:
                        control.registrarFinCliente();
                        intentarEmitirFines();
                        break;
                    case DATA:
                        if (m.spam) {
                            m.cuarentenaContador = 10000 + (int) (Math.random() * 10001);
                            System.out.println(getName() + " enviando SPAM a cuarentena: " + m +
                                             " (tiempo: " + m.cuarentenaContador + ")");
                            cuarentena.offerSemiactiva(m);
                        } else {
                            System.out.println(getName() + " enviando mensaje valido a entrega: " + m);
                            while (!entrega.offerSemiactiva(m)) {
                                Thread.yield();
                            }
                        }
                        break;
                }
            }
        } catch (InterruptedException e) {
            System.out.println(getName() + " interrumpido");
        }

        System.out.println(getName() + " finalizado");
    }

    private void intentarEmitirFines() {
        if (control.debeEmitirseFinEntrega()) {
            System.out.println(getName() + " emitiendo FIN a buzon de entrega");
            while (!entrega.offerSemiactiva(Mensaje.finSistema())) {
                Thread.yield();
            }
        }

        if (control.debeEmitirseFinCuarentena()) {
            System.out.println(getName() + " emitiendo FIN a buzon de cuarentena");
            cuarentena.offerSemiactiva(Mensaje.finSistema());
        }

        synchronized (entrada) {
            entrada.notifyAll();
        }
    }
}