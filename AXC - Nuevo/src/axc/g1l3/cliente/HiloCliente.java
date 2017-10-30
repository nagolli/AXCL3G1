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

    public void run()
    {
        String mensaje;
        DatagramPacket paqueteEnviar;
        byte[] mensajeEnBytes;
        DatagramPacket paqueteRecibido;
        try {
            if (conectar()) {
                //Configuracion por TCP
                //System.out.println("Conectado y configurando");
                recibir_datos = new DataInputStream(TCP.getInputStream());
                mensaje = recibir_datos.readUTF();
                id = procesarMensaje(mensaje, 1);
                numGrupo = procesarMensaje(mensaje, 2);
                tamGrupo = procesarMensaje(mensaje, 3);
                iteraciones = procesarMensaje(mensaje, 4);
                recibir_datos.close();
                TCP.close();
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
                        barrera.await();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(HiloCliente.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (BrokenBarrierException ex) {
                        Logger.getLogger(HiloCliente.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    //System.out.println("Sale de barrera");
                    //System.out.println(id + " comienza iteracion " + (iteracion + 1));
                    iniTime = System.currentTimeMillis();

                    //ENVIAR COORDENADAS
                    try {
                        mensaje = 1 + "/" + id + "/" + numGrupo + "/" + x + "/" + y + "/";
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
                            UDP.setSoTimeout(15000);
                        } catch (SocketException ex) {
                            //System.out.println(id+"SOCKET EXCEPTION: ");
                        }
                        mensajeEnBytes = new byte[256];
                        paqueteRecibido = new DatagramPacket(mensajeEnBytes, 256);
                        try {
                            UDP.receive(paqueteRecibido);
                            mensaje = new String(mensajeEnBytes).trim();
                            //System.out.println(id + " mensaje recibido ");
                            switch (procesarMensaje(mensaje, 1)) {
                                case 1:
                                    //if(id==1)System.out.println("Recibe coordenada "+mensaje);
                                    RecibirCoordenada(mensaje, paqueteRecibido);
                                    break;
                                case 2:
                                    //if(id==1)System.out.println("Recibe confirmacion");
                                    RecibirConfirmacion();
                                    break;
                            }
                        } catch (Exception e) {
                            //System.out.println(id+"Mensaje no llegado: ");
                            i--;
                        }
                    }

                }
                //System.out.println("Fin iteraciones");
                enviarLatencias();
                UDP.close();
            }
        } catch (IOException ex) {
            System.out.println("DEBUG: " + ex);
        }
    }

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

    private void RecibirCoordenada(String mensaje, DatagramPacket paqueteRecibido) throws IOException
    {
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
    }

    private boolean RecibirConfirmacion()
    {
        //System.out.println(id + "Recibe confirmacion, lleva" + contador);
        if (contador < tamGrupo - 1) {
            //System.out.println("Aumenta contador");
            contador++;
        }
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
        contador = 0;
        Mover();
        //System.out.println(id + " fin iteracion");
        return true;
    }

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
        System.out.println("Latencia media: " + media + " en id " + id);
        try {
            mensaje = 3 + "/" + id + "/" + numGrupo + "/" + media + "/";

            mensajeEnBytes = mensaje.getBytes();
            paqueteEnviar = new DatagramPacket(mensajeEnBytes, mensaje.length(), ip, puerto);
            UDP.send(paqueteEnviar);
            System.out.println("Latencia enviadas " + id);
        } catch (IOException ex) {
        }
    }

    private void NuevaPosicion()
    {
        x = (int) ((maxX - minX) * random());
        y = (int) ((maxY - minY) * random());
    }

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

    private void anadirPosicion(int idRec, int X, int Y)
    {
        Posiciones.get(idRec % tamGrupo).set(0, X);
        Posiciones.get(idRec % tamGrupo).set(1, Y);
    }

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


