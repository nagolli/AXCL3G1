/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axc.g1l3.cliente;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 *
 * @author Maria
 */
public class HiloRecepcion extends Thread
{

    private Socket TCP;
    Cliente padre;
    private DatagramSocket UDP;
    private byte[] mensaje_bytes;
    private DatagramPacket servPaquete;
    private String mensaje;
    int id;
    int x;
    int y;

    public HiloRecepcion(Socket TCP, Cliente padre, DatagramSocket UDP)
    {
        this.TCP = TCP;
        this.padre = padre;
        this.UDP = UDP;
    }

    public void run()
    {
        try {
            while (CheckNotClosed(TCP)) {
                UDP.setSoTimeout(20000);
                mensaje_bytes = new byte[256];
                servPaquete = new DatagramPacket(mensaje_bytes, 256);
                try {
                    UDP.receive(servPaquete);
                    mensaje = new String(mensaje_bytes).trim();
                    //  id/x/y
                    id=procesarMensaje(mensaje,1);
                    id=procesarMensaje(mensaje,2);
                    id=procesarMensaje(mensaje,3);
                    System.out.println(id+"/"+x+"/"+y);
                    
                    

                } catch (SocketTimeoutException e) {
                    TCP.close();
                }
            }
        } catch (Exception e) {
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
