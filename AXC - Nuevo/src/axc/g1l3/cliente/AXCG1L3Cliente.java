/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axc.g1l3.cliente;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ignacio
 */
public class AXCG1L3Cliente
{
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        String ip = "localhost";
        int numCli = 500;
        //System.out.println(numCli);
        Ventana ventanaInicial=new Ventana(ip, numCli); //Argumento: Numero de clientes
        ventanaInicial.setVisible(true);
    }
    
}
