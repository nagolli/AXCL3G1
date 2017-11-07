/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axc.g1l3.cliente;

import java.io.DataInputStream;
import java.io.IOException;
import static java.lang.Math.random;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ignacio
 */
public class HiloCliente extends Thread
{

    //private final INFO info;              ///< Esta clase guardará la información del cliente en cuestion
    //private Vector<INFO> ubi;
    int puerto, tamGrupo, id, iteraciones, numGrupo;
    InetAddress ip;
    Socket TCP;
    DatagramSocket UDP;
    DataInputStream recibir_datos;
    int x, y;
    final int maxX = 500;
    final int maxY = 500;
    final int minX = 0;
    final int minY = 0;
    long iniTime, finTime, totTime, total = 0, media;
    ArrayList<ArrayList<Integer>> Posiciones;
    private Cliente padre;
    private int iteracion;
    CyclicBarrier barrera;
    int contador;

    /**
     * Constructor de la clase HiloCliente con parametros.
     * 
     * @param ip            Ip del hilo creado.
     * @param puerto        Puerto del hilo creado.
     * @param padre         Padre del hilo creado.
     * @param barrera       Barrera que usaremos para lanzar multiples hilos.
     */
    public HiloCliente(InetAddress ip, int puerto, Cliente padre, CyclicBarrier barrera)
    {
        this.puerto = puerto;
        this.ip = ip;
        total = 0;
        this.padre = padre;
        Posiciones = new ArrayList();
        iteracion = -1;
        this.barrera = barrera;
    }

    /**
     * 
     */
    @Override
    public void run()
    {
        String mensaje;
        /* DatagramPacket --> Permite crear instancias de un array de bytes, 
        *  que agrupa el mensaje, la longitud del mensaje, la dirección Internet
        *  y el puerto local del socket de destino.
        */
        DatagramPacket paqueteEnviar;
        
        byte[] mensajeEnBytes;
        DatagramPacket paqueteRecibido;
        /*
        * Intentamos realizar una conexión, con la función booleana conectar(),
        * si se consigue conectar, se realiza 
        */
        try {
            if (conectar()) {
                //Configuracion por TCP
                //System.out.println("Conectado y configurando");
                // Es útil para leer datos del tipo primitivo de una forma portable.
                recibir_datos = new DataInputStream(TCP.getInputStream());
                /*readUTF() --> Lee una cadena que ha sido codificada usando un 
                * formato UTF-8 modificado. La cadena de caracteres se decodifica
                * desde el UTF y se devuelve como String.
                */
                mensaje = recibir_datos.readUTF();
                id = procesarMensaje(mensaje, 1);
                numGrupo = procesarMensaje(mensaje, 2);
                tamGrupo = procesarMensaje(mensaje, 3);
                iteraciones = procesarMensaje(mensaje, 4);
                //Cerramos las conexiones despues de procesar los mensajes.
                recibir_datos.close();
                TCP.close();
                //Bucle para añadir las posiciones al array, dependiendo del 
                //tamaño del grupo
                for (int i = 0; i < tamGrupo; i++) {
                    Posiciones.add(new ArrayList()
                    {
                        {
                            add(0);
                            add(0);
                        }
                    });
                }
                NuevaPosicion();
                Mover();
                //System.out.println("Configurado" + iteraciones);

                for (iteracion = 0; iteracion < iteraciones; iteracion++) {
                    //Sincronizador de hilos de este cliente
                    contador = 0;
                    try {
                        //Bloquea hasta que los hilos hagan esta llamada.
                        barrera.await();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(HiloCliente.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (BrokenBarrierException ex) {
                        Logger.getLogger(HiloCliente.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    //System.out.println("Sale de barrera");
                    //System.out.println(id + " comienza iteracion " + (iteracion + 1));
                    //Devuelve el tiempo en milisegundos.
                    iniTime = System.currentTimeMillis();

                    //ENVIAR COORDENADAS
                    try {
                        mensaje = 1 + "/" + id + "/" + numGrupo + "/" + x + "/" + y + "/"+iteracion+"/";
                        mensajeEnBytes = mensaje.getBytes();
                        paqueteEnviar = new DatagramPacket(mensajeEnBytes, mensaje.length(), ip, puerto);

                        UDP.send(paqueteEnviar);
                        //System.out.println(id + " mensaje enviado ");
                    } catch (IOException e) {
                        //System.out.println("DEBUG: "+e);
                    }

                    for (int i = 0; i < (tamGrupo - 1) * 2; i++) {
                        //System.out.println(id+"Esperando mensaje: ");
                        //if(id==1)System.out.println("Bucle UDP "+i+((tamGrupo-1)*2));
                        try {
                            //tiempo de espera especificado, en milisegundos.
                            UDP.setSoTimeout(20000);
                        } catch (SocketException ex) {
                            //System.out.println(id+"SOCKET EXCEPTION: ");
                        }
                        mensajeEnBytes = new byte[256];
                        paqueteRecibido = new DatagramPacket(mensajeEnBytes, 256);
                        try {
                            //Recibe un DatagramPacket de este socket
                            UDP.receive(paqueteRecibido);
                            mensaje = new String(mensajeEnBytes).trim();
                            //System.out.println(id + " mensaje recibido ");
                            switch (procesarMensaje(mensaje, 1)) {
                                case 1:
                                    //if(id==1)System.out.println("Recibe coordenada "+mensaje);
                                    if(!RecibirCoordenada(mensaje, paqueteRecibido))
                                    {
                                        i--;
                                    }
                                    break;
                                case 2:
                                    //if(id==1)System.out.println("Recibe confirmacion");
                                    RecibirConfirmacion();
                                    break;
                            }
                        } catch (Exception e) {
                            //System.out.println(id+"Mensaje no llegado: ");
                            i=tamGrupo*2;
                            finTime = System.currentTimeMillis();
                            total += (finTime - iniTime);
                            contador = 0;
                            Mover();
                            System.out.println("Mensaje perdido");
                        }
                    }

                }
                //System.out.println("Fin iteraciones");
                enviarLatencias();
                UDP.close();
            }
        }
        //Si no se puede conectar se lanzara una excepcion con el mensaje de error
        //correspondiente.
        catch (IOException ex) {
            //System.out.println("DEBUG: " + ex);
        }
    }

    /**
     * En esta función creamos una conexión con protocolos TCP y UDP.
     * 
     * @return          True si la conexión se ha realizado con exito.
     *                  False si la conexión no es aceptada.
     */
    private boolean conectar()
    {
        try {
            TCP = new Socket(ip, puerto);
            UDP = new DatagramSocket();
            return true;
        } catch (Exception e) {
            System.out.println("Servidor no acepta conexiones.");
            return false;
        }
    }

    /**
     * Esta función se encarga de recibir las coordenadas de cada uno de los
     * clientes.
     * 
     * @param mensaje                   Mensaje a enviar
     * @param paqueteRecibido           Paquete que se ha recibido.
     * @return boolean                  Si la cordenada es de su iteracion.
     * @throws IOException              Posible excepción.
     */
    private boolean RecibirCoordenada(String mensaje, DatagramPacket paqueteRecibido) throws IOException
    {
        if(procesarMensaje(mensaje,6)!=iteracion)
        {
            System.out.println("Mensaje de otra iteracion");
            return false;
        }
        //RECIBIR COORDENADAS
        int xRec = procesarMensaje(mensaje, 4);
        int yRec = procesarMensaje(mensaje, 5);

        anadirPosicion(procesarMensaje(mensaje, 2), xRec, yRec);

        //System.out.println(id + " recibe coordenada de "+procesarMensaje(mensaje, 2));
        mensaje = 2 + "/" + id + "/" + numGrupo + "/" + procesarMensaje(mensaje, 2) +"/";
        byte[] mensajeEnBytes = mensaje.getBytes();
        DatagramPacket paqueteEnviar = new DatagramPacket(mensajeEnBytes, mensaje.length(), ip, puerto);

        UDP.send(paqueteEnviar);
        //System.out.println(id + " envia confirmacion "+mensaje);
        return true;
    }

    /**
     * Esta función confirma si la información se ha recibido correctamente, si 
     * el contador es menor que el tamaño del grupo se incrementa el contador y 
     * devuelve true ya que la informacion se ha recibido,sino devuelve false.
     * 
     * @return      Devolveremos true si la información se ha enviado correctamente.
     *              False en caso de que la información se pierda.
     */
    private boolean RecibirConfirmacion()
    {
        //System.out.println(id + "Recibe confirmacion, lleva" + contador);
        //if (contador < tamGrupo - 1) {
            //System.out.println("Aumenta contador");
            contador++;
        //}
        if (contador < tamGrupo - 1) {
            //System.out.println("Como contador es menor que "+(tamGrupo-1)+"No hace nada");
            return false;
        }
        //System.out.println("Como contador es igual que "+(tamGrupo-1));
        //System.out.println("Calculo de latencia");
        finTime = System.currentTimeMillis();
        //System.out.println(finTime+"-"+iniTime);
        //System.out.println("Latencia: " + (finTime - iniTime) + " en id " + id);
        total += (finTime - iniTime);
        //System.out.println("Latencia total: " + total + " en id " + id);
        contador = 0;
        Mover();
        //System.out.println(id + " fin iteracion");
        return true;
    }

    /**
     * Esta funcion calcula las latencias en tiempo real, calculamos una media 
     * de latencias con el total entre las iteraciones que se hacen. 
     */
    private void enviarLatencias()
    {
        //Sincronizador de hilos de este cliente
        try {
            barrera.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(HiloCliente.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BrokenBarrierException ex) {
            Logger.getLogger(HiloCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] mensajeEnBytes;
        DatagramPacket paqueteEnviar;
        String mensaje;

        media = total / iteraciones;
        System.out.println("Latencia total: "+total+" y Latencia media: " + media + " en id " + id);
        try {
            mensaje = 3 + "/" + id + "/" + numGrupo + "/" + media + "/";

            mensajeEnBytes = mensaje.getBytes();
            paqueteEnviar = new DatagramPacket(mensajeEnBytes, mensaje.length(), ip, puerto);
            UDP.send(paqueteEnviar);
            System.out.println("Latencia enviadas " + id);
        } catch (IOException ex) {
        }
    }

    /**
     * Se calcula una posición de forma aleatoria tomando valores maximos y 
     * minimos de las coordenadas X e Y.
     */
    private void NuevaPosicion()
    {
        x = (int) ((maxX - minX) * random());
        y = (int) ((maxY - minY) * random());
    }

    /**
     * Calculamos los movimientos que se realizan por las distintas coordenadas
     * y la añadimos, con su id y sus coordenadas X e Y.
     */
    private void Mover()
    {
        int aux = (int) (random() % 9);
        if (aux < 3) {
            x = x - 1;
        }
        if (aux > 5) {
            x = x + 1;
        }
        if (x > maxX) {
            x = maxX;
        }
        if (x < minX) {
            x = minX;
        }
        if (aux == 1 || aux == 4 || aux == 7) {
            y = y - 1;
        }
        if (aux == 0 || aux == 3 || aux == 6) {
            y = y + 1;
        }
        if (y > maxY) {
            y = maxY;
        }
        if (y < minY) {
            y = minY;
        }
        anadirPosicion(id, x, y);
    }

    /**
     * En esta función procesamos los distintos mensajes que se van a enviar, 
     * teniendo en cuenta la longitud del mensaje y cada vez que  llegamos al
     * caracter "/" sabemos que ya tenemos una parte del mensaje registrada.
     * Si llegamos a tener el mismo numero de atributos que esperabamos tener, 
     * devolvemos el numero de atributos que tiene el mensaje. Sino obtenemos
     * que la variable num sea una cadena vacia. Mientras que si no encontramos
     * el caracter "/" guardamos en la variable num su antiguo valor mas lo que 
     * haya en dicho índice especificado de String.
     * 
     * @param Mensaje       Mensaje a enviar.
     * @param c             Numero de atributos que tiene el mensaje.
     * @return              Devolvemos el numero de atributos que tiene el 
     *                      mensaje, siempre que haya el caracter "/" o sino 
     *                      devolveremos -1.
     */
    private int procesarMensaje(String Mensaje, int c)
    {
        String num = "";
        int aux = 0;
        for (int i = 0; i < Mensaje.length(); i++) {
            if (Mensaje.charAt(i) == '/') {
                aux++;
                if (aux == c) {
                    try {
                        return Integer.parseInt(num);
                    } catch (Exception e) {
                        return -1;
                    }
                } else {
                    num = "";
                }
            } else {
                num = num + Mensaje.charAt(i);
            }
        }
        return -1;
    }

    /**
     * Esta función añade las distintas posiciones por cada cliente que se 
     * reporta su posicion.
     * 
     * @param idRec         Identificador.
     * @param X             Coordenada X.
     * @param Y             Coordenada Y.
     */
    private void anadirPosicion(int idRec, int X, int Y)
    {
        Posiciones.get(idRec % tamGrupo).set(0, X);
        Posiciones.get(idRec % tamGrupo).set(1, Y);
    }

    /**
     * Funcion toString que pasa todos los atributos de la clase HiloCliente a un
     * String y lo devuelve con la variable Mensaje.
     * 
     * @return      Mensaje completo del HiloCliente con toda su informacion 
     *              correspondiente.
     */
    @Override
    public String toString()
    {
        String Mensaje = "Hilo " + id + "\n";
        Mensaje = Mensaje + "Posicion: [ " + x + " , " + y + " ]\n";
        if (iteracion > 0) {
            Mensaje = Mensaje + "Numero vecinos: " + (tamGrupo - 1) + "\n";
            Mensaje = Mensaje + "Iteracion: " + iteracion + "\n";
            Mensaje = Mensaje + "  Posiciones:\n";
            for (int i = 0; i < tamGrupo; i++) {
                if (i != id % tamGrupo) {
                    Mensaje = Mensaje + "  id: " + (i + tamGrupo * numGrupo) + " [ " + Posiciones.get(i).get(0) + " , " + Posiciones.get(i).get(1) + " ]\n";
                }
            }
        }
        return Mensaje;
    } 
}