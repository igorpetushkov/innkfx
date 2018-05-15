package innkfx.dev.runners;

import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import innkfx.dev.ST_OrderHistoryStorage;

public class InnkFX_DEV_CONSOLE {
    private static String jnlpUrl = "http://platform.dukascopy.com/demo/jforex.jnlp";
    private static String userName = "***";
    private static String password = "***";

    public static void main(String[] args) throws Exception {
        final IClient client = ClientFactory.getDefaultInstance();
        connect(client);

        // * * * * //

        ST_OrderHistoryStorage.init(client);

        // - - - - //
    }

    private static void connect(IClient client) throws Exception {
        client.connect(jnlpUrl, userName, password);

        int i = 10;
        while (i > 0 && !client.isConnected()) {
            Thread.sleep(1000);
            i--;
        }
        if (!client.isConnected()) {
            System.err.println("Failed to connect Dukascopy servers");
            System.exit(1);
        }
    }
}
