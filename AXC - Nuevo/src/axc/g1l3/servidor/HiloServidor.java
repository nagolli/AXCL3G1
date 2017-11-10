/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axc.g1l3.servidor;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

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
    ArrayList<ArrayList<DatagramPacket>> Paquetes; //PONER INDIRECCION
    ArrayList<ArrayList<String>> Mensajes;         //PONER INDIRECCION
    boolean primero;
    ServerSocket TCP;
    DatagramSocket UDP;
    ArrayList<ArrayList<Socket>> Clientes = new ArrayList<>(cantidadClientes);  //Grupo, Socket
    ArrayList<ArrayList<Long>> Latencias;   //Grupo, Latencia
    Servidor padre;
    ArrayList<ReentrantLock> Locks;

    public HiloServidor(int cantidadClientes, int tamanoGrupos, int iteraciones, int puertoServidor, Servidor padre, ArrayList<ArrayList<DatagramPacket>> Paquetes, ArrayList<ArrayList<String>> Mensajes, DatagramSocket UDP, boolean primero, ArrayList<DatagramPacket> Pendientes, ArrayList<String> MPendientes, ArrayList<ReentrantLock> Locks, ArrayList<ArrayList<Long>> Latencias)
    {
        this.Locks=Locks;
        this.cantidadClientes = cantidadClientes;
        this.tamanoGrupos = tamanoGrupos;
        this.iteraciones = iteraciones;
        this.puertoServidor = puertoServidor;
        this.padre = padre;
        this.Pendientes = Pendientes;
        this.MPendientes = MPendientes;
        this.Mensajes = Mensajes;
        this.Paquetes = Paquetes;
        this.UDP = UDP;
        this.primero = primero;
        this.Latencias=Latencias;

    }

    public void AceptarConexiones()
    {
        int conexiones = 0, grupo = 0;
        try {

            TCP = new ServerSocket(puertoServidor);
            Clientes.add(new ArrayList<>(tamanoGrupos));
            //System.out.println(TCP.getInetAddress() + ":" + TCP.getLocalPort());
            while (conexiones < cantidadClientes) {
                Socket socket_conexion = TCP.accept();
                Clientes.get(grupo).add(socket_conexion);
                conexiones++;
                padre.clienteMas(conexiones);

                if (conexiones % tamanoGrupos == 0 && conexiones != cantidadClientes) {
                    grupo++;
                    Clientes.add(new ArrayList<>(tamanoGrupos));
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
        padre.print("Inicio comunicacion.\n");
        //System.out.println("Inicio Envio Inicios");
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
            
            for (i = 0; i < Clientes.size(); i++) {
                for (j = 0; j < Clientes.get(i).size(); j++) {
                    Clientes.get(i).get(j).close();
                }
            }
            
            padre.print("IDs Asignadas.\n");
            UDP.setSoTimeout(10000);
            //System.out.println("Fin envio Inicios");
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void run()
    {
        //System.out.println("Comienzo Run");
        if (primero) {

            ComienzoComunicacion();
        }

        while (true) {
            try {
                bucleUDP(Paquetes, Mensajes);
            } catch (Exception e) {
            }
        }
    }

    private void bucleUDP(ArrayList<ArrayList<DatagramPacket>> Paquetes, ArrayList<ArrayList<String>> Mensajes) throws SocketException, LineUnavailableException
    {
        String mensaje;
        DatagramPacket paqueteRecibido;
        byte[] mensajeEnBytes;

        //Recibir Mensaje UDP
        mensajeEnBytes = new byte[256];
        paqueteRecibido = new DatagramPacket(mensajeEnBytes, 256);
        try {
            UDP.receive(paqueteRecibido);   ///< LOCK?
            //System.out.println("Mensaje Recibido");
            mensaje = new String(mensajeEnBytes).trim();
            //System.out.println(mensaje);
            if (mensaje != null) {
                int caso = procesarMensaje(mensaje, 1);
                //System.out.println("Mensaje con contenido caso: " + caso);
                switch (caso) {
                    case 1:
                        //REENVIAR COORDENADAS
                        if (SumaClientes(Paquetes) < cantidadClientes) {
                            //System.out.println("Caso 1A");
                            reenviarCoordenadas(false, Paquetes, Mensajes, paqueteRecibido, mensaje);
                        } else {
                            //System.out.println("Caso 1B");
                            reenviarCoordenadas(true, Paquetes, Mensajes, paqueteRecibido, mensaje);
                        }
                        break;
                    case 2:
                        //REENVIAR CONFIRMACION

                        //System.out.println("Caso 2");
                        reenviarRespuesta(paqueteRecibido, mensajeEnBytes, Paquetes, Mensajes);
                        break;
                    case 3:
                        //RECIBIR TIEMPO
                        //System.out.println("Caso 3");
                        recibirTiempo(mensajeEnBytes);
                        break;
                }
            }
        } catch (SocketTimeoutException e) {
            System.out.println("No llegan mensajes");
            /*
            try {
                Clip sonido = AudioSystem.getClip();
                sonido.open(AudioSystem.getAudioInputStream(new File("alarma.wav")));
                sonido.start();
            } catch (UnsupportedAudioFileException | IOException ex) {
                Logger.getLogger(HiloServidor.class.getName()).log(Level.SEVERE, null, ex);
            }*/
            
        } catch (IOException ex) {
            Logger.getLogger(HiloServidor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void reenviarCoordenadas(boolean inicializado, ArrayList<ArrayList<DatagramPacket>> Paquetes, ArrayList<ArrayList<String>> Mensajes, DatagramPacket paqueteRecibido, String mensaje) throws IOException
    {
        //padre.print("Recibida coordenadas de " + procesarMensaje(mensaje, 2) + ".\n");
        int grupoClienteRecibido = procesarMensaje(mensaje, 3);
        
        if(!inicializado)
        {
        try{
        Locks.get(0).lock();
        //System.out.println("CODE:"+cantidadClientes+" "+Pendientes.size() );
        if ((cantidadClientes - 1 >= Pendientes.size())) {
            Paquetes.get(grupoClienteRecibido).add(paqueteRecibido); 
            Mensajes.get(grupoClienteRecibido).add(mensaje);         
            if (cantidadClientes - 1 > Pendientes.size()) {          
                System.out.println("Añadido a pendientes" + (Pendientes.size() + 1));
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
        }catch(Exception e){}finally{
        Locks.get(0).unlock();
        }
        }
        
        
        //System.out.println("Enviando Mensajes " + cantidadClientes + " " + Mensajes.size());
        int idClienteEnviar = procesarMensaje(mensaje, 2);
        //Enviar a todos el mensaje
        for (int m = 0; m < Mensajes.get(grupoClienteRecibido).size(); m++) {
            int idEnviar = procesarMensaje(Mensajes.get(grupoClienteRecibido).get(m), 2);
                if (idEnviar != idClienteEnviar) {
                    byte[] mensajeEnBytes = paqueteRecibido.getData();
                    System.out.println("Enviando: de" +idClienteEnviar+" a "+ idEnviar);
                    DatagramPacket paqueteEnvio = new DatagramPacket(mensajeEnBytes, mensajeEnBytes.length, Paquetes.get(grupoClienteRecibido).get(m).getAddress(), Paquetes.get(grupoClienteRecibido).get(m).getPort());
                    UDP.send(paqueteEnvio);
                    //padre.print("Enviadas coordenadas de " + idClienteEnviar + " a " + idEnviar + ".\n");
                
            }

        }

    }

    public void reenviarRespuesta(DatagramPacket paqueteRecibido, byte[] mensajeEnBytes, ArrayList<ArrayList<DatagramPacket>> Paquetes, ArrayList<ArrayList<String>> Mensajes) throws SocketException, IOException
    {
        String mensaje = new String(mensajeEnBytes).trim();
        int grupo = procesarMensaje(mensaje, 3);
        int idConfirmar = procesarMensaje(mensaje, 4);
        for (int i = 0; i < Paquetes.get(grupo).size(); i++) {
                if (procesarMensaje(Mensajes.get(grupo).get(i), 2) == idConfirmar) {
                    int puertoDestino = Paquetes.get(grupo).get(i).getPort();
                    InetAddress IP = Paquetes.get(grupo).get(i).getAddress();
                    DatagramPacket paqueteEnvio = new DatagramPacket(mensajeEnBytes, mensajeEnBytes.length, IP, puertoDestino);
                    UDP.send(paqueteEnvio);
                    System.out.println("Reenviada Confirmacion de " + procesarMensaje(mensaje, 2) + " a " + idConfirmar + ".\n");
                    //padre.print("Reenviada Confirmacion de " + procesarMensaje(mensaje, 2) + " a " + idConfirmar + ".\n");
                    return;
                }
        }

    }

    void recibirTiempo(byte[] mensajeEnBytes)
    {
        String mensaje;
        Long tiempo;
        int grupo;

        mensaje = new String(mensajeEnBytes).trim();

        grupo = procesarMensaje(mensaje, 3);
        tiempo = procesarMensajeLon(mensaje, 4)/iteraciones;
        padre.print("Recibida Latencia de " + procesarMensaje(mensaje, 2) + " de " + tiempo + " ms.\n");
        //System.out.println("Latencia recibida: " + tiempo);
        try{
        Locks.get(1).lock();
        Latencias.get(grupo).add(tiempo);
        if (SumaLatencias(Latencias) == cantidadClientes) {    
            calcularTiempos();
        }
        }catch(Exception e){}finally{
        Locks.get(1).unlock();
        }
    }

    void calcularTiempos()
    {
        //System.out.println("DEBUG: CALCULANDO TIEMPOS");
        //System.out.println(Latencias);
        float latencia_media;

        for (int i = 0; i < Latencias.size(); i++) {
            latencia_media = 0;
            for (int j = 0; j < Latencias.get(i).size(); j++) {
                latencia_media += Latencias.get(i).get(j);
            }

            latencia_media = latencia_media / Latencias.get(i).size();
            Latencias.set(i, new ArrayList());
            Latencias.get(i).add((long) latencia_media);
            padre.print("Latencia medio del grupo " + i + " = " + latencia_media + " ms.\n");
        }
        latencia_media = 0;
        for (int i = 0; i < Latencias.size(); i++) {
            latencia_media += Latencias.get(i).get(0);
        }
        latencia_media = latencia_media / Latencias.size();
        padre.print("Latencia media total = " + latencia_media + " ms.\n");

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

    private int SumaClientes(ArrayList<ArrayList<DatagramPacket>> Paquetes)
    {
        int cont=0;
        for(int i=0;i<Paquetes.size();i++)
            cont+=Paquetes.get(i).size();
        return cont;
    }
    
    private int SumaLatencias(ArrayList<ArrayList<Long>> Latencias)
    {
        int cont=0;
        for(int i=0;i<Latencias.size();i++)
            cont+=Latencias.get(i).size();
        return cont;
    }
}
