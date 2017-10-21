/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axc.g1l3.lanzador.de.clientes;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author Ignacio
 */
public class Clientes
{
    ArrayList<Socket> Listado;
    
    Clientes()
    {
        Listado=new ArrayList();
    }
    
    void AddSocket(Socket TCP)
    {
        Listado.add(TCP);
    }
    
    void lanzar(int value)
    {
        for(int i=0;i<value;i++)
        {
            new HiloCliente(this).start();
        }
    }

    void apagar() throws IOException
    {
        for(int i=0;i<Listado.size();i++)
        {
            Listado.get(i).close();
        }
    }

    void apagar(int value) throws IOException
    {
        for(int i=0;(i<value)&&(Listado.size()>0);i++)
        {
            Listado.get(Listado.size()-1).close();
            Listado.remove(Listado.size()-1);
        }
    }
    
    
    
}
