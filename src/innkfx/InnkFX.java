package innkfx;

import com.dukascopy.api.*;
import com.dukascopy.api.plugins.IPluginContext;
import com.dukascopy.api.plugins.Plugin;

import innkfx.dev.runners.InnkFX_DEV_ST_GUI;
import innkfx.widgets.history.HistoryTable;
import innkfx.strategies.catchthechaos.CatchTheChaos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InnkFX extends Plugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(InnkFX_DEV_ST_GUI.class);

    private IPluginContext context;
    private HistoryTable historyTable;
    private CatchTheChaos cth;

    @Override
    public void onStart(IPluginContext context) throws JFException {
        this.context = context;

        historyTable = new HistoryTable(context);
        historyTable.init(HistoryTable.UI.JF);

        cth = new CatchTheChaos(LOGGER);
        cth.onStart(context);
    }

//    @Override
//    public void onTick(Instrument instrument, final ITick tick){
//    }

    @Override
    public void onStop() throws JFException {
        historyTable.close();
        context.getConsole().getOut().println("InnkFX stop");
    }
}
