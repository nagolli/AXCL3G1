/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axc.g1l3.servidor;

import axc.g1l3.utilidades.MensajeroUDP;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ignacio
 */
public class HiloServidor extends Thread
{
    
    protected Servidor server;
    protected Socket TCP;
    private DatagramSocket UDP;
    private int idClienteAsignada;
    private InetAddress direccion;
    private boolean almacenado;
    private static int contador;
    private int idCliente;
    private String mensaje_enviado;
    private String mensaje_recibido;
    
    public static void setContador(int contador)
    {
        HiloServidor.contador = contador;
    }
    
    public HiloServidor(Socket socket, Servidor server, DatagramSocket Udp)
    {
        almacenado = false;
        this.TCP = socket;
        direccion = TCP.getLocalAddress();
        this.server = server;
        idCliente = contador;
        HiloServidor.contador = idCliente + 1;
        UDP = Udp;
    }

    /*
    Codigo que ejecuta mientras se encuentre conectado
     */
    public void run()
    {
        try {
            /* Cabecera de funciones TCP*/
            DataOutputStream enviar_datos = new DataOutputStream(TCP.getOutputStream());
            DataInputStream recibir_datos = new DataInputStream(TCP.getInputStream());
            /*
            enviar_datos.writeUTF(mensaje_enviado);
            enviar_datos.flush();
            mensaje=recibir_datos.readUTF();
             */
            enviar_datos.writeUTF(server.getGrupos());
            enviar_datos.flush();

            /* Espera hasta recibir int de sala como string*/
            int idTema = Integer.parseInt(recibir_datos.readUTF());

            /* Se agrega a sala */
            int idSala = server.addConexion(TCP, idTema, idCliente);
            /*Envia el ID de cliente y ID de Sala como string*/
            enviar_datos.writeUTF(String.valueOf(idCliente) + "/" + String.valueOf(idSala) + "/");
            enviar_datos.flush();

            /*El cliente manda su puerto UDP*/
            int numPort = Integer.parseInt(recibir_datos.readUTF());
            server.addUDPPort(idSala, direccion, numPort);
            TCP.close();
        } catch (SocketException ex) {
            Logger.getLogger(HiloServidor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HiloServidor.class.getName()).log(Level.SEVERE, null, ex);
        }

        /*Se pone en bucle, para realizar las conexiones UDP*/
        while (server.getIteracion() < 100) {
            
        }
    }

    /*
    Distribuir coordenadas:
    Función que dada una ID de sala y un DatagramPacket, 
    lo reenvia al resto de clientes en la sala.
    
    Se llama desde el BucleUDP
     */
    private void distribuirCoordenadas(int IDsala, DatagramPacket paqueteRecibido, int IdHilo)
    {
        byte[] mensajeEnBytes;
        DatagramPacket paqueteEnviado;
        int portDest, portSent;
        InetAddress dir, dirSent;
        portSent=server.GetPorClienteUDP(IDsala, IdHilo);
        dirSent=server.GetDirCliente(IDsala, IdHilo);
        mensajeEnBytes = paqueteRecibido.getData();
        server.setUltimoMensajeEnviado(null,mensajeEnBytes,IDsala,IdHilo);
        try {
            //System.out.println("Enviando datos a sala"+IDsala+" que tiene "+GetClientesSala(IDsala)+" clientes");
            for (int i = 0; i < GetClientesSala(IDsala); i++) {
                portDest = GetPorCliente(IDsala, i);
                dir = GetDirCliente(IDsala, i);
                if(portSent!=portDest&&dirSent!=dir)
                {
                //System.out.println("Paquete enviado a "+dir+":"+portDest+" de contenido"+new String(mensajeEnBytes).trim());
                MensajeroUDP.EnviarMensaje3(UDP, dir, portDest, mensajeEnBytes);
                paqueteEnviado = new DatagramPacket(mensajeEnBytes, mensajeEnBytes.length, dir, portDest); // Envía
                UDP.send(paqueteEnviado);
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    /*Funciones de consulta al server*/
    private int GetClientesSala(int IDsala)
    {
        return server.GetClientesSala(IDsala);
    }
    
    private InetAddress GetDirCliente(int IDsala, int indice)
    {
        return server.GetDirCliente(IDsala, indice);
    }
    
    private int GetPorCliente(int IDsala, int indice)
    {
        return server.GetPorClienteUDP(IDsala, indice);
    }
    
    private void BucleUDP()
    {
        /*Proceso si le llega:
        1) Llega mensaje
        2) Cual es su id?
        3) En que estado esta?
            Si ID.getEstado!=estado+1
            4a) Se ignora el mensaje
            Else
            4b) Se procesa la respuesta         
            5b) Se avanza estado
         */
        try {
            String mensaje;
            byte[] mensajeEnBytes = new byte[256];
            DatagramPacket PaqueteRecibido = new DatagramPacket(mensajeEnBytes, 256);
            UDP.setSoTimeout(10000);
            
            try {
                UDP.receive(PaqueteRecibido);
                //System.out.println("Paquete recibido");

                mensaje = new String(mensajeEnBytes).trim();
                if (mensaje != null) {
                    int estado = MensajeroUDP.procesarMensajeInt(mensaje, 1);
                    int idHilo = MensajeroUDP.procesarMensajeInt(mensaje, 2);
                    int idSala = MensajeroUDP.procesarMensajeInt(mensaje, 3);
                    int estadoCliente = server.getEstadoCliente(idHilo, idSala);
                    if ((estadoCliente + 1 == estado) || (estado == 7 && estadoCliente == 7)) {
                        switch (estado) {
                            case 2:
                                server.estadoClienteMas(idHilo, idSala);
                                distribuirCoordenadas(idSala, PaqueteRecibido, idHilo);
                                break;
                            case 4:
                                int idDest = MensajeroUDP.procesarMensajeInt(mensaje, 4);
                                server.setUltimoMensajeEnviado(MensajeroUDP.EnviarMensaje5(UDP, direccion, estadoCliente, idDest),null,idSala,idHilo);
                                break;
                            case 6:
                                int idRec = MensajeroUDP.procesarMensajeInt(mensaje, 4);
                                server.estadoClienteMas(idHilo, idSala);
                                server.setClienteRecibido(idSala, idHilo, idRec);
                                break;
                            case 7:
                                String lat = MensajeroUDP.procesarMensajeStr(mensaje, 4);
                                server.AnadirLatencia(Float.parseFloat(lat), idSala);
                                MensajeroUDP.EnviarMensaje8(UDP, direccion, estadoCliente);
                                break;
                        }
                        
                    }
                }
            } catch (SocketTimeoutException e) {
                
            }
        } catch (Exception e) {
            
        }
    }
}
