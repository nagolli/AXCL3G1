/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axc.g1l3.servidor;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ignacio
 */
public class Servidor
{

    private final int cantidadClientes;
    private final int tamanoGrupos;
    private final int iteraciones;
    private HiloServidor hilo;
    private final int puerto=1993;
    private Ventana vista;
    ArrayList<DatagramPacket> Paquetes,Pendientes;
    ArrayList<String> Mensajes,MPendientes;
    DatagramSocket UDP;

    public Servidor(int cantidadClientes, int tamanoGrupos, int iteraciones, Ventana vista)
    {
        this.cantidadClientes = cantidadClientes;
        this.tamanoGrupos = tamanoGrupos;
        this.iteraciones = iteraciones;
        this.vista=vista;
        Paquetes = new ArrayList();
        Mensajes = new ArrayList();
        Pendientes = new ArrayList();
        MPendientes = new ArrayList();
        try {
            UDP = new DatagramSocket(1993);
        } catch (SocketException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
        hilo = new HiloServidor(cantidadClientes,tamanoGrupos,iteraciones,puerto,this,Paquetes,Mensajes,UDP,true,Pendientes,MPendientes);
        //Se pueden crear más hilos, pero no aceptan conexiones y el valo booleano debe ser false
        hilo.AceptarConexiones();
        vista.Lanzar();

    }
    
    public void LanzarPrueba()
    {
            hilo.start();
            //Si hay más hilos lanzarlos aqui
    }

    void PrintIteracion(int i)
    {
        vista.SetIteraciones(i);
        vista.print("--------------------------------------------------------\n");
    }

    void print(String string)
    {
        vista.print(string);
    }

    void clienteMas(int i)
    {
        vista.SetClientes(i);
    }
    

}
