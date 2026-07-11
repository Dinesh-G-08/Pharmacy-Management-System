/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pack;

/**
 *
 * @author yokes
 */
import java.io.File;
import javax.swing.JOptionPane;
import dao.Loco;

public class Open {
    public static void openById(String id){
        try{
            if((new File(Loco.path+id+".pdf")).exists()){
                Process p = Runtime
                        .getRuntime()
                        .exec("rundll32 url.dll,FileProtocolHandler "+Loco.path+""+id+".pdf");
                    
            }else{
                JOptionPane.showMessageDialog(null, "File Not Exist...");
            }
        
        }
        catch(Exception e){
        JOptionPane.showMessageDialog(null, e);
        }
        
    }
    
}
