/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axc.g1l3.cliente;

/**
 *
 * @author Ignacio
 */
public class Vecino
{

    private boolean Yo;
    private int x;
    private int y;
    private int id;

    Vecino(int id, int x, int y, int idPropia)
    {
        this.id = id;
        Yo = (idPropia == id);
        this.x = x;
        this.y = y;
    }

    public boolean Actualizar(int id, int x, int y)
    {
        if (id == this.id) {
            this.x = x;
            this.y = y;
            return true;
        }
        return false;
    }

    public String toString()
    {
        if (Yo) {
            return ("Mi posición: " + x + "," + y);
        } else {
            return ("Vecino de id " + id + " en posición " + x + "," + y);
        }
    }

}
