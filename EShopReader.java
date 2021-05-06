
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author DELL
 */
public class EShopReader {
    public static void main(String[] args) throws IOException, CsvException { 
        String line = "";
        String splitBy = ";";
        FileWriter model_file = new FileWriter("src/data/transaction.csv");
        CSVWriter writer = new CSVWriter(model_file);
        try   
            {  
            //parsing a CSV file into BufferedReader class constructor  
                BufferedReader br = new BufferedReader(new FileReader("src/data/eshop.csv"));
                
                String[][] collect = new String[165475][2];
                String[] model;
                
                for(int i = 0; i<165475; i++){
                    line = br.readLine();
                    model = line.split(splitBy);
                    if(model[5] == null)
                        collect[i][0] = collect[i-1][0];
                    else
                        collect[i][0] = model[5];
                    collect[i][1] = model[7];
                }

                for(int i = 0; i<5; i++){
                    //System.out.println(collect[i][0]+"  "+collect[i][1]);
                }
                String[] k = new String[165475];
                k[0] = collect[1][1];
                int count = 0;
                
                for(int i = 2; i<165475; i++){           
                    if(collect[i][0].compareTo(collect[i-1][0])==0){
                        k[count] += ','+collect[i][1];
                    }else {
                        k[count+1] = collect[i][1];
                        //System.out.println(k[count]);
                        count+=1;
                    }   
                }
                String[][] final_model = new String[24026][2];
                for(int i = 1; i<24027;i++){
                    final_model[i-1][0]= String.valueOf(i);
                    final_model[i-1][1]= k[i-1];
                    //System.out.println(final_model[i-1][1]);
                }
                
                br.close();
                //
                List<String[]> data = new ArrayList<String[]>();
                data.add(new String[]{"", "session","model"});
                for(int i = 0; i < 24026; i++){
                    data.add(new String[]{String.valueOf(i), final_model[i][0],final_model[i][1]});
                }
                writer.writeAll(data);
                writer.close();
                /*
                for(int i =0; i<5;i++){
                    System.out.println(item[i]);
                }*/
            }   
            catch (IOException e)   
            {  
                e.printStackTrace();  
            }   
    }
}
    