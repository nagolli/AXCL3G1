/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axc.g1l3.cliente;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author Maria
 */
public class HiloEnviado extends Thread
{

    private Socket TCP;
    Cliente padre;
    private DatagramSocket UDP;
    int puerto_servidor;

    public HiloEnviado(Socket TCP, Cliente padre, DatagramSocket UDP)
    {
        this.TCP = TCP;
        this.padre = padre;
        this.UDP = UDP;
        puerto_servidor=1993;
    }

    public void run()
    {
        try {
            while (CheckNotClosed(TCP)) {
                enviarCoordenadas(UDP, padre.getId(), padre.getX(), padre.getY());
                Thread.sleep(1000);
            }
        } catch (Exception e) {
        }
    }

    public void enviarCoordenadas(DatagramSocket UDP, int id, int x, int y) throws UnknownHostException
    {
        String mensaje;
        DatagramPacket paquete;
        InetAddress direccion;
        byte[] mensaje_bytes;

        try {
            ///< Esto manda la localización
            direccion = InetAddress.getByName("localhost");
            mensaje = id + "/" + x + "/" + y + "/" + UDP.getLocalPort()+"/";
            mensaje_bytes = mensaje.getBytes();
            paquete = new DatagramPacket(mensaje_bytes, mensaje.length(), direccion, puerto_servidor);

            UDP.send(paquete);                ///< Envio paquete
        } catch (IOException e) {

        }
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
}
