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
import java.net.SocketTimeoutException;
import java.util.ArrayList;

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

    public HiloCliente(InetAddress ip, int puerto, Cliente padre)
    {
        this.puerto = puerto;
        this.ip = ip;
        total = 0;
        this.padre = padre;
        Posiciones = new ArrayList();
        iteracion = -1;
    }

    public void run()
    {
        String mensaje;
        DatagramPacket paqueteEnviar;
        byte[] mensajeEnBytes;
        DatagramPacket paqueteRecibido;
        int cont;
        boolean esperarConfirmacion = false;
        InetAddress ipEnviar;
        int xRec, yRec;
        ArrayList<InetAddress> IPS = new ArrayList<InetAddress>();

        try {
            if (conectar()) {
                //Configuracion por TCP
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
            }
        } catch (IOException ex) {
        }

        for (iteracion = 0; iteracion < iteraciones; iteracion++) {
            iniTime = System.currentTimeMillis();
            //ENVIAR COORDENADAS
            try {
                UDP = new DatagramSocket();
                mensaje = id + "/" + numGrupo + "/" + x + "/" + y + "/";
                mensajeEnBytes = mensaje.getBytes();
                paqueteEnviar = new DatagramPacket(mensajeEnBytes, mensaje.length(), ip, puerto);

                UDP.send(paqueteEnviar);
            } catch (IOException e) {
            }
            //RECIBIR COORDENADAS
            cont = 0;
            while (cont < tamGrupo - 1) {
                try {
                    UDP.setSoTimeout(15000);
                    mensajeEnBytes = new byte[256];
                    paqueteRecibido = new DatagramPacket(mensajeEnBytes, 256);
                    try {
                        UDP.receive(paqueteRecibido);
                        cont++;
                        mensaje = new String(mensajeEnBytes).trim();
                        xRec = procesarMensaje(mensaje, 3);
                        yRec = procesarMensaje(mensaje, 4);
                        if (yRec != -1) {

                            anadirPosicion(procesarMensaje(mensaje, 1), xRec, yRec);
                            IPS.add(paqueteRecibido.getAddress());
                        }
                    } catch (SocketTimeoutException e) {
                    }

                } catch (IOException ex) {
                }
            }

            for (int ips = 0; ips < IPS.size(); ips++) {
                try {
                    ipEnviar = IPS.get(ips);
                    mensaje = id + "/" + numGrupo + "/";
                    mensajeEnBytes = mensaje.getBytes();
                    paqueteEnviar = new DatagramPacket(mensajeEnBytes, mensaje.length(), ipEnviar, puerto);

                    UDP.send(paqueteEnviar);
                } catch (IOException ex) {
                }
            }

            //ENVIAR CONFIRMACIONES
            cont = 0;
            try {
                UDP.setSoTimeout(10000);
                while (cont < tamGrupo - 1 && !esperarConfirmacion) {

                    mensajeEnBytes = new byte[256];
                    paqueteRecibido = new DatagramPacket(mensajeEnBytes, 256);
                    try {
                        UDP.receive(paqueteRecibido);
                    } catch (SocketTimeoutException e) {
                        esperarConfirmacion = true;
                    }
                    mensaje = new String(mensajeEnBytes).trim();
                    cont++;
                }
            } catch (IOException e) {
            }
            finTime = System.currentTimeMillis();
            total += (finTime - iniTime);
            Mover();

            try {
                sleep(10);
            } catch (InterruptedException ex) {
            }
        }
        enviarLatencias();
        UDP.close();
    }

    private boolean conectar()
    {
        try {
            TCP = new Socket(ip, puerto);
            return true;
        } catch (Exception e) {
            System.out.println("Servidor no acepta conexiones.");
            return false;
        }
    }

    private void enviarLatencias()
    {
        byte[] mensajeEnBytes;
        DatagramPacket paqueteEnviar;
        String mensaje;

        media = total / iteraciones;

        try {
            mensaje = id + "/" + numGrupo + "/" + media + "/";

            mensajeEnBytes = mensaje.getBytes();
            paqueteEnviar = new DatagramPacket(mensajeEnBytes, mensaje.length(), ip, puerto);
            UDP.send(paqueteEnviar);
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
                    return Integer.parseInt(num);
                } else {
                    num = "";
                }
            } else {
                num = num + Mensaje.charAt(i);
            }
        }
        return -1;
    }

    private void anadirPosicion(int idRec, int x, int y)
    {
        Posiciones.get(idRec % tamGrupo).set(0, x);
        Posiciones.get(idRec % tamGrupo).set(1, y);
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
                    Mensaje = Mensaje + "  id: " + (i + tamGrupo * numGrupo) + " [ " + x + " , " + y + " ]\n";
                }
            }
        }
        return Mensaje;
    }
}
