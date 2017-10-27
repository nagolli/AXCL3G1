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
        Ventana ventanaInicial=new Ventana(10,10,1); //Argumento: Maximo clientes en una sala, Temas
    }
    
    public static void start(int n, int m,int i) throws FileNotFoundException
    {
        Ventana ventanaInicial=new Ventana(n,m,i); //Argumento: Maximo clientes en una sala, Temas
    }
    
}
