package bitbot.util;

import bitbot.util.database.DatabaseConnection;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author z
 */
public class BitcoinWisdomReader {

    public static void ReadJsonFile(String file, String path, boolean showdebugOnly) throws InterruptedException {
        System.out.println("Output to SQL table: " + path);
        System.out.println("======================Starting in 3 seconds======================"); // some delay, in case its pressed by accident
        
        Thread.sleep(3000);
        
        Calendar beforeDate = Calendar.getInstance();
        beforeDate.set(Calendar.MONTH, 9 - 1);
        beforeDate.set(Calendar.DAY_OF_MONTH, 05);
        beforeDate.set(Calendar.YEAR, 2014);
        
        System.out.println("Before: " + beforeDate.getTimeInMillis() / 1000);
        
     /*  Calendar endDate = Calendar.getInstance();
        endDate.set(Calendar.MONTH, 3 - 1);
        endDate.set(Calendar.DAY_OF_MONTH, 6);
        
        System.out.println("End: " + endDate.getTimeInMillis() / 1000);*/
             
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();

            JSONParser parser = new JSONParser(); // Init parser

            // Container factory for the JSON array to persist the order
            ContainerFactory containerFactory = new ContainerFactory() {
                @Override
                public List creatArrayContainer() {
                    return new LinkedList();
                }

                @Override
                public Map createObjectContainer() {
                    return new LinkedHashMap();
                }
            };
            LinkedList<LinkedList> dataArray = (LinkedList<LinkedList>) parser.parse(line, containerFactory);

            Iterator<LinkedList> itr = dataArray.iterator();

            while (itr.hasNext()) {
                LinkedList arrayData = itr.next();

                long time = Long.parseLong(arrayData.get(0).toString());
                long unk1 = Long.parseLong(arrayData.get(1).toString()); // might be data order
                long unk2 = Long.parseLong(arrayData.get(2).toString());

                float open = Float.parseFloat(arrayData.get(3).toString());
                float close = Float.parseFloat(arrayData.get(4).toString());
                float high = Float.parseFloat(arrayData.get(5).toString());
                float low = Float.parseFloat(arrayData.get(6).toString());
                double vol_cur = Float.parseFloat(arrayData.get(7).toString());
                double vol = Float.parseFloat(arrayData.get(10).toString());

                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(time * 1000);

             /*   if (cal.before(beforeDate)) {
                    System.out.println(String.format("[%s] Open: %f, High: %f, Low: %f, Close: %f, VolumeCur: %f, Volume: %f", cal.getTime().toString(), open, high, low, close, vol_cur, vol));

                    if (showdebugOnly)
                        InsertSQLTable(path, high, low, vol, vol_cur, open, close, time); 
                } else {
                    //System.out.println(time);
                }*/
                
                if (time <= 1389355200) {
                                        System.out.println(String.format("[%s] Open: %f, High: %f, Low: %f, Close: %f, VolumeCur: %f, Volume: %f", cal.getTime().toString(), open, high, low, close, vol_cur, vol));

                    if (showdebugOnly)
                        InsertSQLTable(path, high, low, vol, vol_cur, open, close, time); 
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void InsertSQLTable(String tableName, float High, float Low, double Volume, double Volume_Cur, float Open, float LastPrice, long LastCommitTime) {
        PreparedStatement ps = null;
        try {
            Connection con = DatabaseConnection.getConnection();

            ps = con.prepareStatement("INSERT INTO bitcoinbot." + tableName + " (\"high\", \"low\", \"vol\", \"vol_cur\", \"open\", \"close\", \"server_time\") VALUES (?,?,?,?,?,?,?);");
            ps.setFloat(1, High);
            ps.setFloat(2, Low);
            ps.setDouble(3, Volume);
            ps.setDouble(4, Volume_Cur);
            ps.setFloat(5, Open);
            ps.setFloat(6, LastPrice);
            ps.setLong(7, (long) LastCommitTime);

            ps.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
