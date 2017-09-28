/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyectobd1;

import frames.VentanaSeleccion;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Gerald
 */
public class ConexionServidor {
    public String bd;
    public String userName;
    public String userPassword;
    public Connection con;  
    public Statement stmt;  
    public ResultSet rs;
    public DatabaseMetaData metaDatos;
    public ResultSetMetaData rsCantidadColumnas;
    //public ArrayList<String> listaTablas;
    ArrayList<Tabla> listaTablas;
    
    public ConexionServidor(){
        
    }
    
    public ConexionServidor(String pBd,String pUserName,String pUserPassword) {
        this.bd = pBd;
        this.userName = pUserName;
        this.userPassword = pUserPassword;
        this.con = null;
        this.stmt = null;
        this.rs = null;
        this.rsCantidadColumnas = null;
        this.metaDatos = null;
        this.listaTablas = new ArrayList<>();
    }

    public void agregarNombreTabla(Tabla nuevaTabla){
        this.listaTablas.add(nuevaTabla);
    }
    
    public void mostrarNombreTablas(){
        for(int i = 0; i < this.listaTablas.size(); i++){
            System.out.println(listaTablas.get(i));
        }
    }

    public ResultSet getRs() {
        return rs;
    }

    public void setRs(ResultSet rs) {
        this.rs = rs;
    }
        
    public void hacerConexion(){
        try {  
            // Establece la conexion con la base de datos.  
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");  
            this.con = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName="+bd+";",userName,userPassword);  
            this.stmt = this.con.createStatement();
            this.metaDatos = con.getMetaData();
            agregarListaTablas();
            System.out.println(existeTablaP("clientes"));
            System.out.println("Conexion establecida...");
        }  

      // Muestra los errores ocurridos.  
        catch (Exception e) {  
           System.out.println("Error usuario,contraseña o base de datos incorrecto");
           //e.printStackTrace();  
        }
    }
    
    public void realizarInstruccionSql(int tipo, String consulta) throws SQLException{
        this.rs = null;
        if(tipo == 0){
            this.rs = stmt.executeQuery(consulta);
            this.rsCantidadColumnas = rs.getMetaData();
            // Recorre las tuplas de la tabla , hay que arreglar las consultas con las columnas
//            while (rs.next()) {
//                for(int i = 0; i < rsCantidadColumnas.getColumnCount(); i++){
//                    System.out.print(rs.getString(i+1) + " ");  
//                }
//                System.out.println("");
//            }
            System.out.println("Consulta realizada...");
        }else{
            stmt.executeUpdate(consulta);
            System.out.println("Instruccion sql realizada...");
        }  
    }
    
    public void agregarListaTablas() throws SQLException{
        if(con != null){
             ResultSet result = null;
            String[] types = {"TABLE"};
            result = metaDatos.getTables(null, "dbo", "%", types);
            while (result.next()) {
                this.rs = null;
                String consulta = "SELECT * FROM "+result.getString(3);
                realizarInstruccionSql(0, consulta);
                Tabla nuevaTabla = new Tabla(result.getString(3),false,convertirVector2ArrayList(atributosCosulta()));
                agregarNombreTabla(nuevaTabla);
            }
            
            System.out.println("Se agregaron existosamente los nombres de las tablas...");
        }
    }
    
    public boolean existeTablaP(String nombreTabla){
        for(int i = 0; i < this.listaTablas.size(); i++){
            if(this.listaTablas.get(i).getNombre().equals(nombreTabla)){
                return true;
            }
        }
        return false;
    
    }
    
    /**
     * Esta funcion devuelve los atributos de una tabla
     * @param nombre
     * @return resultado
     */
    public ArrayList<String> obtenerAtributos(String nombre){
        ArrayList<String> resultado = new ArrayList<>();
        for(int i = 0; i < this.listaTablas.size(); i++){
            if(this.listaTablas.get(i).getNombre().equals(nombre)){
                resultado = this.listaTablas.get(i).getListaAtributos();
                return resultado;
            }
        } 
        return resultado;
    }
    
    
    /**
     * Esta funcion compara todos los elementos de dos tablas
     * @param atributos1
     * @param atributos2
     * @return boolean
     */
    public boolean atributosIgualesP(ArrayList<String> atributos1, ArrayList<String> atributos2){
        if(atributos1.size() == atributos2.size()){
            for(int i = 0; i < atributos1.size(); i++){
                if(!atributos1.get(i).equals(atributos2.get(i))){
                    return false;
                }
            }
            return true;
        }else{
            return false;
        }
    }
    
    /**
     * Esta funcion revisa si una tabla es temporal o no
     * 
     */
    public boolean esTablaTemporalP(String nombreTabla){
        for(int i = 0; i < this.listaTablas.size(); i++){
            if(this.listaTablas.get(i).getNombre().equals(nombreTabla) && this.listaTablas.get(i).getTemporal()){
                return true;
            }
        }
        return false;
    
    }
    
    /**
     * Metodo sirve para convertir de un vector de string a un arraylist de string
     * 
     */
    public ArrayList<String> convertirVector2ArrayList(String[] vector) throws SQLException{
        ArrayList<String> lista = new ArrayList<>();
        for(int i=0;i<vector.length;i++){
            lista.add(vector[i]);
        }
        return lista;
    }
    
    /**
     * Metodo encargado de extraer los nombres de los atributos de una consulta
     * @return
     * @throws SQLException 
     */
    public String[] atributosCosulta() throws SQLException{
        ResultSetMetaData consulta = rs.getMetaData();
        int columnas = consulta.getColumnCount();
        String[] atributos = new String[columnas];
        for(int i=0;i<columnas;i++){
            //System.out.println(consulta.getColumnName(i+1));        
            atributos[i]= consulta.getColumnName(i+1);
        }
        return atributos;
    }
    /**
     * Metodo encargado de extraer todas las tuplas de una consulta y retornar resultado[][]
     * @return
     * @throws SQLException 
     */
    public String[][] tuplasConsulta() throws SQLException{
        ResultSetMetaData consulta = rs.getMetaData();
        int columna = consulta.getColumnCount();
        ArrayList<String[]> tuplas = new ArrayList();
        // Recorre las tuplas de la tabla , hay que arreglar las consultas con las columnas
        while (rs.next()) {
            String[] atributos = new String[columna];
            for(int i = 0; i < columna; i++){
                //System.out.print(rs.getString(i+1) + " ");
                atributos[i] = rs.getString(i+1);     
            }
            tuplas.add(atributos);
        }
        String[][] result = new String[tuplas.size()][columna];
        for(int i=0;i<tuplas.size();i++){
            for(int j=0;j<columna;j++){
                result[i][j]= tuplas.get(i)[j];
            }
            
        }
        //System.out.println(Arrays.deepToString(result));
        return result;
    }
}
