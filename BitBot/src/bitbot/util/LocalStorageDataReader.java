package bitbot.util;

import bitbot.util.database.DatabaseConnection;
import bitbot.util.encryption.input.ByteArrayByteStream;
import bitbot.util.encryption.input.GenericSeekableLittleEndianAccessor;
import bitbot.util.encryption.input.SeekableLittleEndianAccessor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

/**
 *
 * @author zheng
 */
public class LocalStorageDataReader {

    public static void ReadJsonFile(String path, String ExchangeCurrencyPair, boolean showdebugOnly) throws InterruptedException {
        System.out.println("Output to SQL table: " + path);
        System.out.println("======================Starting in 3 seconds======================"); // some delay, in case its pressed by accident

        Thread.sleep(3000);

        // Start saving to local file
        File f = new File("CachedPrice");
        if (!f.exists()) {
            f.mkdirs();
        }
        try {
            // Loop through all possible files, there may be multiple for a single pair
            // Ex: bitfinex-btc_usd, bitfinex-btc_usd_1, bitfinex-btc_usd_2, bitfinex-btc_usd_3
            for (int z_fileCount = 0; z_fileCount < Integer.MAX_VALUE; z_fileCount++) {

                final File f_data = new File(f, z_fileCount == 0 ? ExchangeCurrencyPair : (ExchangeCurrencyPair + "_" + z_fileCount));
                if (!f_data.exists()) {
                    break;
                }

                FileInputStream fis = null;
                BufferedReader reader = null;
                try {
                    fis = new FileInputStream(f_data);

                    byte[] byte_line = new byte[fis.available()];
                    int readbytes = fis.read(byte_line, 0, fis.available());
                    if (readbytes != -1) {
                        final SeekableLittleEndianAccessor slea = new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream(byte_line));

                        // int file_version = slea.readInt(); // to be used for future updates
                        int data_size = slea.readInt();
                        //System.out.println("[" + z_fileCount + "] Read count: " + data_size);

                        for (int i = 0; i < data_size; i++) {
                            final byte startingMarker = slea.readByte();
                            // starting and ending marker to ensure simple checksum and the file integrity
                            // if those markers are not -1, reload the entire one from backup
                            if (startingMarker != -1) {
                                // cleanup
                                return;
                            }
                            float close = slea.readFloat();
                            float open = slea.readFloat();
                            float high = slea.readFloat();
                            float low = slea.readFloat();
                            long servertime = slea.readLong();
                            double volume = slea.readDouble();
                            double volume_cur = slea.readDouble();
                            float ratio = slea.readFloat();

                            Calendar cal = Calendar.getInstance();
                            cal.setTimeInMillis(servertime * 1000);

                            if (servertime < 1445439600) {
                                System.out.println(String.format("[%s] Open: %f, High: %f, Low: %f, Close: %f, VolumeCur: %f, Volume: %f", cal.getTime().toString(), open, high, low, close, volume_cur, volume));

                                if (showdebugOnly) {
                                    InsertSQLTable(path, high, low, volume, volume_cur, open, close, servertime, ratio);
                                }
                            }

                        }
                    }
                } catch (Exception error) {
                    // data is corrupted?
                    error.printStackTrace();
                    f_data.delete();
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                    if (fis != null) {
                        fis.close();
                    }
                }
            }
        } catch (IOException exp) {
            exp.printStackTrace();
        }

    }

    private static void InsertSQLTable(String tableName, float High, float Low, double Volume, double Volume_Cur, float Open, float LastPrice, long LastCommitTime, float ratio) {
        PreparedStatement ps = null;
        try {
            Connection con = DatabaseConnection.getConnection();

            ps = con.prepareStatement("INSERT INTO bitcoinbot." + tableName + " (\"high\", \"low\", \"vol\", \"vol_cur\", \"open\", \"close\", \"server_time\", \"buysell_ratio\") VALUES (?,?,?,?,?,?,?,?);");
            ps.setFloat(1, High);
            ps.setFloat(2, Low);
            ps.setDouble(3, Volume);
            ps.setDouble(4, Volume_Cur);
            ps.setFloat(5, Open);
            ps.setFloat(6, LastPrice);
            ps.setLong(7, (long) LastCommitTime);
            ps.setFloat(8, ratio); // ratio

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
