// Este ejemplo es de _Java Examples in a Nutshell_. (http://www.oreilly.com)
// Copyright (c) 1997 por David Flanagan
// Este ejemplo se proporciona SIN NINGUNA GARANTÍA, ya sea expresa o implícita.
// Puede estudiarlo, utilizarlo, modificarlo y distribuirlo con fines no comerciales.
// Para cualquier uso comercial, consulte http://www.davidflanagan.com/javaexamples

/ * CPG * / // Fuente: https://resources.oreilly.com/examples/9781565923713/blob/master/SimpleProxyServer.java
/ * CPG * / // Modificado por Carlos Pineda G. 2020

importar java.io. *;
importar java.net. *;

/ **
 * Esta clase implementa un servidor proxy simple de un solo subproceso.
 ** /
public class SimpleProxyServer {
  / ** El método principal analiza los argumentos y los pasa a runServer * /
  public static void main (String [] args) lanza IOException {
    tratar {
      // Verifica la cantidad de argumentos
      / * CPG * / // argumentos: host-remoto puerto-remoto puerto-local puerto-server2
      / * CPG * / if (args.length! = 3) 
      si (args.length! = 4) 
        lanzar nueva IllegalArgumentException ("Número incorrecto de argumentos");

      // Obtener los argumentos de la línea de comandos: el host y el puerto para los que somos proxy
      // y el puerto local en el que escuchamos las conexiones
      Cadena host = args [0];
      int puerto remoto = Integer.parseInt (args [1]);
      int localport = Integer.parseInt (args [2]);
      / * CPG * / int localport2 = Integer.parseInt (args [3]); // puerto del server2
      // Imprime un mensaje de inicio
      System.out.println ("Proxy de inicio para" + host + ":" + puerto remoto +
                         "en el puerto" + puerto local);
      // Y empieza a ejecutar el servidor
      / * CPG * / // runServer (host, puerto remoto, puerto local); // nunca regresa
      runServer (host, puerto remoto, puerto local, puerto local2); // nunca regresa
    } 
    captura (Excepción e) {
      System.err.println (e);
      System.err.println ("Uso: java SimpleProxyServer" +
                         / * CPG * / // "<host> <remoteport> <localport>");
                         "<host-remoto> <puertoremoto> <puertolocal> <puerto-servidor2>");
    }
  }

  / **
   * Este método ejecuta un servidor proxy de un solo subproceso para 
   * host: puerto remoto en el puerto local especificado. Nunca regresa.
   ** /
  / * CPG * / // public static void runServer (String host, int remoteport, int localport) 
  public static void runServer (String host, int remoteport, int localport, int localport2) 
       lanza IOException {
    // Crea un ServerSocket para escuchar conexiones con
    ServerSocket ss = nuevo ServerSocket (puerto local);

    // Cree búferes para la comunicación de cliente a servidor y de servidor a cliente.
    // Hacemos una final para que se pueda usar en una clase anónima a continuación.
    // Tenga en cuenta las suposiciones sobre el volumen de tráfico en cada dirección ...
    byte final [] solicitud = nuevo byte [1024];
    byte [] respuesta = nuevo byte [4096];
    
    // Este es un servidor que nunca regresa, así que ingrese un ciclo infinito.
    while (verdadero) { 
      // Variables para sujetar los sockets al cliente y al servidor.
      Socket cliente = nulo, servidor = nulo;
      / * CPG * / Socket server2 = null;
      tratar {
        // Espere una conexión en el puerto local
        cliente = ss.accept ();
        
        // Obtener flujos de clientes. Hazlos definitivos para que puedan
        // se utilizará en el hilo anónimo a continuación.
        InputStream final from_client = client.getInputStream ();
        OutputStream final to_client = client.getOutputStream ();

        // Establece una conexión con el servidor real
        // Si no podemos conectarnos al servidor, enviamos un error al 
        // cliente, desconecte, luego continúe esperando otra conexión.
        intente {servidor = nuevo Socket (host, puerto remoto); 
	/ * CPG * / // en este caso el puerto del servidor2 se define como el puerto local mas uno
	/ * CPG * / server2 = new Socket ("localhost", localport2);
	} 
        catch (IOException e) {
          PrintWriter out = new PrintWriter (nuevo OutputStreamWriter (to_client));
          out.println ("El servidor proxy no se puede conectar a" + host + ":" +
                      puerto remoto + ": \ n" + e);
          out.flush ();
          cliente.close ();
          Seguir;
        }

        // Obtiene las transmisiones del servidor.
        InputStream final from_server = server.getInputStream ();
        OutputStream final to_server = server.getOutputStream ();
	/ * CPG * / final InputStream from_server2 = server2.getInputStream ();
	/ * CPG * / OutputStream final a_servidor2 = servidor2.getOutputStream ();

        // Cree un hilo para leer las solicitudes del cliente y pasarlas al 
        // servidor. Tenemos que usar un hilo separado porque las solicitudes y
        // las respuestas pueden ser asincrónicas.
        Hilo t = nuevo Hilo () {
          public void run () {
            int bytes_read;
            tratar {
              while ((bytes_read = from_client.read (solicitud))! = -1) {
                to_server.write (solicitud, 0, bytes_read);
                to_server.flush ();
		/ * CPG * / to_server2.write (solicitud, 0, bytes_read);
		/ * CPG * / to_server2.flush ();
              }
            }
            catch (IOException e) {}

            // el cliente nos cerró la conexión, así que cierre nuestro 
            // conexión al servidor. Esto también hará que
            // bucle de servidor a cliente en la salida del hilo principal.
            intente {to_server.close ();
		/ * CPG * / to_server2.close ();
	    } captura (IOException e) {}
          }
        };

        // Iniciar la ejecución del hilo de solicitud de cliente a servidor
        t.start ();  

        // Mientras tanto, en el hilo principal, lee las respuestas del servidor
        // y devolverlos al cliente. Esto se hará en
        // en paralelo con el hilo de solicitud de cliente a servidor anterior.
        int bytes_read;
	/ * CPG * / 
	// la respuesta del server (remoto) no se necesita por tanto se ignora
	/ *	
        tratar {
          while ((bytes_read = from_server.read (respuesta))! = -1) {
            to_client.write (respuesta, 0, bytes_read);
            to_client.flush ();
          }
        }
        catch (IOException e) {}
 	* /	
	// se envia al cliente la respuesta del server2 (local)
        tratar {
          while ((bytes_read = from_server2.read (respuesta))! = -1) {
            to_client.write (respuesta, 0, bytes_read);
            to_client.flush ();
          }
        }
        catch (IOException e) {}

        // El servidor cerró su conexión con nosotros, así que cierre nuestro 
        // conexión con nuestro cliente. Esto hará que el otro hilo salga.
        to_client.close ();
      }
      catch (IOException e) {System.err.println (e); }
      // Cierre los sockets sin importar lo que pase cada vez que pase por el bucle.
      finalmente { 
        tratar { 
          si (servidor! = nulo) servidor.close (); 
	  / * CPG * / if (servidor2! = Nulo) servidor.close ();
          if (cliente! = nulo) cliente.close (); 
        }
        catch (IOException e) {}
      }
    }
  }
}