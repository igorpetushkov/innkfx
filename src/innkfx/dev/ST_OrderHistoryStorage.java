package innkfx.dev;

import java.util.Arrays;

import java.util.HashSet;
import java.util.Set;

import com.dukascopy.api.*;
import com.dukascopy.api.system.IClient;
import com.dukascopy.api.system.IStrategyExceptionHandler;
import innkfx.storages.orders.OrderHistoryStorage;

public class ST_OrderHistoryStorage implements IStrategy {
    public static final Period period = Period.WEEKLY;
    public static final Set<Instrument> instruments = new HashSet<>(Arrays.asList(
            Instrument.EURUSD,
            Instrument.GBPUSD
    ));

    public static void init(IClient client) {
        client.setSubscribedInstruments(instruments);
        client.startStrategy(new ST_OrderHistoryStorage(), getStrategyExceptionHandler());
    }


    private IConsole console;

    private OrderHistoryStorage storage;

    public void onStart(IContext context) throws JFException {
        this.console = context.getConsole();
        this.storage = new OrderHistoryStorage(context);

//        List<IOrder> orders = this.storage.getOrders(instruments, period);
    }

    public void onAccount(IAccount account) throws JFException {
    }

    public void onMessage(IMessage message) throws JFException {
    }

    public void onStop() throws JFException {
    }

    public void onTick(Instrument instrument, ITick tick) throws JFException {
    }

    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
    }

    private static IStrategyExceptionHandler getStrategyExceptionHandler() {
        return new IStrategyExceptionHandler() {
            @Override
            public void onException(long strategyId, Source source, Throwable t) {

            }
        };
    }
}
