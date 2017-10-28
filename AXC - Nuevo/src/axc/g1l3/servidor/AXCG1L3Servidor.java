/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axc.g1l3.servidor;

import java.io.FileNotFoundException;

/**
 *
 * @author Ignacio
 */
public class AXCG1L3Servidor
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException
    {
        Ventana ventanaInicial=new Ventana(6,3,1); //Argumento: Clientes, clientes por sala, Iteraciones
    }
    
    public static void start(int n, int m,int i) throws FileNotFoundException
    {
        Ventana ventanaInicial=new Ventana(n,m,i); //Argumento: Clientes, clientes por sala, Iteraciones
    }
    
}
