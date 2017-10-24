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
import java.util.ArrayList;
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
    int id,idSala;
    DataOutputStream enviar_datos;
    DataInputStream recibir_datos;
    vistaCliente vista;
    ArrayList<Vecino> miembros_sala;

    public Cliente()
    {
        puerto_servidor = 1993;
        miembros_sala = new ArrayList();
    }

    public Cliente(Socket cliente, Cliente cl)
    {
        puerto_servidor = 1993;
        this.cliente = cliente;
        //address = cliente.getLocalAddress();
        host = "localhost";
        this.cl = cl;
        miembros_sala = new ArrayList();
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
            //System.out.println("Sala solicitada: "+i);
            enviar_datos.writeUTF(String.valueOf(i));
            enviar_datos.flush();
            //Recibir ID
            mensaje=recibir_datos.readUTF();
            id = Integer.parseInt(this.procesarMensaje(mensaje, 1));
            idSala = Integer.parseInt(this.procesarMensaje(mensaje, 2));
            UDP = new DatagramSocket();
            //Enviar Puerto UDP
            enviar_datos.writeUTF(String.valueOf(UDP.getLocalPort()));
            enviar_datos.flush();
            //Bucle
            new HiloEnviado(cliente, this, UDP).start();
            new HiloRecepcion(cliente, this, UDP).start();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void recibirVecino(int id, int x, int y)
    {
        try{
        boolean seguir = true;
        boolean encontrado=false;
        int i;
        for (i = 0; i < miembros_sala.size(); i++) {
            if (seguir) {
                encontrado = miembros_sala.get(i).Actualizar(id, x, y);
                if(encontrado==true)
                {
                    seguir=false;
                }
            }
        }
        if(!encontrado)
        {
            miembros_sala.add(new Vecino(id,x,y,this.id));
        }
        }
        catch(Exception e)
        {
            System.err.println("IOException " + e);
        }
        //System.out.println(miembros_sala);
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
    
    public ArrayList<Vecino> getVecinos()
    {
            return miembros_sala;
    }
    
    private String procesarMensaje(String Mensaje, int c)
    {
        String nombre="";
        int aux=1;
        for (int i = 0; i < Mensaje.length(); i++) {
            if (Mensaje.charAt(i) == '/') {
                if(aux==c)
                {
                    return nombre;
                }
                else
                {
                    nombre="";
                    aux++;
                }
            } else {
                nombre = nombre + Mensaje.charAt(i);
            }
        }
        return "";
        
    }

    int GetSala()
    {
        return this.idSala;
    }
}
