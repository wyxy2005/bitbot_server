package bitbot.handler;

import bitbot.util.HttpClient;
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
        "162.158.0.0/15",
        "104.16.0.0/12",
        "172.64.0.0/13"
    };

    private static final String[] IPAddress_v6 = {
        "2400:cb00::/32",
        "2405:8100::/32",
        "2405:b500::/32",
        "2606:4700::/32",
        "2803:f800::/32"
    };

    private static final List<Pair<Integer, Integer>> CompiledIPAddrRange = new ArrayList<>();

    static {
        String[] usingAddressList;

        String HttpGetResult = HttpClient.httpsGet("https://www.cloudflare.com/ips-v4", "");
        if (HttpGetResult == null) {
            usingAddressList = IPAddress_v4; // fallbacks
        } else {
            /*     List<String> addresses = new ArrayList<>();

            Pattern p = Pattern.compile("\\R");
            Matcher m = p.matcher(HttpGetResult);
            while (m.find()) {
                addresses.add(m.group());
            }
            
            usingAddressList = new String[addresses.size()];
            for (int i = 0; i < addresses.size(); i++) {
                usingAddressList[i] = addresses.get(i);
            }*/
            usingAddressList = HttpGetResult.split("\n");
        }

        for (String CloudFlareIP : IPAddress_v4) {
            /*       if (CloudFlareIP.isEmpty()) {
             continue;
             }
             System.out.println(CloudFlareIP);*/
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
