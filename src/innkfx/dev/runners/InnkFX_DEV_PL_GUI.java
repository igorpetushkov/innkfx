package innkfx.dev.runners;

import com.dukascopy.api.plugins.PluginGuiListener;
import com.dukascopy.api.plugins.widget.IPluginWidget;
import com.dukascopy.api.plugins.widget.PluginWidgetListener;
import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import innkfx.dev.PL_HistoryTable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.UUID;

public class InnkFX_DEV_PL_GUI {
    private static String jnlpUrl = "http://platform.dukascopy.com/demo/jforex.jnlp";
    private static String userName = "***";
    private static String password = "***";

    private static JFrame frame;
    private static UUID pluginId = null;

    public static void main(String[] args) throws Exception {
        final IClient client = ClientFactory.getDefaultInstance();
        connect(client);

        client.setSubscribedInstruments(PL_HistoryTable.instruments);
        pluginId = client.runPlugin(new PL_HistoryTable(), null, getPluginGuiListener(client));
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

    private static PluginGuiListener getPluginGuiListener(final IClient client) {
        return new PluginGuiListener(){
            @Override
            public void onWidgetAdd(IPluginWidget pluginWidget) {
                frame = buildPluginFrame(pluginWidget);
            }
            @Override
            public void onWidgetListenerAdd(final PluginWidgetListener listener){
                frame.addWindowListener(new WindowAdapter(){
                    @Override
                    public void windowClosing(WindowEvent e) {
                        listener.onWidgetClose();
                        client.stopPlugin(pluginId);
                        System.exit(0);
                    }
                });
            }
        };
    }

    private static JFrame buildPluginFrame(IPluginWidget pluginWidget) {
        JFrame frame = new JFrame("InnkFX_DEV_PL_GUI");
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenSize = tk.getScreenSize();
        int screenHeight = screenSize.height;
        int screenWidth = screenSize.width;
        frame.setSize(screenWidth / 2, screenHeight / 2);
        frame.setLocation(screenWidth / 4, screenHeight / 4);

        JPanel panel = pluginWidget.getContentPanel();
        panel.setMinimumSize(new Dimension(800,600));
        panel.setPreferredSize(new Dimension(800,600));

        frame.add(panel);
        frame.pack();
        frame.setVisible(true);

        return frame;
    }
}
