package research.dlsu.cacaoapp;

import android.util.Log;

/**
 * Created by courtneyngo on 5/2/16.
 */
public class RemoteServer {

    // A-pe phone serial number 356899054383422

    public static String IPADDRESS = "192.168.1.133:8081";
    public static String SCHEMA = "http://";
    public static String WEBAPP = "ServerCacao";
    public static String INSERTCACAO = "insertcacao";
    public static String INSERTCACAOUPDATE = "insertcacaoupdate";
    public static String GETRESULTS = "getresults";


    public static String buildInsertCacaoUri(String ipaddress){
        Log.i("IPADDRESS", SCHEMA + ipaddress + "/" + WEBAPP + "/" + INSERTCACAO);
        return SCHEMA + ipaddress + "/" + WEBAPP + "/" + INSERTCACAO;
    }

    public static String buildInsertCacaoUpdateUri(String ipaddress){
        Log.i("IPADDRESS", SCHEMA + ipaddress + "/" + WEBAPP + "/" + INSERTCACAOUPDATE);
        return SCHEMA + ipaddress + "/" + WEBAPP + "/" + INSERTCACAOUPDATE;
    }

    public static String buildGetResultsUri(String ipaddress){
        Log.i("IPADDRESS", SCHEMA + ipaddress + "/" + WEBAPP + "/" + GETRESULTS);
        return SCHEMA + ipaddress + "/" + WEBAPP + "/" + GETRESULTS;
    }
}

