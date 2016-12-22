package bitbot.util;

import bitbot.util.database.DatabaseConnection;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

/**
 *
 * @author z
 */
public class MT4CVSReader {

    public static void ReadCVSFile(String file) {
        Calendar cal_cutoffDate = Calendar.getInstance();
        cal_cutoffDate.set(2014, 0, 10, 0, 0);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] lineSplit = line.split(",");
                // 2011.03.24,0:00,0.832,0.9,0.827,0.867,14009

                // Date
                String Date = lineSplit[0];
                String[] Time = lineSplit[1].split(":");

                // Init var
                Calendar cal = Calendar.getInstance();
                cal.set(
                        Integer.parseInt(Date.substring(0, 4)),
                        Integer.parseInt(Date.substring(5, 7)) - 1,
                        Integer.parseInt(Date.substring(8, 10)),
                        Integer.parseInt(Time[0]),
                        Integer.parseInt(Time[1]), 0);

                // Etc
                float Open = (float) Float.parseFloat(lineSplit[2]);
                float High = (float) Float.parseFloat(lineSplit[3]);
                float Low = (float) Float.parseFloat(lineSplit[4]);
                float Close = (float) Float.parseFloat(lineSplit[5]);
                double VolumeCur = (double) Float.parseFloat(lineSplit[6]);
                double Volume = (double) VolumeCur * Open;

                if (cal.before(cal_cutoffDate)) {
                    System.out.println(String.format("[%d %d %d] Open: %f, High: %f, Low: %f, Close: %f, VolumeCur: %f, Volume: %f", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE), Open, High, Low, Close, VolumeCur, Volume));
                    
                    InsertSQLTable("btce_price_nmc_usd", High, Low, Volume, VolumeCur, Open, Close, cal.getTimeInMillis());
                }
            }
        } catch (IOException e) {
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
            ps.setLong(7, (long) (LastCommitTime / 1000l));

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
