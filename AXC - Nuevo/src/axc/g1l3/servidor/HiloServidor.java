/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axc.g1l3.servidor;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ignacio
 */
public class HiloServidor extends Thread
{

    int cantidadClientes;
    int puertoServidor;
    int tamanoGrupos;
    int iteraciones;
    ArrayList<DatagramPacket> Pendientes;
    ArrayList<String> MPendientes;
    int latencias;

    ServerSocket TCP;
    DatagramSocket UDP;
    ArrayList<ArrayList<Socket>> Clientes = new ArrayList<>(cantidadClientes);
    ArrayList<ArrayList<Long>> Latencias = new ArrayList<>(cantidadClientes);
    Servidor padre;

    public HiloServidor(int cantidadClientes, int tamanoGrupos, int iteraciones, int puertoServidor, Servidor padre)
    {
        this.cantidadClientes = cantidadClientes;
        this.tamanoGrupos = tamanoGrupos;
        this.iteraciones = iteraciones;
        this.puertoServidor = puertoServidor;
        this.padre = padre;
        Pendientes = new ArrayList();
        MPendientes = new ArrayList();
        latencias=0;
    }

    public void AceptarConexiones()
    {
        int conexiones = 0, grupo = 0;
        try {

            TCP = new ServerSocket(puertoServidor);
            Clientes.add(new ArrayList<>(tamanoGrupos));
            Latencias.add(new ArrayList<>(tamanoGrupos));
            System.out.println(TCP.getInetAddress() + ":" + TCP.getLocalPort());
            while (conexiones < cantidadClientes) {
                Socket socket_conexion = TCP.accept();
                Clientes.get(grupo).add(socket_conexion);
                conexiones++;
                padre.clienteMas(conexiones);

                if (conexiones % tamanoGrupos == 0 && conexiones != cantidadClientes) {
                    grupo++;
                    Clientes.add(new ArrayList<>(tamanoGrupos));
                    Latencias.add(new ArrayList<>(tamanoGrupos));
                }
                Thread.sleep(5);
            }
        } catch (IOException e) {
            System.out.println("Error " + e);
        } catch (InterruptedException ex) {
            Logger.getLogger(HiloServidor.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error");
        }
    }

    private void ComienzoComunicacion()
    {
        //TCP
        /*
            enviar_datos = new DataOutputStream(cliente.getOutputStream());
            recibir_datos = new DataInputStream(cliente.getInputStream());
            enviar_datos.writeUTF(mensaje_enviado);
            enviar_datos.flush();
            mensaje=recibir_datos.readUTF();
         */
        int i, j;
        DataOutputStream enviar_datos = null;
        System.out.println("Inicio Envio Inicios");
        try {
            Socket destino;
            for (i = 0; i < Clientes.size(); i++) {
                for (j = 0; j < Clientes.get(i).size(); j++) {
                    destino = (Socket) Clientes.get(i).get(j);
                    enviar_datos = new DataOutputStream(destino.getOutputStream());
                    enviar_datos.writeUTF((i * tamanoGrupos + j) + "/" + i + "/" + Clientes.get(i).size() + "/" + String.valueOf(iteraciones) + "/");
                    //                            ID               grupo        clientes en su grupo                  Iteraciones
                    enviar_datos.flush();
                }
            }
            enviar_datos.close();
            System.out.println("Fin envio Inicios");
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void run()
    {
        System.out.println("Comienzo Run");
        String mensaje;
        int puertoDestino;
        int grupoClienteRecibido;
        int idClienteEnviar;
        InetAddress IP;
        DatagramPacket paqueteRecibido;
        DatagramPacket paqueteEnvio;
        int contador;
        ArrayList<DatagramPacket> Paquetes = new ArrayList();
        ArrayList<String> Mensajes = new ArrayList();
        byte[] mensajeEnBytes;
        int idEnviar;

        try {
            UDP = new DatagramSocket(puertoServidor);
            ComienzoComunicacion();

            while (true) {
                try {
                    bucleUDP(Paquetes, Mensajes);
                } catch (Exception e) {
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(HiloServidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void bucleUDP(ArrayList<DatagramPacket> Paquetes, ArrayList<String> Mensajes) throws SocketException
    {
        String mensaje;
        DatagramPacket paqueteRecibido;
        byte[] mensajeEnBytes;

        //Recibir Mensaje UDP
        mensajeEnBytes = new byte[256];
        paqueteRecibido = new DatagramPacket(mensajeEnBytes, 256);
        UDP.setSoTimeout(10000);
        try {
            UDP.receive(paqueteRecibido);
            System.out.println("Mensaje Recibido");
            mensaje = new String(mensajeEnBytes).trim();
            System.out.println(mensaje);
            if (mensaje != null) {
                int caso=procesarMensaje(mensaje, 1);
                System.out.println("Mensaje con contenido caso: "+caso);
                switch (caso) {
                    case 1:
                        //REENVIAR COORDENADAS
                        if (Paquetes.size() < cantidadClientes) {
                            System.out.println("Caso 1A");
                            reenviarCoordenadas(false, Paquetes, Mensajes, paqueteRecibido, mensaje);
                        } else {
                            System.out.println("Caso 1B");
                            reenviarCoordenadas(true, Paquetes, Mensajes, paqueteRecibido, mensaje);
                        }
                        break;
                    case 2:
                        //REENVIAR CONFIRMACION
                        
                        System.out.println("Caso 2");
                        reenviarRespuesta(paqueteRecibido, mensajeEnBytes);
                        break;
                    case 3:
                    //RECIBIR TIEMPO
                        System.out.println("Caso 3");
                        recibirTiempo(mensajeEnBytes);
                        break;
                }
            }
        } catch (SocketTimeoutException e) {
            System.out.println("No llegan mensajes");
        } catch (IOException ex) {
            Logger.getLogger(HiloServidor.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Switch

    }

    public void reenviarCoordenadas(boolean inicializado, ArrayList<DatagramPacket> Paquetes, ArrayList<String> Mensajes, DatagramPacket paqueteRecibido, String mensaje) throws IOException
    {
        if (!inicializado) {
            Paquetes.add(paqueteRecibido);
            Mensajes.add(mensaje);
            if (cantidadClientes-1 > Pendientes.size()) {
                System.out.println("Añadido a pendientes"+(Pendientes.size()+1));
                Pendientes.add(paqueteRecibido);
                MPendientes.add(mensaje);
                return;
            } else {
                System.out.println("Enviando Pendientes");
                for (int i = 0; i < cantidadClientes - 1; i++) {
                    reenviarCoordenadas(true, Paquetes, Mensajes, Pendientes.get(i), MPendientes.get(i));
                }
            }
        }
        System.out.println("Enviando Mensaje");
        int grupoClienteRecibido = procesarMensaje(mensaje, 3);

        for (int c = 0; c < Paquetes.size(); c++) {
            int idClienteEnviar = procesarMensaje(mensaje, 2);
            //Enviar a todos el mensaje
            for (int m = 0; m < Mensajes.size(); m++) {
                int idEnviar = procesarMensaje(Mensajes.get(m), 2);
                if (procesarMensaje(Mensajes.get(m), 3) == grupoClienteRecibido) {
                    if (idEnviar != idClienteEnviar) {
                        byte[] mensajeEnBytes = Paquetes.get(c).getData();
                        DatagramPacket paqueteEnvio = new DatagramPacket(mensajeEnBytes, mensajeEnBytes.length, Paquetes.get(m).getAddress(), Paquetes.get(m).getPort());
                        UDP.send(paqueteEnvio);
                    }
                }
            }

        }

    }

    public void reenviarRespuesta(DatagramPacket paqueteRecibido, byte[] mensajeEnBytes) throws SocketException, IOException
    {
        int puertoDestino = paqueteRecibido.getPort();
        InetAddress IP = paqueteRecibido.getAddress();
        DatagramPacket paqueteEnvio = new DatagramPacket(mensajeEnBytes, mensajeEnBytes.length, IP, puertoDestino);
        UDP.send(paqueteEnvio);
    }

    void recibirTiempo(byte[] mensajeEnBytes)
    {
        String mensaje;
        Long tiempo;
        int grupo;

        mensaje = new String(mensajeEnBytes).trim();

        grupo = procesarMensaje(mensaje, 3);
        tiempo = procesarMensajeLon(mensaje, 4);
        System.out.println("Latencia recibida: " + tiempo);
        Latencias.get(grupo).add(tiempo);
        latencias++;
        if(latencias==cantidadClientes)
        {
            calcularTiempos();
        }

    }

    void calcularTiempos()
    {
        float latencia_media;

        for (int i = 0; i < Latencias.size(); i++) {
            latencia_media = 0;
            for (int j = 0; j < Latencias.get(i).size(); j++) {
                latencia_media += Latencias.get(i).get(j);
            }

            latencia_media = latencia_media / 1000;

            padre.print("Latencia medio del grupo " + i + " = " + latencia_media + " s\n");
        }
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

    private Long procesarMensajeLon(String Mensaje, int c)
    {
        String nombre = "";
        int aux = 1;
        for (int i = 0; i < Mensaje.length(); i++) {
            if (Mensaje.charAt(i) == '/') {
                if (aux == c) {
                    return Long.parseLong(nombre);
                } else {
                    nombre = "";
                    aux++;
                }
            } else {
                nombre = nombre + Mensaje.charAt(i);
            }
        }
        return 0l;
    }

}
