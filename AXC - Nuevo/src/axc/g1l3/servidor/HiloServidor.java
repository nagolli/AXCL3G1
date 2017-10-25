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
    }

    public void AceptarConexiones(){
        int conexiones = 0, grupo = 0;
        try {
            TCP = new ServerSocket(puertoServidor);
            Clientes.add(new ArrayList<>(tamanoGrupos));
            Latencias.add(new ArrayList<>(tamanoGrupos));
            System.out.println("Esperando conexion de clientes...");
            while (conexiones < cantidadClientes) {
                Socket socket_conexion = TCP.accept();
                Clientes.get(grupo).add(socket_conexion);
                conexiones++;

                if (conexiones % tamanoGrupos == 0 && conexiones != cantidadClientes) 
                {
                    grupo++;
                    Clientes.add(new ArrayList<>(tamanoGrupos));
                    Latencias.add(new ArrayList<>(tamanoGrupos));
                }

            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
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
        DataOutputStream enviar_datos=null;
        try {
            Socket destino;
            for (i = 0; i < Clientes.size(); i++) {
                for (j = 0; j < Clientes.get(i).size(); j++) {
                    destino = (Socket) Clientes.get(i).get(j);
                    enviar_datos = new DataOutputStream(destino.getOutputStream());
                    enviar_datos.writeUTF((i * tamanoGrupos + j) + "/" + Clientes.get(i).size()+ "/");
                    enviar_datos.flush();
                }
                System.out.println("Comunicacion iniciada con el grupo " + i + " que tiene " + j + " clientes");
            }
            enviar_datos.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
    
    @Override
    public void run()
    {
        String mensaje;
        int puertoDestino;
        int grupoClienteRecibido;
        int idClienteRecibido;
        int idClienteEnviar;
        InetAddress IP;
        DatagramPacket paqueteRecibido;
        DatagramPacket paqueteEnvio;
        int contador;
        ArrayList<DatagramPacket> Paquetes;
        ArrayList<String> Mensajes;
        byte[] mensajeEnBytes;
        int idEnviar;

        try {
            UDP = new DatagramSocket(puertoServidor);
            ComienzoComunicacion();

            for (int i = 0; i < iteraciones; i++) {
                padre.PrintIteracion(i+1);
                contador = 0;
                Paquetes = new ArrayList();
                Mensajes = new ArrayList();
                while (contador <= cantidadClientes) {
                    if (contador == 0) {    //Recibir primer mensaje
                        for (int c = 0; c < cantidadClientes; c++) {
                            mensajeEnBytes = new byte[256];
                            paqueteRecibido = new DatagramPacket(mensajeEnBytes, 256);
                            UDP.setSoTimeout(10000);

                            try {
                                UDP.receive(paqueteRecibido);
                                mensaje = new String(mensajeEnBytes).trim();
                                Paquetes.add(paqueteRecibido);
                                Mensajes.add(mensaje);
                                padre.print("Reenviando posicion del cliente " + mensaje);
                            } catch (SocketTimeoutException e) {
                            } catch (IOException ex) {
                                Logger.getLogger(HiloServidor.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        }
                    }
                    //Si ya ha recibido un mensaje previamete ya tiene datos
                    paqueteRecibido = Paquetes.get(contador);
                    mensaje = Mensajes.get(contador);
                    puertoDestino = paqueteRecibido.getPort();
                    IP = paqueteRecibido.getAddress();
                    idClienteRecibido = procesarMensaje(mensaje, 1);
                    grupoClienteRecibido = procesarMensaje(mensaje, 2);

                    if (contador == 0) {
                        for (int c = 0; 0 < Paquetes.size(); c++) {
                            idClienteEnviar = procesarMensaje(Mensajes.get(i), 1);
                            //Enviar a todos el mensaje
                            for (int m = 0; m < Mensajes.size(); m++) {
                                idEnviar = procesarMensaje(Mensajes.get(m), 1);
                                if (procesarMensaje(Mensajes.get(m), 2) == grupoClienteRecibido) {
                                    if (idEnviar != idClienteEnviar) {
                                        mensajeEnBytes = Paquetes.get(c).getData();
                                        paqueteEnvio = new DatagramPacket(mensajeEnBytes, mensajeEnBytes.length, Paquetes.get(m).getAddress(), Paquetes.get(m).getPort());
                                        UDP.send(paqueteEnvio);
                                    }
                                }
                            }
                        }

                    }

                    //Recibir Respuestas
                    int contadorAux = 0;
                    mensajeEnBytes = new byte[256];
                    paqueteRecibido = new DatagramPacket(mensajeEnBytes, 256);
                    UDP.setSoTimeout(2000);
                    while (contadorAux < tamanoGrupos - 1) {

                        contadorAux++;
                        try {
                            UDP.receive(paqueteRecibido);
                            paqueteEnvio = new DatagramPacket(mensajeEnBytes, mensajeEnBytes.length, IP, puertoDestino);
                            UDP.send(paqueteEnvio);
                        } catch (SocketTimeoutException e) {
                            System.err.println(e.getMessage());
                        }
                    }

                    contador++;
                }//Fin While
            }
            for (int i = 0; i < cantidadClientes; i++) {
                recibirTiempos();
            }

            TCP.close();
            UDP.close();

            calcularTiempos();
        } catch (IOException ex) {
            Logger.getLogger(HiloServidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    void recibirTiempos() 
    {
        String mensaje;
        Long tiempo;
        int grupo;
        byte[] mensajeEnBytes = new byte[256];
        DatagramPacket resp_paquete = new DatagramPacket(mensajeEnBytes, 256);

        try {
            UDP.setSoTimeout(10000);
            try {
                UDP.receive(resp_paquete);
            } catch (SocketTimeoutException e) {
                System.err.println(e.getMessage());
            }
            mensaje = new String(mensajeEnBytes).trim();
            if (mensaje.contains(".")) { //Solo usamos longs en estos mensajes
                grupo = procesarMensaje(mensaje,2);
                tiempo=procesarMensajeLon(mensaje,3);
                Latencias.get(grupo).add(tiempo);
            } 
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    void calcularTiempos() {
        float latencia_media;

        for (int i = 0; i < Latencias.size(); i++) {
            latencia_media = 0;
            for (int j = 0; j < Latencias.get(i).size(); j++) {
                latencia_media += Latencias.get(i).get(j);
            }

            latencia_media = latencia_media/1000;

            padre.print("Latencia medio del grupo " + i + " = " + latencia_media + " s");
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
        String nombre="";
        int aux=1;
        for (int i = 0; i < Mensaje.length(); i++) {
            if (Mensaje.charAt(i) == '/') {
                if(aux==c)
                {
                    return Long.parseLong(nombre);
                }
                else
                {
                    nombre="";
                    aux++;
                }
            } else {
                nombre = nombre + Mensaje.charAt(i);
            }
        }
        return 0l;
    }

    

    
    
}
