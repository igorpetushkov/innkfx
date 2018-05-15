package innkfx.strategies.catchthechaos.alligator;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;

import innkfx.strategies.catchthechaos.common.ChIndicator;

public class ChAlligator extends ChIndicator {
    private double jaw;
    private double teeth;
    private double lips;

    public ChAlligator(Instrument instrument, Period period, long timestamp, double jaw, double teeth, double lips) {
        super(instrument, period, timestamp);

        this.jaw = jaw;
        this.teeth = teeth;
        this.lips = lips;
    }

    public double getJaw() {
        return jaw;
    }

    public double getTeeth() {
        return teeth;
    }

    public double getLips() {
        return lips;
    }

    public double[] getJTL() {
        return new double[]{ this.jaw, this.teeth, this.lips };
    }

    public double valueFromJawToTeeth() {
        return getJaw() - getTeeth();
    }

    public double valueFromJawToTeeth1000() {
        return valueFromJawToTeeth() * 1000;
    }

    public double valueFromLipsToTeeth() {
        return getLips() - getTeeth();
    }

    public double valueFromLipsToTeeth1000() {
        return valueFromLipsToTeeth() * 1000;
    }

}
