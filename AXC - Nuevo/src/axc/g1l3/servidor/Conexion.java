/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axc.g1l3.servidor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ignacio
 */
public class Conexion
{

    private Socket TCP;
    private Integer PuertoUDP;
    private InetAddress IP;
    private int idCliente;
    private int estado;
    private ArrayList<Boolean> cuenta;
    private DatagramPacket UltimoEnviado;
    private byte[] UltimoDifundidoEnviado;

    Conexion(Socket TCP, int IDCliente)
    {
        this.TCP = TCP;
        IP = TCP.getInetAddress();
        idCliente = IDCliente;
        estado=0;
    }

    void setUDPPort(int puerto)
    {
        PuertoUDP = puerto;
    }

    int getUDPPort()
    {
        return PuertoUDP;
    }

    InetAddress getIP()
    {
        return IP;
    }

    void closeTCP()
    {
        try {
            TCP.close();
        } catch (IOException ex) {
        }
    }

    public int getIdCliente()
    {
        return idCliente;
    }

    public int getEstado()
    {
        return estado;
    }

    public void IniEstado()
    {
        this.estado = 1;
    }
    
    public void AvanzaEstado()
    {
        this.estado = estado+2;
    }
    
    public boolean SetCuenta(int i)
    {
        if(cuenta.get(i))
        {
            return false;
        }
        else
        {
            cuenta.set(i,true);
            return true;
        }
    }
    
    public int getCuenta()
    {
        int aux=1; //Ya se cuenta a si mismo que nunca será TRUE
        for(int i=0;i<cuenta.size();i++)
        {
            if(cuenta.get(i))
                aux++;
        }
        return aux;
    }

    void iniciarCuenta()
    {
        for(int i=0;i<cuenta.size();i++)
        {
            cuenta.set(i,false);
        }
    }

    public DatagramPacket getUltimoEnviado()
    {
        return UltimoEnviado;
    }

    public byte[] getUltimoDifundidoEnviado()
    {
        return UltimoDifundidoEnviado;
    }

    public void setUltimoEnviado(DatagramPacket UltimoEnviado)
    {
        this.UltimoEnviado = UltimoEnviado;
    }

    public void setUltimoDifundidoEnviado(byte[] UltimoDifundidoEnviado)
    {
        this.UltimoDifundidoEnviado = UltimoDifundidoEnviado;
    }
    
    
    
}

