/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axc.g1l3.servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
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
    int idAsignada;
    InetAddress direccion;
    boolean almacenado;
    static int contador;
    int id;                     //Aun no se usa, pero podría ser necesario

    public static void setContador(int contador)
    {
        HiloServidor.contador = contador;
    }

    public HiloServidor(Socket socket, Servidor server)
    {
        almacenado = false;
        this.TCP = socket;
        direccion = TCP.getLocalAddress();
        this.server = server;
        id = contador;
        HiloServidor.contador = id + 1;
    }

    /*
    Codigo que ejecuta mientras se encuentre conectado
     */
    public void run()
    {
        try {
            /* Envia string de salas*/
            OutputStream outstream = TCP.getOutputStream();
            PrintWriter out = new PrintWriter(outstream);
            out.print(server.getGrupos());

            /* Espera hasta recibir int de sala*/
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(TCP.getInputStream()));
            String line = "";
            while ((line = inFromClient.readLine()) != null) {
            }
            int intSala = Integer.parseInt(line);

            /* Se agrega a sala */
            idAsignada = server.addConexion(TCP, intSala);

            /*Se pone en bucle, en espera retransmitiendo información hasta fin de conexión*/
            while (!TCP.isClosed()) {
                String mensaje;
                byte[] mensajeEnBytes = new byte[256];
                DatagramPacket PaqueteRecibido = new DatagramPacket(mensajeEnBytes, 256);
                UDP.setSoTimeout(20000);

                UDP.receive(PaqueteRecibido);
                if (!almacenado) {
                    server.addUDPdata(idAsignada, direccion, PaqueteRecibido);
                    almacenado = true;
                }

                mensaje = new String(mensajeEnBytes).trim();
                if (mensaje != null) {
                    distribuirCoordenadas(idAsignada, PaqueteRecibido);
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
    private void distribuirCoordenadas(int IDsala, DatagramPacket paqueteRecibido
    )
    {
        byte[] mensajeEnBytes;
        DatagramPacket paqueteEnviado;
        int portDest;
        InetAddress address;
        try {
            for (int i = 0; i < GetClientesSala(IDsala); i++) {
                mensajeEnBytes = paqueteRecibido.getData();
                portDest = GetPorCliente(IDsala, i);
                if (portDest > 0) {
                    address = GetDirCliente(IDsala, i);
                    if (address != null) {
                        paqueteEnviado = new DatagramPacket(mensajeEnBytes, mensajeEnBytes.length, address, portDest); // Envía
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

}
