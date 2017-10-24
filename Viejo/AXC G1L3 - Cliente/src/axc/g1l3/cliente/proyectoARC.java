/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axc.g1l3.cliente;

import java.io.IOException;

/**
 *
 * @author aline
 */
public class proyectoARC {
    
    
    public static void main(String[] args) throws IOException
    {
        Cliente cliente = new Cliente();
        vistaCliente vc = new vistaCliente(cliente);
        vc.setVisible(true);
        cliente.Conectar(vc);
      

        
    }
    
}
