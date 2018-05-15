package innkfx.strategies.catchthechaos.order;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;
import innkfx.strategies.catchthechaos.fractal.ChFractal;

public class ChOrderSignal extends ChFractal {
    public ChOrderSignal(Instrument instrument, Period period, long timestamp, Side side, double price) {
        super(instrument, period, timestamp, side, price);
    }
}
