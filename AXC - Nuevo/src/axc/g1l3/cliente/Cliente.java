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
    
    /**
     * Constructor de la clase Cliente, con los parametros padre, de la clase 
     * Ventana, ip, que será la ip del cliente que se ha conectado, y numCli, que
     * indicara el numero del cliente que se acaba de conectar.
     * 
     * @param padre         Atributo de la clase Ventana.
     * @param ip            Ip del Cliente
     * @param numCli        Numero de cliente que despues se añadiran a una barrera
     *                      utilizaremos la clase CyclicBarrier para lanzar varios
     *                      hilos a la vez ya que con join() se haria muy pesado.
     */
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
        System.out.println(numCli);
        barrera = new CyclicBarrier(numCli);
    }

    /**
     * En esta función lanzamos los distintos hilos de los clientes, lo añadimos
     * al array y lo iniciamos. Si no lanzaremos una excepción mostrando un error
     * donde no se ha inicializado un hilo.
     * 
     * @param aux       Tamaño del array de hilos de Clientes.
     */
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

    /**
     * Esta función añade al atributo padre, de la clase Ventana, un mensaje de 
     * tipo String.
     * 
     * @param mensaje       Mensaje que se quiere introducir en la Ventana.
     */
    public void Print(String mensaje)
    {
        padre.AnadirTexto(mensaje);
    }
    /**
    *   Esta función elimina el mensaje que hay en la Ventana. 
    */
    public void Limpiar()
    {
        padre.QuitarTexto();
    }

    /**
     *  Esta función recoge la información que haya en el array de clientes.
     * 
     * @param hilo      El hilo de cada uno de los clientes.
     * 
     * @return          Devolvemos la información de los clientes, en caso contrario
     *                  devolveremos una cadena vacia.
     */
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
