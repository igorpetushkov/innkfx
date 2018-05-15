package innkfx.dev;

import com.dukascopy.api.*;
import com.dukascopy.api.JFException;
import com.dukascopy.api.plugins.IMessageListener;
import com.dukascopy.api.plugins.IPluginContext;
import com.dukascopy.api.plugins.Plugin;
import innkfx.widgets.history.HistoryTable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PL_HistoryTable extends Plugin implements IMessageListener {
    public static final Period period = Period.WEEKLY;
    public static final Set<Instrument> instruments = new HashSet<>(Arrays.asList(
        Instrument.EURUSD,
        Instrument.GBPUSD
    ));

    private IPluginContext context;

    @Override
    public void onStart(final IPluginContext context) throws JFException {
        this.context = context;

        HistoryTable table = new HistoryTable(context);
        table.init(HistoryTable.UI.SIMPLE);
    }

    @Override
    public void onStop() throws JFException {
//        historyTable.close();
//        context.stop();
    }

    @Override
    public void onMessage(IMessage message) throws JFException {

    }
}
