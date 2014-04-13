package bitbot.handler;

import bitbot.util.Pair;
import java.util.ArrayList;
import java.util.List;

/**
 * https://www.cloudflare.com/ips
 *
 * @author zheng hao
 */
public class CloudflareIPValidator {

    // should I do IPV6? :(
    private static final String[] IPAddress_v4 = {
        "199.27.128.0/21",
        "173.245.48.0/20",
        "103.21.244.0/22",
        "103.22.200.0/22",
        "103.31.4.0/22",
        "141.101.64.0/18",
        "108.162.192.0/18",
        "190.93.240.0/20",
        "188.114.96.0/20",
        "197.234.240.0/22",
        "198.41.128.0/17",
        "162.158.0.0/15"};

    private static final List<Pair<Integer, Integer>> CompiledIPAddrRange = new ArrayList<>();

    static {
        for (String CloudFlareIP : IPAddress_v4) {
            String[] splitIPAddr = CloudFlareIP.split("\\.");
            String[] last_mask = splitIPAddr[3].split("\\/");

            // Convert IPs into ints (32 bits). 
            // E.g. 157.166.224.26 becomes 10011101  10100110  11100000 00011010
            int addr = ((Integer.parseInt(splitIPAddr[0]) << 24) & 0xFF000000)
                    | ((Integer.parseInt(splitIPAddr[1]) << 16) & 0xFF0000)
                    | ((Integer.parseInt(splitIPAddr[2]) << 8) & 0xFF00)
                    | (Integer.parseInt(last_mask[0]) & 0xFF);

            // Get CIDR mask
            int mask = (-1) << (32 - Integer.parseInt(last_mask[1]));

            // Find lowest, highest IP address
            int lowest = addr & mask;
            int highest = lowest + (~mask);

            CompiledIPAddrRange.add(new Pair(lowest, highest));
        }
    }

    /*
     * Method to determine if an IP address belongs to CloudFlare
     *
     * @param string IP
     * @return boolean
     */
    public static boolean isCloudFlareIPAddress(String clientAddress) {
        String[] splitIPAddr = clientAddress.split("\\.");

        int addr = ((Integer.parseInt(splitIPAddr[0]) << 24) & 0xFF000000)
                | ((Integer.parseInt(splitIPAddr[1]) << 16) & 0xFF0000)
                | ((Integer.parseInt(splitIPAddr[2]) << 8) & 0xFF00)
                | (Integer.parseInt(splitIPAddr[3]) & 0xFF);
        
        //System.out.println(addr);
        for (Pair<Integer, Integer> cloudFlareIP : CompiledIPAddrRange) {
            //System.out.println("Left: " + cloudFlareIP.left + " right: " + cloudFlareIP.right);
            if (addr >= cloudFlareIP.left && addr <= cloudFlareIP.right) {
                return true;
            }
        }
        return false;
    }
}
