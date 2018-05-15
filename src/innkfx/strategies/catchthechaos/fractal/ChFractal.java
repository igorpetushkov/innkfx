package innkfx.strategies.catchthechaos.fractal;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;

import innkfx.strategies.catchthechaos.common.ChIndicator;

public class ChFractal extends ChIndicator {
    public enum Side {
        UP("UP"),
        DOWN("DOWN");

        private String text;

        Side(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private Side side;
    private double price;

    private boolean performed;
    private boolean invalid;

    public ChFractal(Instrument instrument, Period period, long timestamp, Side side, double price) {
        super(instrument, period, timestamp);

        this.side = side;
        this.price = price;

        performed = false;
        invalid = false;
    }

    public double getPrice() {
        return price;
    }

    public Side getSide() {
        return side;
    }

    public boolean isUP() {
        return side.equals(Side.UP);
    }

    public boolean isDown() {
        return side.equals(Side.DOWN);
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
}
