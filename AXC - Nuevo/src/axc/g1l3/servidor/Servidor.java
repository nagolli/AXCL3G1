/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axc.g1l3.servidor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ignacio
 */

/*
Clase servidor, desde aqui se conecta a la vista y 
 es clase padre de todos los hilos

 */
public class Servidor
{

    private int clientes, maxClientes;
    private int salas;
    private String grupos;
    private Ventana vista;
    private static int PORT;
    private boolean Aceptando, fin;
    private DatagramSocket UDP = null;

    private ArrayList< ArrayList<Sala>> DatosSalas;
    private int temas;
    private int iteracion;

    /*
    Constructor, inicializa la estructura interna y lee el string con los grupos
    Llamado desde Constructor de Ventana
     */
    Servidor(int max, Ventana vista, int temas) throws FileNotFoundException
    {
        Aceptando = true;
        PORT = 1993;
        clientes = 0;
        salas = 0;
        iteracion = 0;
        maxClientes = max;
        this.vista = vista;
        HiloServidor.setContador(1);
        this.temas = temas;

        DatosSalas = new ArrayList();
        for (int i = 0; i < temas; i++) {
            DatosSalas.add(new ArrayList());
        }
    }

    /*
    Inicio de servidor, llamado desde Constructor de Ventana
     */
    public void encender()
    {
        ServerSocket serverSocket = null;
        Socket TCP = null;
        UDP = null;

        /*
        Creación del Socket servidor
         */
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();

        }

        /*
        Gestion de las conexiones TCP y inicializado de cada hilo respectivo
         */
        try {
            UDP = new DatagramSocket(1993);
            while (Aceptando) {
                try {
                    TCP = serverSocket.accept();
                    clientes++;
                    vista.SetClientes(clientes);

                } catch (IOException e) {
                    System.out.println("I/O error: " + e);
                }
                // Nuevo hilo para cada cliente
                new HiloServidor(TCP, this, UDP).start();
                try {
                    Thread.sleep(10); //Para evitar que todos los hilos entren simultaneamente
                } catch (InterruptedException ex) {
                    Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (SocketException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);

        }
    }

    /*
    Get puerto de servidor
     */
    public int getPuerto()
    {
        return PORT;
    }

    /*
    Setter de aceptando, para impedir más conexiones
     */
    public void switchAceptando()
    {
        Aceptando = !Aceptando;
    }

    /*
    Get grupos, string con las posibles salas
    Formato: nombre/nombre/nombre
    Llamado desde HiloServidor en el Run
     */
    public String getGrupos()
    {
        return grupos;
    }

    public int getTemas()
    {
        return temas;
    }

    /*
    Apagar, cierra la conexión con todos los sockets TCP abiertos
    Llamado desde la vista al cerrar la aplicación
     */
    public void iniciarUDP() throws IOException
    {
        fin = false;
        Aceptando = false;
        try {
            Thread.sleep(5000); //Espera 5 segundos de cortesia por si alguien aun esta conectandose, 
        } catch (InterruptedException ex) {         //pero no acepta nuevas conexiones en este tiempo
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (int i = 0; i < DatosSalas.size(); i++) {
            for (int j = 0; j < DatosSalas.get(i).size(); j++) {
                DatosSalas.get(i).get(j).iniciarIteracionUDP();
            }
        }

        while (!checkAcabado()) //Cada segundo comprueba si se ha acabado
        {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        float aux=0f;
         for (int i = 0; i < DatosSalas.size(); i++) {
            for (int j = 0; j < DatosSalas.get(j).size(); j++) {
                vista.printLatencia("Sala "+DatosSalas.get(i).get(j).getIdSala()+" latencia de: "+DatosSalas.get(i).get(j).getLatencia()+" s con "+DatosSalas.get(i).get(j).size()+"clientes\n");
                aux=DatosSalas.get(i).get(j).getLatencia();
            }
        }
        vista.printLatencia("Media de salas: "+aux/salas+" s\n");

    }

    /*
    Funcion que obtiene el puerto de un cliente previamente conectado
    Se llama desde HiloServidor, distribuir coordenadas
     */
    public int GetPorClienteUDP(int IDsala, int indice)
    {
        for (int i = 0; i < DatosSalas.size(); i++) {
            for (int j = 0; j < DatosSalas.get(i).size(); j++) {
                if (IDsala == DatosSalas.get(i).get(j).getIdSala()) {
                    return DatosSalas.get(i).get(j).GetPuerto(indice);
                }
            }
        }
        return 0;
    }

    /*
    Funcion que obtiene la dirección de un cliente previamente conectado
    Se llama desde HiloServidor, distribuir coordenadas
     */
    public InetAddress GetDirCliente(int IDsala, int indice)
    {
        for (int i = 0; i < DatosSalas.size(); i++) {
            for (int j = 0; j < DatosSalas.get(i).size(); j++) {
                if (IDsala == DatosSalas.get(i).get(j).getIdSala()) {
                    return DatosSalas.get(i).get(j).GetIP(indice);
                }
            }
        }

        return null;
    }

    /*
    Funcion que devuelve la cantidad de clientes en una sala
    Se llama desde HiloServidor, distribuir coordenadas
     */
    public int GetClientesSala(int IDsala)
    {
        for (int i = 0; i < DatosSalas.size(); i++) {
            for (int j = 0; j < DatosSalas.get(i).size(); j++) {
                if (IDsala == DatosSalas.get(i).get(j).getIdSala()) {
                    return DatosSalas.get(i).get(j).size();
                }
            }

        }
        return 0;
    }

    /*
    Funcion que añade a la estructura una nueva conexión.
    Se llama desde HiloServidor en el Run
     */
    public int addConexion(Socket TCP, int tema, int idCliente)
    {
        /*
        Si hay espacio pondrá el socket en una sala existente
         */
        for (int j = 0; j < DatosSalas.get(tema).size(); j++) {
            if (DatosSalas.get(tema).get(j).size() < maxClientes) {
                DatosSalas.get(tema).get(j).AddConexion(TCP, idCliente);
                return DatosSalas.get(tema).get(j).getIdSala();
            }
        }
        /*
        Si no hay espacio creará una nueva sala y añadirá el socket
         */
        salas++;
        DatosSalas.get(tema).add(new Sala(salas, UDP));
        vista.SetSalas(salas);
        DatosSalas.get(tema).get(DatosSalas.get(tema).size() - 1).AddConexion(TCP, idCliente);
        return salas; //nueva ID de sala será la maxima cantidad de salas
    }

    /*
    
     */
    public void addUDPPort(int IDsala, InetAddress dir, int puerto)
    {

        for (int i = 0; i < DatosSalas.size(); i++) {
            for (int j = 0; j < DatosSalas.get(i).size(); j++) {
                if (IDsala == DatosSalas.get(i).get(j).getIdSala()) {
                    DatosSalas.get(i).get(j).addUDPPort(dir, puerto);
                    return;
                }
            }
        }
    }

    int getIteracion()
    {
        return this.iteracion;
    }

    void IteracionSiguiente()
    {
        iteracion++;
    }

    int getEstadoCliente(int idCliente, int idSala)
    {
        for (int i = 0; i < DatosSalas.size(); i++) {
            for (int j = 0; j < DatosSalas.get(i).size(); j++) {
                if (idSala == DatosSalas.get(i).get(j).getIdSala()) {
                    return DatosSalas.get(i).get(j).getEstadoCliente(idCliente);
                }
            }
        }
        return 0;
    }

    void estadoClienteMas(int idCliente, int idSala)
    {
        for (int i = 0; i < DatosSalas.size(); i++) {
            for (int j = 0; j < DatosSalas.get(i).size(); j++) {
                if (idSala == DatosSalas.get(i).get(j).getIdSala()) {
                    DatosSalas.get(i).get(j).estadoClienteMas(idCliente);
                    return;
                }
            }
        }
    }

    void setClienteRecibido(int IDSala, int idCliente, int SegundaID)
    {
        for (int i = 0; i < DatosSalas.size(); i++) {
            for (int j = 0; j < DatosSalas.get(i).size(); j++) {
                if (IDSala == DatosSalas.get(i).get(j).getIdSala()) {
                    DatosSalas.get(i).get(j).setCuenta(idCliente, idCliente);
                    return;
                }
            }
        }
    }

    void AnadirLatencia(float latencia, int idSala)
    {
        for (int i = 0; i < DatosSalas.size(); i++) {
            for (int j = 0; j < DatosSalas.get(i).size(); j++) {
                if (idSala == DatosSalas.get(i).get(j).getIdSala()) {
                    DatosSalas.get(i).get(j).addLatencia(latencia);
                }
            }
        }
    }

    void setUltimoMensajeEnviado(DatagramPacket paquete, byte[] mensajeEnBytes, int IDsala, int IdHilo)
    {
        for (int i = 0; i < DatosSalas.size(); i++) {
            for (int j = 0; j < DatosSalas.get(i).size(); j++) {
                if (IDsala == DatosSalas.get(i).get(j).getIdSala()) {
                    DatosSalas.get(i).get(j).setUltimoMensajeEnviado(paquete, mensajeEnBytes, IdHilo);
                }
            }
        }

    }

    boolean checkAcabado()
    {
        for (int i = 0; i < DatosSalas.size(); i++) {
            for (int j = 0; j < DatosSalas.get(j).size(); j++) {
                if (DatosSalas.get(i).get(j).getIteracion() < 100) {
                    return false;
                }
            }
        }
        return true;
    }

}
