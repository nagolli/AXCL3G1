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
import java.util.concurrent.locks.ReentrantLock;
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
    private ArrayList<HiloServidor> hilo;
    private final int puerto=1993;
    private Ventana vista;
    ArrayList<DatagramPacket> Pendientes;
    ArrayList<String> MPendientes;
    ArrayList<ArrayList<DatagramPacket>> Paquetes;
    ArrayList<ArrayList<String>> Mensajes;
    DatagramSocket UDP;
    ArrayList<ReentrantLock> Locks;
    ArrayList<ArrayList<Long>> Latencias;

    public Servidor(int cantidadClientes, int tamanoGrupos, int iteraciones, Ventana vista)
    {
        hilo=new ArrayList();
        Locks=new ArrayList();
        Locks.add(new ReentrantLock());
        Locks.add(new ReentrantLock());
        Locks.add(new ReentrantLock());
        this.cantidadClientes = cantidadClientes;
        this.tamanoGrupos = tamanoGrupos;
        this.iteraciones = iteraciones;
        this.vista=vista;
        int num=cantidadClientes/tamanoGrupos;
        Paquetes = new ArrayList<>();
        Mensajes = new ArrayList<>();
        Latencias = new ArrayList<>();
        for(int i=0;i<num;i++)
        {
            Paquetes.add(new ArrayList());
            Mensajes.add(new ArrayList());
            Latencias.add(new ArrayList());
        }
        Pendientes = new ArrayList();
        MPendientes = new ArrayList();
        try {
            UDP = new DatagramSocket(1993);
        } catch (SocketException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0);
        }
        hilo.add(new HiloServidor(cantidadClientes,tamanoGrupos,iteraciones,puerto,this,Paquetes,Mensajes,UDP,true,Pendientes,MPendientes,Locks,Latencias));
        //Se pueden crear más hilos, pero no aceptan conexiones y el valo booleano debe ser false
        hilo.add(new HiloServidor(cantidadClientes,tamanoGrupos,iteraciones,puerto,this,Paquetes,Mensajes,UDP,false,Pendientes,MPendientes,Locks,Latencias));
        hilo.add(new HiloServidor(cantidadClientes,tamanoGrupos,iteraciones,puerto,this,Paquetes,Mensajes,UDP,false,Pendientes,MPendientes,Locks,Latencias));
        hilo.add(new HiloServidor(cantidadClientes,tamanoGrupos,iteraciones,puerto,this,Paquetes,Mensajes,UDP,false,Pendientes,MPendientes,Locks,Latencias));
        hilo.get(0).AceptarConexiones();
        vista.Lanzar();

    }
    
    public void LanzarPrueba()
    {
        for(int i=0;i<hilo.size();i++)
            hilo.get(i).start();
            //Si hay más hilos lanzarlos aqui
    }

    void PrintIteracion(int i)
    {
        vista.SetIteraciones(i);
        vista.print("--------------------------------------------------------\n");
    }

    void print(String string)
    {
        try{
        Locks.get(2).lock();
        vista.print(string);
        }catch(Exception e){}finally{
        Locks.get(2).unlock();
        }
    }

    void clienteMas(int i)
    {
        vista.SetClientes(i);
    }
    

}
