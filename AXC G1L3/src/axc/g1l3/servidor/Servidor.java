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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

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

    int clientes, maxClientes;
    int salas;
    String grupos;
    Ventana vista;
    static final int PORT = 1993;
    ArrayList< ArrayList< ArrayList<Socket>>> listaTCP; //Grupo/Sala/Socket
    ArrayList< ArrayList< ArrayList<DatagramPacket> >> listaUDP; //Grupo/Sala/Socket
    ArrayList< ArrayList<Integer>> idSalas; //Grupo/IdSala

    /*
    Constructor, inicializa la estructura interna y lee el string con los grupos
    Llamado desde Constructor de Ventana
     */
    public Servidor(int max, Ventana vista) throws FileNotFoundException
    {
        clientes = 0;
        salas = 0;
        maxClientes = max;
        this.vista = vista;
        HiloServidor.setContador(1);

        Scanner fileIn = new Scanner(new File("grupos.txt"));
        grupos = fileIn.nextLine();
        System.out.println(grupos);
        listaTCP = new ArrayList();
        listaUDP = new ArrayList();
        idSalas = new ArrayList();
        for (int i = 0; i < grupos.length(); i++) {
            if (grupos.charAt(i) == '/') {
                listaTCP.add(new ArrayList());
                listaUDP.add(new ArrayList());
                listaTCP.get(listaTCP.size() - 1).add(new ArrayList());
                listaUDP.get(listaUDP.size() - 1).add(new ArrayList());
                idSalas.add(new ArrayList());
            }
        }
        idSalas.add(new ArrayList());

    }

    /*
    Inicio de servidor, llamado desde Constructor de Ventana
     */
    public void encender()
    {
        ServerSocket serverSocket = null;
        Socket TCP = null;

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
        while (true) {
            try {
                TCP = serverSocket.accept();
                clientes++;
                vista.SetClientes(clientes);

            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
            // Nuevo hilo para cada cliente
            new HiloServidor(TCP, this).start();
        }
    }

    /*
    Get puerto de servidor, sin usar
     */
    public int getPuerto()
    {
        return PORT;
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

    /*
    Apagar, cierra la conexión con todos los sockets TCP abiertos
    Llamado desde la vista al cerrar la aplicación
     */
    public void apagar() throws IOException
    {
        for (int i = 0; i < listaTCP.size(); i++) {
            for (int j = 0; j < listaTCP.get(i).size(); j++) {
                for (int k = 0; k < listaTCP.get(i).get(j).size(); k++) {
                    listaTCP.get(i).get(j).get(k).close();
                    clientes--;
                    vista.SetClientes(clientes);
                }
            }
        }
    }

    /*
    Funcion que obtiene el puerto de un cliente previamente conectado
    Se llama desde HiloServidor, distribuir coordenadas
     */
    public int GetPorClienteUDP(int IDsala, int indice)
    {
        for (int i = 0; i < listaUDP.size(); i++) {
            for (int j = 0; j < listaUDP.get(i).size(); j++) {
                try{
                return listaUDP.get(i).get(j).get(indice).getPort();
                }catch(Exception e){}
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
        for (int i = 0; i < listaTCP.size(); i++) {
            for (int j = 0; j < listaTCP.get(i).size(); j++) {
                return listaTCP.get(i).get(j).get(indice).getLocalAddress();
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
        for (int i = 0; i < idSalas.size(); i++) {
            for (int j = 0; j < idSalas.get(i).size(); j++) {
                if (idSalas.get(i).get(j) == IDsala) {
                    return listaTCP.get(i).get(j).size();
                }
            }
        }
        return 0;
    }

    /*
    Funcion que añade a la estructura una nueva conexión.
    Se llama desde HiloServidor en el Run
     */
    public int addConexion(Socket TCP, int intSala)
    {
        int aux;
        /*
        Si hay espacio pondrá el socket en una sala existente
         */
        for (int i = 0; i < listaTCP.get(intSala).size(); i++) {
            aux = listaTCP.get(intSala).get(i).size();
            if (aux < maxClientes) {
                listaTCP.get(intSala).get(i).add(TCP);
            }
            return idSalas.get(intSala).get(i);
        }
        /*
        Si no hay espacio creará una nueva sala y añadirá el socket
         */
        listaTCP.get(intSala).add(new ArrayList());
        listaUDP.get(intSala).add(new ArrayList());
        idSalas.get(intSala).add(salas + 1);
        salas++;
        vista.SetSalas(salas);
        listaTCP.get(intSala).get(listaTCP.get(intSala).size() - 1).add(TCP);
        listaUDP.get(intSala).get(listaUDP.get(intSala).size() - 1).add(null);
        return salas;
    }

    /*
    
    */
    public void addUDPdata(int idAsignada, InetAddress dir,DatagramPacket data)
    {
        for (int i = 0; i < idSalas.size(); i++) {
            for (int j = 0; j < idSalas.get(i).size(); j++) {
                if (idSalas.get(i).get(j) == idAsignada) {
                    for (int k = 0; k < listaTCP.get(i).get(j).size(); k++) {
                        if (listaTCP.get(i).get(j).get(k).getLocalAddress() == dir) {
                            listaUDP.get(i).get(j).set(k,data);
                            return;
                        }
                    }
                }
            }
        }
    }
    
    /*
    Funcion que en caso de que el cliente cierre la conexión lo elimina de la estructura
    Se llama desde HiloServidor en el Run
     */
    public void desconexion(int idAsignada, InetAddress dir)
    {
        for (int i = 0; i < idSalas.size(); i++) {
            for (int j = 0; j < idSalas.get(i).size(); j++) {
                if (idSalas.get(i).get(j) == idAsignada) {
                    for (int k = 0; k < listaTCP.get(i).get(j).size(); k++) {
                        if (listaTCP.get(i).get(j).get(k).getLocalAddress() == dir) {
                            listaTCP.get(i).get(j).remove(k);
                            listaUDP.get(i).get(j).remove(k);
                            clientes--;
                            vista.SetClientes(clientes);
                            return;
                        }
                    }
                }
            }
        }
    }
}