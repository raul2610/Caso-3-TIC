# Caso 3 - Simulador de Centro de Mensajeria (Java)

Autor: Equipo (login1, login2)

## Diseno y clases

Diagrama de alto nivel (ASCII):

```
Clientes --> [BuzonEntrada cap=L, wait/notify] --> FiltrosSpam
FiltrosSpam --(DATA ok, semiactiva)--> [BuzonEntrega cap=K] --espera activa--> Servidores
FiltrosSpam --(DATA spam, semiactiva + t[10000..20000])--> [BuzonCuarentena ilimitado]
ManejadorCuarentena (cada 1s) revisa cuarentena, descarta (1..21 %7==0) y libera a entrega
ControlEstado: monitor que decide emision unica de FIN y coordina terminacion
```

Clases principales:

- `Mensaje`: contiene tipo (`INICIO`, `DATA`, `FIN`), id global secuencial, id de cliente, secuencial por cliente, flag spam y contador de cuarentena (ms).
- `BuzonEntrada`: cola limitada; productores y filtros usan espera pasiva (`synchronized` + `wait/notifyAll`).
- `BuzonCuarentena`: lista ilimitada; insercion semiactiva; `procesarUnaVuelta()` recorre mensajes, descuenta 1000 ms por ciclo y acumula los que pasan a entrega.
- `BuzonEntrega`: cola limitada con insercion semiactiva; lectura en espera activa (`pollActiva`). Al recibir FIN arma un broadcast interno (copia por servidor) sin dejar residuos en la cola.
- `ClienteEmisor`: genera secuencia fija de mensajes definida en `config.txt` (`INICIO`, N `DATA`, `FIN`).
- `FiltroSpam`: consume en pasiva; DATA spam va a cuarentena con tiempo aleatorio [10000..20000] ms, DATA valida va a entrega; registra FIN de cliente y decide la emision unica de FIN (entrega/cuarentena) cuando entrada y cuarentena estan vacias.
- `ManejadorCuarentena`: semiactivo (ciclo cada 1 s); descuenta contadores, descarta maliciosos y libera mensajes listos hacia entrega; termina con FIN.
- `ServidorEntrega`: espera activa; procesa DATA con retardo aleatorio corto, finaliza al recibir su copia de FIN.
- `ControlEstado`: monitor que guarda conteo de FIN de clientes y decide cuando emitir el FIN del sistema, ademas de exponer utilidades para despertar filtros.

## Sincronizacion por pareja

- Cliente -> BuzonEntrada: espera pasiva; al depositar notifica (`notifyAll`) a filtros.
- Filtro -> BuzonEntrada: `extraer()` espera pasiva hasta obtener mensaje o deteccion de cierre.
- Filtro -> BuzonCuarentena: semiactiva; siempre puede insertar.
- Filtro -> BuzonEntrega: semiactiva con `Thread.yield()` si esta lleno.
- Manejador -> BuzonCuarentena: seccion critica sincronizada, recorre lista en cada ciclo.
- Servidor -> BuzonEntrega: espera activa; recibe copias de FIN desde el broadcast interno.
- Filtros <-> ControlEstado: metodos sincronizados para actualizar contadores y evaluar condiciones de cierre.

## Puntos clave del funcionamiento

- **Mensajes por cliente**: son fijos y se obtienen exclusivamente del archivo de configuracion. Si falta la clave o es invalida, se usa el valor por defecto (20).
- **Emision de FIN del sistema**: exactamente un filtro emite FIN cuando se cumplen las condiciones: todos los FIN de cliente recibidos y tanto entrada como cuarentena estan vacios. Se publican dos mensajes FIN: uno al buzon de entrega (que reparte copias a cada servidor) y otro a la cuarentena para cerrar el manejador.
- **Cuarentena**: contadores se interpretan como milisegundos (10-20 segundos). En cada ciclo se resta 1000 ms, se descartan los mensajes maliciosos (multiplo de 7) y se liberan los que llegan a 0.
- **Terminacion limpia**: al finalizar, los tres buzones quedan vacios, todos los servidores reciben FIN y los hilos completan correctamente (`join()` en `App`).

## Validacion realizada

Pruebas manuales en PowerShell:

1. Configuracion por defecto (`config.txt`): la simulacion termina en ~18-20 segundos, todos los buzones quedan vacios y el resumen marca terminacion limpia.
2. Cambio de parametros (capacidades peque??as y mayor numero de clientes): se verifica que los clientes esperan pasivamente cuando el buzon de entrada se llena y que los filtros liberan correctamente tras recibir FIN.
3. Verificacion de FIN distribuido: los logs del buzon de entrega muestran la distribucion de FIN a cada servidor y la bandera `finCompletamenteDistribuido` se imprime como `true`.
4. Cuarentena activa: se observan mensajes descartados por el multiplicador de 7 y otros liberados cuando su contador llega a 0.

## Ejecucion

```
javac -d bin src/*.java
java -cp bin App config.txt
```

### Ejemplo de `config.txt`

```
clientes=3
mensajesPorCliente=10
filtros=3
servidores=3
capacidadEntrada=15
capacidadEntrega=10
```

Todos los valores son obligatorios para fijar el comportamiento, pero si se omite alguno se usan los defaults indicados en `Config.defaults()`.