/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axc.g1l3.servidor;

import axc.g1l3.utilidades.MensajeroUDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ignacio
 */
public class Sala
{

    private ArrayList<Conexion> conexiones;
    private DatagramSocket UDP;
    private int idSala;
    private int latenciasRecibidas;
    private Float latencia;
    private int iteracion;

    public Sala(int id, DatagramSocket UDP)
    {
        conexiones = new ArrayList();
        idSala = id;
        this.UDP = UDP;
        latencia = 0f;
        latenciasRecibidas = 0;
        iteracion = 0;
    }

    public int getIdSala()
    {
        return idSala;
    }

    public int getIteracion()
    {
        return iteracion;
    }

    public void IteracionUDP()
    {
        for (int i = 0; i < conexiones.size(); i++) {
            conexiones.get(i).iniciarCuenta();
            conexiones.get(i).IniEstado();
            conexiones.get(i).setUltimoEnviado(MensajeroUDP.EnviarMensaje1(UDP, conexiones.get(i).getIP(), conexiones.get(i).getUDPPort()));
        }
        iteracion++;
    }

    public void iniciarIteracionUDP()
    {

        for (int i = 0; i < conexiones.size(); i++) {
            conexiones.get(i).iniciarCuenta();
            conexiones.get(i).IniEstado();
            conexiones.get(i).setUltimoEnviado(MensajeroUDP.EnviarMensaje1(UDP, conexiones.get(i).getIP(), conexiones.get(i).getUDPPort()));
        }
        iteracion++;
        new HiloReenviado(this, UDP).start();
    }

    int GetPuerto(int indice)
    {
        return conexiones.get(indice).getUDPPort();
    }

    InetAddress GetIP(int indice)
    {
        return conexiones.get(indice).getIP();
    }

    int size()
    {
        return conexiones.size();
    }

    void addUDPPort(InetAddress dir, int puerto)
    {
        for (int k = 0; k < conexiones.size(); k++) {
            if (dir.equals(conexiones.get(k).getIP())) {
                if (conexiones.get(k).getUDPPort() == 0) {
                    conexiones.get(k).setUDPPort(puerto);
                    return;
                }
            }
        }
    }

    void AddConexion(Socket TCP, int idCliente)
    {
        conexiones.add(new Conexion(TCP, idCliente));
    }

    boolean checkCuenta(int idCliente)
    {
        int aux = -1;
        for (int i = 0; i < conexiones.size(); i++) {
            if (idCliente == conexiones.get(i).getIdCliente()) {
                aux = conexiones.get(i).getCuenta();
            }
        }
        return (conexiones.size() == aux);
    }

    void setCuenta(int idCliente, int SegundoCliente)
    {
        int aux1 = -1;
        int aux2 = -1;

        for (int i = 0; i < conexiones.size(); i++) {
            if (idCliente == conexiones.get(i).getIdCliente()) {
                aux1 = i;
            }
            if (SegundoCliente == conexiones.get(i).getIdCliente()) {
                aux2 = i;
            }
        }
        conexiones.get(aux1).SetCuenta(aux2);
    }

    int getEstadoCliente(int idCliente)
    {
        for (int i = 0; i < conexiones.size(); i++) {
            if (idCliente == conexiones.get(i).getIdCliente()) {
                return conexiones.get(i).getEstado();
            }
        }
        return 0;
    }

    int getEstadoSlot(int j)
    {
        return conexiones.get(j).getEstado();
    }

    void estadoClienteMas(int idCliente)
    {
        for (int i = 0; i < conexiones.size(); i++) {
            if (idCliente == conexiones.get(i).getIdCliente()) {
                conexiones.get(i).AvanzaEstado();
                return;
            }
        }
    }

    public Float getLatencia()
    {
        return latencia;
    }

    void addLatencia(float latencia)
    {
        this.latencia += latencia;
        latenciasRecibidas++;
        if (latenciasRecibidas == conexiones.size()) {
            latencia = latencia / latenciasRecibidas;
        }
    }

    void reenviarMensaje(int i)
    {
        int estado = conexiones.get(i).getEstado();
        {
            switch (estado) {
                case 1:
                    try {
                        UDP.send(conexiones.get(i).getUltimoEnviado());
                    } catch (IOException ex) {
                        Logger.getLogger(MensajeroUDP.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                case 3:
                    for (int j = 0; j < conexiones.size(); j++) {
                        MensajeroUDP.EnviarMensaje3(UDP, conexiones.get(j).getIP(), conexiones.get(j).getUDPPort(), conexiones.get(i).getUltimoDifundidoEnviado());
                    }

                    break;
                case 5:
                    try {
                        UDP.send(conexiones.get(i).getUltimoEnviado());
                    } catch (IOException ex) {
                        Logger.getLogger(MensajeroUDP.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
            }
        }
    }

    void setUltimoMensajeEnviado(DatagramPacket paquete, byte[] mensajeEnBytes, int IdHilo)
    {
        for (int i = 0; i < conexiones.size(); i++) {
            if (paquete == null) {
                conexiones.get(i).setUltimoDifundidoEnviado(mensajeEnBytes);
            } else {
                conexiones.get(i).setUltimoEnviado(paquete);
            }
        }
    }
}
