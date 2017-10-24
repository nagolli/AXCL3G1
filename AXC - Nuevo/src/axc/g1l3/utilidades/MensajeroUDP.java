/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axc.g1l3.utilidades;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ignacio
 */
public class MensajeroUDP
{
    static public int procesarMensajeInt(String Mensaje, int c)
    {
        String num="";
        int aux=0;
        for (int i = 0; i < Mensaje.length(); i++) {
            if (Mensaje.charAt(i) == '/') {
                aux++;
                if(aux==c)
                {
                    return Integer.parseInt(num);
                }
                else
                {
                    num = "";
                }
            } else {
                num = num + Mensaje.charAt(i);
            }
        }
        return -1;  
    }
    
    static public String procesarMensajeStr(String Mensaje, int c)
    {
        String cad="";
        int aux=0;
        for (int i = 0; i < Mensaje.length(); i++) {
            if (Mensaje.charAt(i) == '/') {
                aux++;
                if(aux==c)
                {
                    return cad;
                }
                else
                {
                    cad = "";
                }
            } else {
                cad = cad + Mensaje.charAt(i);
            }
        }
        return "";  
    }
    
    static public DatagramPacket EnviarMensaje1(DatagramSocket UDP,InetAddress IPCliente,int puertoCliente)
    {
        InetAddress direccion=IPCliente;
        String mensaje="1/";
        byte[] mensajeEnBytes=mensaje.getBytes();
        DatagramPacket paquete = new DatagramPacket(mensajeEnBytes,mensaje.length(),direccion,puertoCliente);
        
        try {
            UDP.send(paquete);
        } catch (IOException ex) {
            Logger.getLogger(MensajeroUDP.class.getName()).log(Level.SEVERE, null, ex);
        }
        return paquete;
    }
    
    static public void EnviarMensaje2(DatagramSocket UDP,InetAddress IPServidor,int puertoServidor,int idcliente,int idSala,int x, int y)
    {
        InetAddress direccion=IPServidor;
        String mensaje="2/"+idcliente+"/"+"/"+idSala+"/"+x+"/"+y;
        byte[] mensajeEnBytes=mensaje.getBytes();
        DatagramPacket paquete = new DatagramPacket(mensajeEnBytes,mensaje.length(),direccion,puertoServidor);
        
        try {
            UDP.send(paquete);
        } catch (IOException ex) {
            Logger.getLogger(MensajeroUDP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void EnviarMensaje3(DatagramSocket UDP, InetAddress IPDestino, int puertoDestino, byte[] mensajeEnBytes)
    {
        InetAddress direccion=IPDestino;
        DatagramPacket paquete = new DatagramPacket(mensajeEnBytes,mensajeEnBytes.length,direccion,puertoDestino);
        
        try {
            UDP.send(paquete);
        } catch (IOException ex) {
            Logger.getLogger(MensajeroUDP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    static public void EnviarMensaje4(DatagramSocket UDP,InetAddress IPServidor,int puertoServidor,int idDestino, int idOrigen,int idSala)
    {
        InetAddress direccion=IPServidor;
        String mensaje="4/"+idDestino+"/"+"/"+idSala+"/"+idOrigen+"/";
        byte[] mensajeEnBytes=mensaje.getBytes();
        DatagramPacket paquete = new DatagramPacket(mensajeEnBytes,mensaje.length(),direccion,puertoServidor);
        
        try {
            UDP.send(paquete);
        } catch (IOException ex) {
            Logger.getLogger(MensajeroUDP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    static public DatagramPacket EnviarMensaje5(DatagramSocket UDP,InetAddress IPCliente,int puertoCliente, int idOrigen)
    {
        InetAddress direccion=IPCliente;
        String mensaje="5/"+idOrigen+"/";
        byte[] mensajeEnBytes=mensaje.getBytes();
        DatagramPacket paquete = new DatagramPacket(mensajeEnBytes,mensaje.length(),direccion,puertoCliente);
        
        try {
            UDP.send(paquete);
        } catch (IOException ex) {
            Logger.getLogger(MensajeroUDP.class.getName()).log(Level.SEVERE, null, ex);
        }
        return paquete;
    }
    
    static public void EnviarMensaje6(DatagramSocket UDP,InetAddress IPServidor,int puertoServidor,int idCliente,int idSala, int idRecibida)
    {
        InetAddress direccion=IPServidor;
        String mensaje="6/"+idCliente+"/"+idSala+"/"+idRecibida+"/";
        byte[] mensajeEnBytes=mensaje.getBytes();
        DatagramPacket paquete = new DatagramPacket(mensajeEnBytes,mensaje.length(),direccion,puertoServidor);
        
        try {
            UDP.send(paquete);
        } catch (IOException ex) {
            Logger.getLogger(MensajeroUDP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    static public void EnviarMensaje7(DatagramSocket UDP,InetAddress IPCliente,int puertoCliente,int idCliente,float latencia)
    {
        InetAddress direccion=IPCliente;
        String mensaje="7/"+idCliente+"/"+Float.toString(latencia)+"/";
        byte[] mensajeEnBytes=mensaje.getBytes();
        DatagramPacket paquete = new DatagramPacket(mensajeEnBytes,mensaje.length(),direccion,puertoCliente);
        
        try {
            UDP.send(paquete);
        } catch (IOException ex) {
            Logger.getLogger(MensajeroUDP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    static public void EnviarMensaje8(DatagramSocket UDP,InetAddress IPCliente,int puertoCliente)
    {
        InetAddress direccion=IPCliente;
        String mensaje="8/";
        byte[] mensajeEnBytes=mensaje.getBytes();
        DatagramPacket paquete = new DatagramPacket(mensajeEnBytes,mensaje.length(),direccion,puertoCliente);
        
        try {
            UDP.send(paquete);
        } catch (IOException ex) {
            Logger.getLogger(MensajeroUDP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    
    
}

