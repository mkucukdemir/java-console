/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.smallfe.usergenerator;

import java.awt.image.SampleModel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.misc.BASE64Encoder;

/**
 * Write short comment related with created class
 * @author mkucukdemir
 */
public class Main {
    
    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521:globaldb";
    private static final String DB_USER = "C##TEST_DB";
    private static final String DB_PASSWORD = "testDB";
    private static String HTTP_GET_URL = "http://www.isimlerinanlami.net/arama.php?kelime=name&x=0&y=0";
    private static Connection oracleConnection = getDBConnectiton();
    private static final int NUM_U_ADMIN = 4;
    private static final int NUM_U_USER = 20000;
    private static final int NUM_U_COORDINATOR = 3;
    private static final int NUM_U_SUPERVISOR = 5000;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws MalformedURLException, IOException, InterruptedException {
        List<List<SampleName>> namesAndSurnames;
        // Get name samples in DB
        System.out.println("Sample set is fetching from the DB...");
        List<SampleName> samples = getSamplesInDB();
        System.out.println(samples.size() + " samples have found");
        if(!samples.isEmpty()){
            // Ask its validity to http://www.isimlerinanlami.net/arama.php?kelime=name&x=0&y=0 and fill lists of names and surnames
            System.out.println("Your sample is now modeling. It may take a while...");
            namesAndSurnames = distinguishNamesAndSurnames(samples);
            System.out.println("Samples are modelled." + namesAndSurnames.get(0).size() + " names & " + namesAndSurnames.get(1).size() + "surnames have found");
            // Pick a couple of name and surname randomly, then generate user to insert DB
            System.out.println("Finally, user generation started...");
            generateUsers(namesAndSurnames.get(0),namesAndSurnames.get(1));
            System.out.println(" -- THE END -- ");
        }
        else{
            getSamplesInDB_v2();
        }
    }

    private static List<SampleName> getSamplesInDB() {
        List<SampleName> response = new ArrayList<SampleName>();
        Statement statement = null;
        String queryIsimler = "SELECT * FROM ISIMLER WHERE SINIFI IS NULL";
        String queryCount = "SELECT COUNT(*) SAYI FROM ISIMLER WHERE SINIFI IS NULL";
        try {
            statement = oracleConnection.createStatement();
            ResultSet resultSetCount = statement.executeQuery(queryCount);
            resultSetCount.next();
            int count = resultSetCount.getInt("SAYI");
            int portion = count/100;
            ResultSet resultSetIsimler = statement.executeQuery(queryIsimler);
            int iteration = 0;
            System.out.print("[");
            while(resultSetIsimler.next()){
                Integer id = resultSetIsimler.getInt("ID");
                String isim = resultSetIsimler.getString("ISIM");
                char cinsiyet = resultSetIsimler.getString("CINSIYET").charAt(0);
                response.add(new SampleName(id, isim, cinsiyet));
                iteration ++;
                if(iteration>portion){
                    System.out.print("=");
                    iteration = 0;
                }
            }
            System.out.println("]\t\tCOMPLETED");
        } catch (Exception e) {
        }
        return response;
    }

    private static Connection getDBConnectiton() {
        System.out.println("-------- Oracle JDBC Connection Testing ------");

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("Where is your Oracle JDBC Driver?");
            e.printStackTrace();
            return null;
        }
        System.out.println("Oracle JDBC Driver Registered!");
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER,DB_PASSWORD);
        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            return null;
        }
        if (connection != null) {
            System.out.println("You made it, take control your database now!");
        } else {
            System.out.println("Failed to make connection!");
        }
        return connection;
    }

    private static List<List<SampleName>> distinguishNamesAndSurnames(List<SampleName> samples) throws MalformedURLException, IOException, InterruptedException {
        List<List<SampleName>> returnValue = new ArrayList<List<SampleName>>();
        List<SampleName> adlar = new ArrayList<SampleName>();
        List<SampleName> soyadlar = new ArrayList<SampleName>();
        
        Statement statement = null;
        PreparedStatement preparedStatement = null;
        String queryUpdate = "UPDATE ISIMLER SET SINIFI= ? WHERE ID = ?";
        
        System.out.print("[");
        int portion = samples.size()/100;
        int iteration = 0;
        int progressBarUpperBound;
        for (int i = 0; i < samples.size(); i++) {
            Thread.sleep(100);
            try {
                progressBarUpperBound = i/portion;
            } catch (ArithmeticException e) {
                progressBarUpperBound = 100;
            }
            String ad = samples.get(i).getName();
            ad = ad.replaceAll("ı", "%FD")
                    .replaceAll("İ", "%DD")
                    .replaceAll("ğ", "%F0")
                    .replaceAll("Ğ", "%D0")
                    .replaceAll("ü", "%FC")
                    .replaceAll("Ü", "%DC")
                    .replaceAll("ş", "%FE")
                    .replaceAll("Ş", "%DE")
                    .replaceAll("ç", "%E7")
                    .replaceAll("Ç", "%C7")
                    .replaceAll("ö", "%F6")
                    .replaceAll("Ö", "%D6");
            URL urlToAsk = new URL(HTTP_GET_URL.replaceAll("name", ad));
            HttpURLConnection con = (HttpURLConnection) urlToAsk.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            BufferedReader reader;
            try {
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } catch (java.net.ConnectException e) {
                System.err.println("\n"+ urlToAsk.getQuery() + " causes exception");
                con.disconnect();
                Thread.sleep(2000);
                con = (HttpURLConnection) urlToAsk.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("User-Agent", "Mozilla/5.0");
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                System.err.println("New connection has been established");
                System.out.print("[");
                for (int j = 0; j < progressBarUpperBound; j++) {
                    System.out.print("=");
                }
                System.out.print("%" + progressBarUpperBound);
            }
            String line;
            boolean isName = false;
            while ((line = reader.readLine())!=null) {
                if(line.contains("<!-- basla -->")){
                    isName = true;
                    break;
                }
                if(line.contains("<!-- sag menu -->"))
                    break;
            }
            reader.close();
            if(isName)
                adlar.add(samples.get(i));
            else
                soyadlar.add(samples.get(i));
            
            try {
                statement = oracleConnection.createStatement();
                preparedStatement = oracleConnection.prepareStatement(queryUpdate);
                preparedStatement.setString(1, isName?"AD":"SOYAD");
                preparedStatement.setInt(2, samples.get(i).getId());
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                oracleConnection = null;
                oracleConnection = getDBConnectiton();
                System.out.println("New connection has been established...");
                System.out.print("[");
                for (int j = 0; j < progressBarUpperBound; j++) {
                    System.out.print("=");
                }
                System.out.print("%" + progressBarUpperBound);
            }
            
            iteration++;
            if(iteration>portion){
                for (int j = 0; j < 1+(progressBarUpperBound)/10; j++) {
                    System.out.print("\b");
                }
                System.out.print("=");
                iteration = 0;
                System.out.print("%" + progressBarUpperBound);
            }
        }
        System.out.println("]\t\tCOMPLETED");
        returnValue.add(adlar);
        returnValue.add(soyadlar);
        return returnValue;
    }

    private static void generateUsers(List<SampleName> names, List<SampleName> surnames) {
        
    }

    private static void getSamplesInDB_v2() {
        List<SampleName> names = new ArrayList<SampleName>();
        List<SampleName> surnames = new ArrayList<SampleName>();
        SampleName sampleName;
        SampleName sampleSurname;
        Statement statement = null;
        int rol=0;
        long baseId=100000;
        
        String queryForNames =  "SELECT  * " +
                                "FROM "+DB_USER+".ISIMLER " +
                                "WHERE SINIFI='AD'";
        String queryForSurnames =   "SELECT  * " +
                                    "FROM    "+DB_USER+".ISIMLER " +
                                    "WHERE SINIFI='SOYAD' ";
        try{
            ResultSet resultSetCountOfNames = oracleConnection.createStatement().executeQuery(queryForNames);
            ResultSet resultSetCountOfSurnames = oracleConnection.createStatement().executeQuery(queryForSurnames);
            while(resultSetCountOfNames.next()){
                names.add(new SampleName(resultSetCountOfNames.getInt("ID"), resultSetCountOfNames.getString("ISIM"), resultSetCountOfNames.getString("CINSIYET").charAt(0)));
            }
            while(resultSetCountOfSurnames.next()){
                surnames.add(new SampleName(resultSetCountOfSurnames.getInt("ID"), resultSetCountOfSurnames.getString("ISIM"), resultSetCountOfSurnames.getString("CINSIYET").charAt(0)));
            }
        }catch(Exception e){
        }
        
        
        System.out.println("Candidates are fetched...");
        long upperbound = NUM_U_ADMIN+NUM_U_COORDINATOR+NUM_U_SUPERVISOR+NUM_U_USER;
        
        for (long i = 0; i < upperbound; i++) {
            if(i<NUM_U_ADMIN)
                rol = 0;
            else if(i<NUM_U_ADMIN+NUM_U_COORDINATOR)
                rol=1;
            else if(i<NUM_U_ADMIN+NUM_U_COORDINATOR+NUM_U_SUPERVISOR)
                rol=2;
            else if(i<NUM_U_ADMIN+NUM_U_COORDINATOR+NUM_U_SUPERVISOR+NUM_U_USER)
                rol=3;
            Random randomForName = new Random(i);
            Random randomForSurname = new Random(i);
            
            // Kullanici ekle
            try {
                sampleName = names.get(randomForName.nextInt(names.size()));
                sampleSurname = surnames.get(randomForSurname.nextInt(surnames.size()));
                Long id=baseId+i;
                String isim=sampleName.getName();
                String soyisim=sampleSurname.getName();
                char cinsiyet=sampleName.getSex();
                String eposta="";
                String parolaDigest="";
                
                if(cinsiyet=='U')
                    cinsiyet=((new Date()).getTime()%2==0)?'E':'K';
                switch(rol){
                    case 0:
                        eposta = isim.toLowerCase().charAt(0) + soyisim.toLowerCase() + "@kurumbm.gov.tr";
                        break;
                    case 1:
                        eposta = isim.toLowerCase().charAt(0) + soyisim.toLowerCase() + "@kurumdb.gov.tr";
                        break;
                    case 2:
                        eposta = isim.toLowerCase().charAt(0) + soyisim.toLowerCase() + "@uni.edu.tr";
                        break;
                    case 3:
                        eposta = isim.toLowerCase().charAt(0) + soyisim.toLowerCase() + "@mail.com";
                        break;
                    default:
                        break;
                }
                byte[] bytesOfParola = (new String("test123")).getBytes();
                MessageDigest md = MessageDigest.getInstance("MD5");
                BASE64Encoder base64Encoder = new BASE64Encoder();
                parolaDigest=base64Encoder.encode(md.digest(bytesOfParola));
                String queryInsert = "INSERT INTO KULLANICI VALUES("+id+",'"+isim+"','"+soyisim+"','"+cinsiyet+"','"+eposta.replaceAll("ç", "c").replaceAll("ı", "i").replaceAll("ş", "s").replaceAll("ü", "u").replaceAll("ö", "o").replaceAll("ğ", "g").replaceAll("â", "a").replaceAll("û", "u").replaceAll("î", "i")+"','"+parolaDigest+"')";
                statement = oracleConnection.createStatement();
                statement.executeUpdate(queryInsert);
                statement = null;
            } catch (Exception e) {
                i--;
                oracleConnection = null;
                oracleConnection = getDBConnectiton();
            }
            randomForName = null;
            randomForSurname = null;
            System.out.print("\b"+i+"/"+upperbound);
        }
    }
    
}
