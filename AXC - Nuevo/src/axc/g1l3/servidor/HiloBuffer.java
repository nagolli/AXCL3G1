/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axc.g1l3.servidor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ignacio
 */
public class HiloBuffer extends Thread
{

    ArrayList<DatagramPacket> cola;
    DatagramSocket UDP;

    public HiloBuffer(DatagramSocket UDP,ArrayList<DatagramPacket> cola)
    {
        this.UDP=UDP;
        this.cola=cola;
    }

    @Override
    public void run()
    {
        while (true) {
            try {
                recibir();
            } catch (Exception e) {
            }
        }
    }

    private void recibir()
    {
        DatagramPacket paqueteRecibido;
        byte[] mensajeEnBytes;

        //Recibir Mensaje UDP
        mensajeEnBytes = new byte[256];
        paqueteRecibido = new DatagramPacket(mensajeEnBytes, 256);
        try {
            UDP.receive(paqueteRecibido);
            cola.add(paqueteRecibido);
        } catch (SocketTimeoutException e) {
            System.out.println("No llegan mensajes");
        } catch (IOException ex) {
            Logger.getLogger(HiloBuffer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

}
