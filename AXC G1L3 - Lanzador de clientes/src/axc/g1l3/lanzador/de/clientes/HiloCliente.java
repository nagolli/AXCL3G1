/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axc.g1l3.lanzador.de.clientes;

import static java.lang.Math.random;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ignacio
 */
public class HiloCliente extends Thread
{
    private int x;
    private int y;
    private int maxX;
    private int maxY;
    Clientes Padre;
    Socket TCP;
    
    public HiloCliente(Clientes padre)
    {
        Padre=padre;
        maxX=500;
        maxY=500;
        x=(int) (random()%maxX);
        y=(int) (random()%maxY);
    }
    
    
    public void run()
    {
        TCP=new Socket();
        String mensaje="";
        //Configurar Sockets
        
        
        Padre.AddSocket(TCP); //<- Si no va aqui, va antes del bucle
        //Conectar
        
        //Recibir string
        
        //Calcular aleatoriamente numero
        int aux=(int) (random()%GetNum(mensaje));
        //Devolver numero
        
        //Bucle de envio/recepción
        while (!TCP.isClosed()) {
        
        
        
        }
        return;
    }
    
    void Mover()    //<-Aun no se ha usado, pero se puede poner en algun sitio
    {
        int aux = (int) (random()%9);
        if(aux<3)
            x=x-1;
        if(aux>5)
            x=x+1;
        if(x<0)
            x=0;
        if(x>maxX)
            x=maxX;
        if(aux==1||aux==4||aux==7)
            y=y-1;
        if(aux==0||aux==3||aux==6)
            y=y+1;
        if(y<0)
            y=0;
        if(y>maxY)
            y=maxY;
        
        /* PAUSA NO SE SI SE USARA
        aux=(int) (random()%1000)+500; 
        try {
            Thread.sleep(aux);
        } catch (InterruptedException ex) {
            Logger.getLogger(HiloCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
        */
    }
    
    int GetNum(String mensaje){
        int aux=0;
    for (int i = 0; i < mensaje.length(); i++) {
            if (mensaje.charAt(i) == '/') {
                 aux++;
            }
        }
    return aux;
}
    
}
