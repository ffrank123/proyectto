package pe.edu.upeu.dao;

import java.io.Console;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import pe.edu.upeu.crud.AppCrud;
import pe.edu.upeu.modelo.UsuarioTO;
import pe.edu.upeu.util.LeerArchivo;
import pe.edu.upeu.util.LeerTeclado;
import pe.edu.upeu.util.UtilsX;

public class UsuarioDao extends AppCrud {
    LeerArchivo leerA;
    LeerTeclado leerT=new LeerTeclado();
    UtilsX util=new UtilsX();
    UsuarioTO uto;

    final static String TABLA_USUARIO="Usuario.txt";

    public void crearUsuarios() {
        uto=new UsuarioTO();     
        uto.setUsuario(leerT.leer("", "Ingrese un Usuario"));   
        Console cons=System.console();
        System.out.println("Ingrese su calve:");
        char[] clavex=cons.readPassword();
        uto.setClave(getMD5EncryptedValue(String.valueOf(clavex)));
        leerA=new LeerArchivo(TABLA_USUARIO);
        agregarContenido(leerA, uto);
    }

    public static String getMD5EncryptedValue(String password) {
        final byte[] defaultBytes = password.getBytes();
        try {
        final MessageDigest md5MsgDigest = MessageDigest.getInstance("MD5");
        md5MsgDigest.reset();
        md5MsgDigest.update(defaultBytes);
        final byte messageDigest[] = md5MsgDigest.digest();
        final StringBuffer hexString = new StringBuffer();
        for (final byte element : messageDigest) {
        final String hex = Integer.toHexString(0xFF & element);
        if (hex.length() == 1) {
        hexString.append('0');}
        hexString.append(hex);
        }
        password = hexString + "";
        } catch (final NoSuchAlgorithmException nsae) {
        nsae.printStackTrace();
        }
        return password;
        }    

    public boolean login(String usuario, String clave) {
        leerA=new LeerArchivo(TABLA_USUARIO);
        Object[][] dataU=buscarContenido(leerA, 0, usuario);
        if (dataU.length>0 && String.valueOf(dataU[0][1]).equals(clave)) {
            return true;
        }
        return false;
    }


    public void listarUsuarios() {
        leerA=new LeerArchivo(TABLA_USUARIO);
       Object[][] data=listarContenido(leerA);
       imprimirLista(data); 
    }


    
}
