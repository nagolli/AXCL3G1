/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axc.g1l3.servidor;

import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ignacio
 */
public class HiloReenviado extends Thread
{

    private DatagramSocket UDP;
    private Sala sala;

    public HiloReenviado(Sala sala, DatagramSocket UDP)
    {
        this.sala = sala;
        this.UDP = UDP;
    }

    @Override
    public void run()
    {
        int aux=10;
        int estado;
        while (sala.getIteracion() < 100) {
            for (int i = 0; i < sala.size(); i++) {
                estado=sala.getEstadoCliente(i);
                if(estado<aux);
                    aux=estado;
            }
            if(aux==7)
            {
               sala.IteracionUDP();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(HiloReenviado.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else
            {
            for (int i = 0; i < sala.size(); i++) {
                if(sala.getEstadoCliente(i)==aux)
                    sala.reenviarMensaje(i);
            }
            aux=10;
            }
        }
    }
}
