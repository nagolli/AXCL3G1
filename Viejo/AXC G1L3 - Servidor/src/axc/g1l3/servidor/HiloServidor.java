/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axc.g1l3.servidor;

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
    DatagramSocket UDP;
    int idAsignada,idClienteAsignada;
    InetAddress direccion;
    boolean almacenado;
    static int contador;
    int id;
    String mensaje_enviado;
    String mensaje_recibido;

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
        id = contador;
        HiloServidor.contador = id + 1;
        UDP = Udp;
    }

    /*
    Codigo que ejecuta mientras se encuentre conectado
     */
    public void run()
    {
        try {
            /* Envia string de salas*/
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
            int intSala = Integer.parseInt(recibir_datos.readUTF());

            /* Se agrega a sala */
            //System.out.println("Sala solicitada: "+intSala);
            idAsignada = server.addConexion(TCP, intSala);
            System.out.println("Id asignada "+idAsignada);
            /*Envia el ID de cliente y ID de Sala como string*/
            enviar_datos.writeUTF(String.valueOf(id)+"/"+String.valueOf(idAsignada)+"/");
            enviar_datos.flush();

            /*El cliente manda su puerto UDP*/
            int numPort = Integer.parseInt(recibir_datos.readUTF());
            server.addUDPdata(idAsignada, direccion, numPort);
            
            /*Se pone en bucle, en espera retransmitiendo información hasta fin de conexión*/
            while (CheckNotClosed(TCP)) {
                String mensaje;
                byte[] mensajeEnBytes = new byte[256];
                DatagramPacket PaqueteRecibido = new DatagramPacket(mensajeEnBytes, 256);
                UDP.setSoTimeout(10000);
                try {
                    UDP.receive(PaqueteRecibido);
                    //System.out.println("Paquete recibido");

                    mensaje = new String(mensajeEnBytes).trim();
                    if (mensaje != null) {
                        idClienteAsignada=procesarMensaje(mensaje,5);
                        distribuirCoordenadas(idClienteAsignada, PaqueteRecibido);
                    }
                } catch (SocketTimeoutException e) {
                    
                }
            }
            /*Proceso en caso de desconexión*/
            server.desconexion(idAsignada, direccion);

        } catch (SocketException ex) {
            Logger.getLogger(HiloServidor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HiloServidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
    Distribuir coordenadas:
    Función que dada una ID de sala y un DatagramPacket, 
    lo reenvia al resto de clientes en la sala.
    
    Se llama desde el Run
     */
    private void distribuirCoordenadas(int IDsala, DatagramPacket paqueteRecibido)
    {
        byte[] mensajeEnBytes;
        DatagramPacket paqueteEnviado;
        int portDest;
        InetAddress dir;
        try {
            //System.out.println("Enviando datos a sala"+IDsala+" que tiene "+GetClientesSala(IDsala)+" clientes");
            for (int i = 0; i < GetClientesSala(IDsala); i++) {
                mensajeEnBytes = paqueteRecibido.getData();
                portDest = GetPorCliente(IDsala, i);
                //System.out.println("Enviando datos a "+portDest);
                if (portDest > 0) {
                    dir = GetDirCliente(IDsala, i);
                    if (dir != null) {
                        //System.out.println("Paquete enviado a "+dir+":"+portDest+" de contenido"+new String(mensajeEnBytes).trim());
                        paqueteEnviado = new DatagramPacket(mensajeEnBytes, mensajeEnBytes.length, dir, portDest); // Envía
                        UDP.send(paqueteEnviado);
                    }
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

    private boolean CheckNotClosed(Socket con) throws IOException
    {
        return !con.isClosed();
        /*
        DataOutputStream enviar_datos = new DataOutputStream(con.getOutputStream());
        try {
            enviar_datos.writeUTF(" ");
            return true;
        } catch (IOException ex) {
            return false;
        }
         */
    }
    
    private int procesarMensaje(String Mensaje, int c)
    {
        String num="";
        int aux=0;
        for (int i = 0; i < Mensaje.length(); i++) {
            if (Mensaje.charAt(i) == '/') {
                aux++;
                if(aux==c)
                {
                    return Integer.parseInt(num);
                }
                else
                {
                    num = "";
                }
            } else {
                num = num + Mensaje.charAt(i);
            }
        }
        return -1;  
    }
}
