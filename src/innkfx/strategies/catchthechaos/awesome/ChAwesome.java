package innkfx.strategies.catchthechaos.awesome;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;

import innkfx.strategies.catchthechaos.common.ChIndicator;

public class ChAwesome extends ChIndicator {
    private double value;
    private boolean rise;
    private boolean fall;

    private boolean performed;
    private boolean invalid;

    private boolean unreal;

    public ChAwesome(Instrument instrument, Period period, long timestamp, double value) {
        super(instrument, period, timestamp);

        this.value = value;
        this.rise = false;
        this.fall = false;

        performed = false;
        invalid = false;
        unreal = false;
    }

    public double getValue() {
        return value;
    }

    public double getValue1000() {
        return value * 1000;
    }

    public void markAsRise() {
        rise = true;
    }

    public void markAsFall() {
        fall = true;
    }

    public boolean isRise() {
        return rise;
    }

    public boolean isFall() {
        return fall;
    }

    public void perform() {
        performed = true;
    }

    public void invalid() {
        invalid = true;
    }

    public boolean isPerformed() {
        return performed;
    }

    public boolean isValid() {
        return !invalid;
    }

    public void unreal() { unreal = true; }

    public boolean isUnreal() { return unreal; }
}
