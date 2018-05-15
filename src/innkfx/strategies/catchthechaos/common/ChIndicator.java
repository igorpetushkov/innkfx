package innkfx.strategies.catchthechaos.common;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;
import innkfx.strategies.catchthechaos.fractal.ChFractal;
import innkfx.utils.DateUtils;

public class ChIndicator {
    private Instrument instrument;
    private Period period;
    private long timestamp;

    public ChIndicator(Instrument instrument, Period period, long timestamp) {
        this.instrument = instrument;
        this.period = period;
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTime() {
        return DateUtils.formatToGMT0(timestamp);
    }

    public String getTimeGMT(String gmt) {
        return DateUtils.formatToGMT(timestamp, gmt);
    }
}
