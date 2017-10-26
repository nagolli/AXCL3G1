/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axc.g1l3.servidor;

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

    public Servidor(int cantidadClientes, int tamanoGrupos, int iteraciones, Ventana vista)
    {
        this.cantidadClientes = cantidadClientes;
        this.tamanoGrupos = tamanoGrupos;
        this.iteraciones = iteraciones;
        this.vista=vista;
        hilo = new HiloServidor(cantidadClientes,tamanoGrupos,iteraciones,puerto,this);
        hilo.AceptarConexiones();
        vista.Lanzar();
    }
    
    public void LanzarPrueba()
    {
            hilo.start();
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
