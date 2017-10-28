/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axc.g1l3.cliente;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ignacio
 */
public class Cliente
{

    private ArrayList<HiloCliente> clientes;
    private InetAddress ip;
    private int port;
    Ventana padre;
    CyclicBarrier barrera;
    
    Cliente(Ventana padre, String ip, int numCli)
    {
        clientes = new ArrayList<>();
        this.padre = padre;
        try {
            this.ip=InetAddress.getByName(ip);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
        port = 1993;
        
        barrera = new CyclicBarrier(numCli);
    }

    void lanzar(int aux)
    {
        try {
            
            for (int i = 0; i < aux; i++) {
                System.out.println(ip+":"+port);
                HiloCliente hilo = new HiloCliente(ip, port,this,barrera);
                clientes.add(hilo);
                hilo.start();
            }
        } catch (Exception e) {
            System.out.println("Error al lanzar hilo: "+e);
        }
    }

    public void Print(String mensaje)
    {
        padre.AnadirTexto(mensaje);
    }

    public void Limpiar()
    {
        padre.QuitarTexto();
    }

    String getInfo(Integer hilo)
    {
        try{
        return clientes.get(hilo).toString();
        }
        catch(Exception e)
        {
            return "";
        }
    }
}
