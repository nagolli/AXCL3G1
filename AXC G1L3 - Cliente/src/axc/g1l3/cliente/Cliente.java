/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axc.g1l3.cliente;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import static jdk.nashorn.internal.runtime.Debug.id;

/**
 *
 * @author Maria
 */
class Cliente
{

    Cliente cl;
    //Definimos el socket
    DatagramSocket UDP;
    //paquete que vamos a enviar
    DatagramPacket paquete;
    private static int puerto_servidor;
    private static Socket cliente;
    private static InetAddress address;
    private static String host;
    private static String grupos;
    private static vistaCliente cv;
    byte[] mensaje_bytes = new byte[256];
    String mensaje_enviado = "";
    String mensaje = "";
    byte mensaje_recibido;
    int id;
    DataOutputStream enviar_datos;
    DataInputStream recibir_datos;
    vistaCliente vista;

    public Cliente()
    {
        puerto_servidor = 1993;
    }

    public Cliente(Socket cliente, Cliente cl)
    {
        puerto_servidor = 1993;
        this.cliente = cliente;
        //address = cliente.getLocalAddress();
        host = "localhost";
        this.cl = cl;
    }

    public void Conectar(vistaCliente vista)
    {
        this.vista = vista;
        vista.DesconectarSala();
        try {
            //Determina la IP del host, mediante el nombre del host
            //address = InetAddress.getByName(host);
            //Mostramos por pantalla si se conecta o no al servidor
            System.out.println("Conectandose al servidor...");
            /**
             * Inicializamos la variable cliente como un nuevo socket donde
             * tendremos la información de la dirección IP y el puerto.
             */

            cliente = new Socket("localhost", puerto_servidor);

            System.out.println("Cliente " + cliente);
            //TCP
            enviar_datos = new DataOutputStream(cliente.getOutputStream());
            recibir_datos = new DataInputStream(cliente.getInputStream());
            /*
            enviar_datos.writeUTF(mensaje_enviado);
            enviar_datos.flush();
            mensaje=recibir_datos.readUTF();
             */
            grupos = recibir_datos.readUTF();
            vista.setListaDesplegable(grupos);
            vista.ActivarSeleccionSala();
        } /**
         * Si el host no es conocido mostrara por pantalla que se desconoce el
         * host.
         */
        catch (UnknownHostException h) {
            System.out.println("Host no conocido");
        } /**
         * En este caso usamos err,en vez de out, para desplegar un mensaje de
         * error y nos indica que la IP no es valida o el puerto no es válido
         */
        catch (IOException e) {
            System.err.println("IOException " + e);
            vista.ConectarServidor();
        }
    }
    // Envia al Servidor el número de grupo seleccionado

    public void enviarGrupoServidor(int n_grupo)
    {
        System.out.println("Grupo " + n_grupo + " seleccionado. Enviando información al servidor...");
    }

    // Función que guarda los grupos disponibles que le ha pasado el servidor
    public static void setGruposDisponibles(String grupos)
    {
        grupos = grupos;
        System.out.println("g" + grupos);
    }

    // Función que devuelve los grupos disponibles
    public String getGruposDisponibles()
    {
        return this.grupos;
    }

    public boolean seleccionadoGrupo(int i)
    {
        try {
            enviar_datos.writeUTF(String.valueOf(i));
            enviar_datos.flush();
            //Recibir ID
            id = Integer.parseInt(recibir_datos.readUTF());
            UDP = new DatagramSocket();
            new HiloEnviado(cliente, this, UDP).start();
            new HiloRecepcion(cliente,this,UDP).start();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Cliente getCl()
    {
        return cl;
    }

    public void setCl(Cliente cl)
    {
        this.cl = cl;
    }

    public DatagramSocket getUDP()
    {
        return UDP;
    }

    public void setUDP(DatagramSocket UDP)
    {
        this.UDP = UDP;
    }

    public DatagramPacket getPaquete()
    {
        return paquete;
    }

    public void setPaquete(DatagramPacket paquete)
    {
        this.paquete = paquete;
    }

    public static int getPUERTO()
    {
        return puerto_servidor;
    }

    public static void setPUERTO(int PUERTO)
    {
        Cliente.puerto_servidor = PUERTO;
    }

    public static Socket getCliente()
    {
        return cliente;
    }

    public static void setCliente(Socket cliente)
    {
        Cliente.cliente = cliente;
    }

    public static InetAddress getAddress()
    {
        return address;
    }

    public static void setAddress(InetAddress address)
    {
        Cliente.address = address;
    }

    public static String getHost()
    {
        return host;
    }

    public static void setHost(String host)
    {
        Cliente.host = host;
    }

    public static String getGrupos()
    {
        return grupos;
    }

    public static void setGrupos(String grupos)
    {
        Cliente.grupos = grupos;
    }

    public static vistaCliente getCv()
    {
        return cv;
    }

    public static void setCv(vistaCliente cv)
    {
        Cliente.cv = cv;
    }

    public byte[] getMensaje_bytes()
    {
        return mensaje_bytes;
    }

    public void setMensaje_bytes(byte[] mensaje_bytes)
    {
        this.mensaje_bytes = mensaje_bytes;
    }

    public int getId()
    {
        return id;
    }

    public int getX()
    {
        return (vista.getPosX());
    }

    public int getY()
    {
        return (vista.getPosY());
    }
}
