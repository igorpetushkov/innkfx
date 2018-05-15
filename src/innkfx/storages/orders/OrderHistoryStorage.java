package innkfx.storages.orders;

import java.util.*;

import com.dukascopy.api.*;

import com.dukascopy.api.IContext;

import innkfx.utils.DateUtils;

public class OrderHistoryStorage {
    private static final long DEFAULT_FROM_TIME = DateUtils.getWeekStartTime();
    private static final Set<Instrument> DEFAULT_INSTRUMENTS = new HashSet<>(Arrays.asList(
        Instrument.EURUSD,
        Instrument.GBPUSD,
        Instrument.USDJPY,
        Instrument.USDCAD,
        Instrument.AUDUSD,
        Instrument.USDCHF,
        Instrument.NZDUSD
    ));

    private IHistory history;

    public OrderHistoryStorage(IContext context) {
        context.setSubscribedInstruments(DEFAULT_INSTRUMENTS);
        this.history = context.getHistory();
    }

//    public List<IOrder> getOrders(Set<Instrument> instruments) throws JFException {
//        return getOrders(instruments, DEFAULT_FROM_TIME);
//    }

//    public List<IOrder> getOrders(long fromTime, long toTime) throws JFException {
//        return getOrders(DEFAULT_INSTRUMENTS, fromTime, toTime);
//    }

    public List<IOrder> getOrders(Set<Instrument> instruments, long fromTime, long toTime) throws JFException {
        List<IOrder> buffer = new ArrayList<>();

        for (Instrument instrument : instruments) {
            List<IOrder> list = history.getOrdersHistory(instrument, fromTime, toTime);
            buffer.addAll(list);
        }

        return buffer;
    }
}
