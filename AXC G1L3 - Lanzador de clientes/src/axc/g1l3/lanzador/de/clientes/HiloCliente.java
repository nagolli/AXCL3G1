/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axc.g1l3.lanzador.de.clientes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import static java.lang.Math.random;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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
    private int minX;
    private int minY;
    private int maxX;
    private int maxY;
    Clientes Padre;
    Socket TCP;
    DatagramSocket UDP;
    int puerto_servidor;
    int cont;

    public HiloCliente(Clientes padre)
    {
        puerto_servidor = 1993;
        Padre = padre;
        minX = 0;
        minY = 0;
        maxX = 500;
        maxY = 500;
        x = (int) (random() % maxX);
        y = (int) (random() % maxY);
        cont = 0;
    }

    public void run()
    {
        try {
            
            //Conectar
            TCP = new Socket("localhost", puerto_servidor);
            String mensaje = "";
            //Configurar Sockets
            DataOutputStream enviar_datos = new DataOutputStream(TCP.getOutputStream());
            DataInputStream recibir_datos = new DataInputStream(TCP.getInputStream());
            
            Padre.AddSocket(TCP); //<- Si no va aqui, va antes del bucle
            //Recibir string
            mensaje=recibir_datos.readUTF();
            
            //Calcular aleatoriamente numero
            int aux = (int) (random()*100 % GetNum(mensaje));
            //Devolver numero
            enviar_datos.writeUTF(String.valueOf(aux));
            enviar_datos.flush();
            //Recibir ID
            int id=Integer.parseInt(recibir_datos.readUTF());
            
            UDP=new DatagramSocket();
            this.NuevaPosicion();
            //Bucle de envio/recepción
            while (CheckNotClosed(TCP)) {
                this.enviarCoordenadas(UDP, id, x, y);
                Mover();
                Thread.sleep(1000);
            }
        } catch (IOException ex) {
            Logger.getLogger(HiloCliente.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(HiloCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
        return;
    }

    void NuevaPosicion()
    {
        x=(int) ((maxX-minX)*random());
        y=(int) ((maxY-minY)*random());
    }
    
    void Mover()
    {
        int aux = (int) (random() % 9);
        if (aux < 3) {
            x = x - 1;
        }
        if (aux > 5) {
            x = x + 1;
        }
        if (x > maxX) {
            x = maxX;
        }
        if (x < minX) {
            x = minX;
        }
        if (aux == 1 || aux == 4 || aux == 7) {
            y = y - 1;
        }
        if (aux == 0 || aux == 3 || aux == 6) {
            y = y + 1;
        }
        if (y > maxY) {
            y = maxY;
        }
        if (y < minY) {
            y = minY;
        }
    }

    int GetNum(String mensaje)
    {
        int aux = 0;
        for (int i = 0; i < mensaje.length(); i++) {
            if (mensaje.charAt(i) == '/') {
                aux++;
            }
        }
        return aux;
    }
    
    private boolean CheckNotClosed(Socket con) throws IOException
    {
        if(con.isClosed())
        {
            return false;
        }
        if(cont==30)
        {
            cont=0;
        DataOutputStream enviar_datos = new DataOutputStream(con.getOutputStream());
        try {
            enviar_datos.writeUTF(" ");
            return true;
        } catch (IOException ex) {
            return false;
        }
        }
            cont++;
            return true;
    }

    private void enviarCoordenadas(DatagramSocket UDP, int id, int x,int y) throws UnknownHostException
    {
        String mensaje;
        DatagramPacket paquete;
        InetAddress direccion;
        byte[] mensaje_bytes;
        
        try 
        {
            direccion = InetAddress.getByName("localhost");
            mensaje = id + "/" + x + "/" + y + "/" + UDP.getLocalPort()+"/";
            mensaje_bytes = mensaje.getBytes();
            paquete = new DatagramPacket(mensaje_bytes, mensaje.length(), direccion, puerto_servidor);

            UDP.send(paquete);
            System.out.println("El cliente " + id + " envia sus coordenadas");
        }
        catch (IOException e) 
        {

        }
    }
    
}
