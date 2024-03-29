package pe.edu.upeu.dao;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import pe.edu.upeu.crud.AppCrud;
import pe.edu.upeu.modelo.ProductoTO;
import pe.edu.upeu.modelo.VentaDetalleTO;
import pe.edu.upeu.modelo.VentaTO;
import pe.edu.upeu.util.LeerArchivo;
import pe.edu.upeu.util.LeerTeclado;
import pe.edu.upeu.util.UtilsX;

public class VentaDao extends AppCrud{
    LeerTeclado leerT=new LeerTeclado();
    UtilsX util=new UtilsX();
    LeerArchivo leerA;

    VentaTO vTo;
    VentaDetalleTO vdTo;
    final static String TABLA_CLIENTE="Cliente.txt";
    final static String TABLA_PRODUCTO="Producto.txt";
    final static String TABLA_VENTA="Venta.txt";
    final static String TABLA_VENTADETALLE="VentaDetalle.txt";

    SimpleDateFormat formatoFechaHora = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    SimpleDateFormat formatoFecha = new SimpleDateFormat("dd-MM-yyyy");

    public void registroVentaGeneral() {
        util.clearConsole();
        System.out.println("*****************Registro de Ventas***************");
        vTo=new VentaTO();
        leerA=new LeerArchivo(TABLA_VENTA);
        String idVX=generarId(leerA, 0, "V", 1);
        vTo.setIdVenta(idVX);
        vTo.setDniruc(validarCliente(leerT.leer("", "Ingrese DNI/RUC cliente")));
        Date fecha=new Date();
        vTo.setFecha(formatoFechaHora.format(fecha));
        VentaTO vX=crearVenta(vTo);
        double montoX=0;
        if (vX!=null) {
            char continuar='N';
            do {
                System.out.println("--------------Agregar Productos-----------");                    
                 VentaDetalleTO vdX=carritoVentas(vTo);
                 montoX+=vdX.getSubtotal();
                 
                continuar= leerT.leer("", "Desea Agregar mas Productos?S/N")
                                .toUpperCase()
                                .charAt(0);
            } while (continuar=='S');
            leerA=new LeerArchivo(TABLA_VENTA); //Math.round(double*100.0)/100.0
            vX.setPrecioTotal(Math.round(montoX*100.0)/100.0);
            vX.setPreciobase(Math.round((montoX/1.18)*100.0)/100.0);
            vX.setIgv(Math.round((vX.getPreciobase()*0.18)*100.0)/100.0);
            editarRegistro(leerA, 0, vX.getIdVenta(), vX);
        }
    }

    public VentaTO crearVenta(VentaTO to) {
        leerA=new LeerArchivo(TABLA_VENTA);
        agregarContenido(leerA, to);
        leerA=new LeerArchivo(TABLA_VENTA);
        Object[][] data=buscarContenido(leerA, 0, to.getIdVenta());
        if (data.length==1) {
            return to;
        }
        return null;
    }

    public VentaDetalleTO carritoVentas(VentaTO to) {
        System.out.println();
        leerA=new LeerArchivo(TABLA_VENTADETALLE);
        vdTo=new VentaDetalleTO();
        String idDV=generarId(leerA, 0, "DV", 2);
        vdTo.setIdVentDet(idDV);
        vdTo.setIdVenta(to.getIdVenta());
        mostrarProductos();
        vdTo.setIdProducto(leerT.leer("", "Ingrese Id Producto"));
        vdTo.setCantidad(leerT.leer(0, "Ingrese Cantidad"));
        vdTo.setDescuento(0);
        leerA=new LeerArchivo(TABLA_PRODUCTO);
        Object[][] dataP=buscarContenido(leerA, 0, vdTo.getIdProducto());
        
        actualizarProductoStock(vdTo.getIdProducto(),(int)vdTo.getCantidad());

        double pux=Double.parseDouble(String.valueOf(dataP[0][4]));
        vdTo.setPu(pux);
        vdTo.setSubtotal(vdTo.getCantidad()*vdTo.getPu());
        leerA=new LeerArchivo(TABLA_VENTADETALLE);
        agregarContenido(leerA, vdTo);
        leerA=new LeerArchivo(TABLA_VENTADETALLE);
        Object[][] dataX=buscarContenido(leerA, 0, idDV);
        if (dataX.length==1) {
            return vdTo;
        }
        return null;
    }

    public void actualizarProductoStock(String idP, int cantidad) {
        System.err.println("VEER:"+cantidad);
        leerA=new LeerArchivo(TABLA_PRODUCTO);
        ProductoTO to=new ProductoTO();
        Object[][] dataP=buscarContenido(leerA, 0, idP);
        int x=Integer.parseInt(String.valueOf(dataP[0][6]));
        System.err.println("VER2:"+x);
        to.setStock(x-cantidad);
        leerA=new LeerArchivo(TABLA_PRODUCTO);
        editarRegistro(leerA, 0, idP, to);
    }

    public String validarCliente(String dniruc) {
        leerA=new LeerArchivo(TABLA_CLIENTE);
        Object[][] dataC=buscarContenido(leerA, 0, dniruc);
        if (dataC==null || dataC.length==0) {
            new ClienteDao().crearCliente();
        }
        return dniruc;
    }

    public void mostrarProductos() {
        leerA=new LeerArchivo(TABLA_PRODUCTO);
        Object[][] dataP=listarContenido(leerA);

        for (int i = 0; i < dataP.length; i++) {
            if(Integer.parseInt(String.valueOf(dataP[i][6]))>0){
                System.out.print(dataP[i][0]+"="+dataP[i][1]+"(Stock:"+dataP[i][6]+")"+", ");    
            }           
        }
        System.out.println("");
    }


    public void reporteVentasRangoFecha() {
        util.clearConsole();
        System.out.println("===================Reporte de Ventas==================");
        String fechaInit=leerT.leer("", "Ingrese F. Inicio (dd-MM-yyyy)");
        String fechaFinal=leerT.leer("", "Ingrese F. Final (dd-MM-yyyy)");
        leerA=new LeerArchivo(TABLA_VENTA);
        Object[][] dataV=listarContenido(leerA);
        int contadorVRD=0;
        try {
            //Para saber que cantidad de registros coinciden con el rango de fecha
            for (int i = 0; i < dataV.length; i++) {
                String[] fechaVentor=String.valueOf(dataV[i][2]).split(" ");
                Date fechaVentaX=formatoFecha.parse(fechaVentor[0]);
                if(
                (fechaVentaX.after(formatoFecha.parse(fechaInit)) || fechaInit.equals(fechaVentor[0])) && 
                (fechaVentaX.before(formatoFecha.parse(fechaFinal)) || fechaFinal.equals(fechaVentor[0]))
                ){
                    contadorVRD++; 
                }
            }

            //Pasar los datos a un Vetor de tipo VentaTO segun Rango Fechas
            VentaTO[] dataReal=new VentaTO[contadorVRD];
            int indiceVector=0;
            for (int i = 0; i < dataV.length; i++) {
                String[] fechaVentor=String.valueOf(dataV[i][2]).split(" ");
                Date fechaVentaX=formatoFecha.parse(fechaVentor[0]);
                if(
                (fechaVentaX.after(formatoFecha.parse(fechaInit)) || fechaInit.equals(fechaVentor[0])) && 
                (fechaVentaX.before(formatoFecha.parse(fechaFinal)) || fechaFinal.equals(fechaVentor[0]))
                ){
                   VentaTO vTo=new VentaTO();
                   vTo.setIdVenta(dataV[i][0].toString());
                   vTo.setDniruc(dataV[i][1].toString());
                   vTo.setFecha(dataV[i][2].toString());

                   vTo.setIgv(Double.parseDouble(String.valueOf(dataV[i][4].toString())));
                   vTo.setPreciobase(Double.parseDouble(String.valueOf(dataV[i][3])));
                   vTo.setPrecioTotal(Double.parseDouble(String.valueOf(dataV[i][5])));
                   
                   dataReal[indiceVector]=vTo;
                   indiceVector++;
                }
            }
            //Imprimir Ventas     
            Ansi color;
            AnsiConsole.systemInstall();
            color=new Ansi();
            System.out.println(color.bgBrightYellow().fgBlack()
            .a("===========Reporte Ventas entre "+fechaInit+" Y "+fechaFinal+"============").reset());
            
            util.pintarLine('H', 40);
            util.pintarTextHeadBody('H', 3, "ID,DNI,Fecha, Sub. Total, IGV, Imp.Total");
            System.out.println("");
            double subtotalX=0,igvX=0, importeTX =0;
            util.pintarLine('H', 40);
            for (VentaTO TOv : dataReal) {
                String datax=TOv.getIdVenta()+","+TOv.getDniruc()+","+TOv.getFecha()+","+
                TOv.getPreciobase()+","+TOv.getIgv()+","+TOv.getPrecioTotal();

                subtotalX+=TOv.getPreciobase(); 
                igvX+=TOv.getIgv(); 
                importeTX+=TOv.getPrecioTotal();
                util.pintarTextHeadBody('B', 3, datax);
            }
            //System.out.println("");
            color=new Ansi();
            util.pintarLine('H', 40);            
            System.out.println(color.render(
            
            "| @|red Sub. Total:|@ | @|blue S/"+ subtotalX+" |@ | @|red IGV: |@ @|blue S/."+igvX+
            "|@ | @|red Imp. Total: |@ @|blue S/."+importeTX+"|@"
            
            )
            );
            util.pintarLine('H', 40);            


        } catch (Exception e) {
           System.err.println("Error al Reportar!!"+e.getMessage());
        }

        

    }     public void generarPDFReporte() {
        Document document = new Document();
        try {
        PdfWriter.getInstance(document, new FileOutputStream(
        new LeerArchivo()
        .ubicarRutaPDF("reportes","Reporte.pdf")));
        //open
        document.open();
        Font f = new Font();
        f.setStyle(Font.BOLD);
        f.setSize(8);
        Paragraph p = new Paragraph("",f);
        p.add("Reporte de Ventas");
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(5);
        document.add(p);
        //Aqui Agregar Tabla
        //close
        document.close();
        System.out.println("Done");
        } catch (FileNotFoundException | DocumentException e) {
        e.printStackTrace();
        } catch (IOException e) {
        e.printStackTrace();

}}}
