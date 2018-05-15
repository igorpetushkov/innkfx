package innkfx.strategies.catchthechaos.common;

import java.util.*;

import com.dukascopy.api.IContext;
import com.dukascopy.api.IIndicators;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;

public class ChIndicatorMan<T extends ChIndicator> {
    private IContext context;
    private Instrument[] instruments;
    private Period period;
    private String indicatorName;

    private List<T> buffer;

    public ChIndicatorMan(IContext context, Instrument[] instruments, Period period, String indicatorName) {
        this.context = context;
        this.instruments = instruments;
        this.period = period;
        this.indicatorName = indicatorName;

        buffer = new ArrayList<>();
    }

    public void init() {
        if (indicatorName != null) {
            for (Instrument instrument: instruments) {
                context.getChart(instrument).add(getIndicators().getIndicator(indicatorName));
            }
        }
    }

    public Period getPeriod() {
        return period;
    }

    public IIndicators getIndicators() {
        return context.getIndicators();
    }

    public void add(T item) {
        buffer.add(item);
    }

    public void addAll(T[] items) {
        buffer.addAll(Arrays.asList(items));
    }

    public void addAll(List<T> items) {
        buffer.addAll(items);
    }

    public List<T> all() {
        return buffer;
    }

    public T last() {
        return last(0);
    }

    public T last(int shift) {
        return buffer.size() > shift ? buffer.get(buffer.size() - (1 + shift)) : null;
    }

    public T find(long timestamp) {
        Optional<T> opt = buffer.stream().filter(item -> item.getTimestamp() == timestamp).findFirst();

        return opt.isPresent() ? opt.get() : null;
    }

}
