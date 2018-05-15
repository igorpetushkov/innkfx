package innkfx.strategies.catchthechaos.order;

import com.dukascopy.api.*;
import com.dukascopy.api.IContext;
import innkfx.strategies.catchthechaos.common.ChIndicatorMan;
import innkfx.strategies.catchthechaos.fractal.ChFractal;

import java.util.*;
import java.util.stream.Collectors;

public class ChOrderSignalMan extends ChIndicatorMan<ChOrderSignal> {
    public ChOrderSignalMan(IContext context, Instrument[] instruments, Period period) {
        super(context, instruments, period, null);
    }

    public void addOrderSignal(Instrument instrument, Period period, long timestamp, ChFractal.Side side, double price) {
        ChOrderSignal orderSignal = new ChOrderSignal(instrument, period, timestamp, side, price);

        add(orderSignal);
    }

    public List<ChOrderSignal> getUpOrderSignals() {
        return all().stream().filter(ChOrderSignal::isUP).collect(Collectors.toList());
    }
    public List<ChOrderSignal> getDownOrderSignals() {
        return all().stream().filter(ChOrderSignal::isDown).collect(Collectors.toList());
    }

    public ChOrderSignal getLastUpOrderSignal() {
        List<ChOrderSignal> upOrderSignals = getUpOrderSignals();

        return upOrderSignals.size() > 0 ? upOrderSignals.get(upOrderSignals.size() - 1) : null;
    }

    public ChOrderSignal getLastDownOrderSignal() {
        List<ChOrderSignal> downOrderSignals = getDownOrderSignals();

        return downOrderSignals.size() > 0 ? downOrderSignals.get(downOrderSignals.size() - 1) : null;
    }
}
