package innkfx.strategies.catchthechaos.alligator;

import com.dukascopy.api.*;
import innkfx.strategies.catchthechaos.common.ChIndicatorMan;

public class ChAlligatorMan extends ChIndicatorMan<ChAlligator> {
    public static final String INDICATOR_NAME = "ALLIGATOR";

    public ChAlligatorMan(IContext context, Instrument[] instruments, Period period) {
        super(context, instruments, period, INDICATOR_NAME);
    }

    public void onBar(Instrument instrument, IBar bar) throws JFException {
        add(buildAlligator(instrument, getPeriod(), bar.getTime(), 1));
    }

    public ChAlligator buildAlligator(Instrument instrument, Period period, long timestamp, int shift) throws JFException {
        double[] alli = calculateAlligator(instrument, period, shift);

        return new ChAlligator(instrument, period, timestamp, alli[0], alli[1], alli[2]);
    }

    public double[] calculateAlligator(Instrument instrument, Period period, int shift) throws JFException {
        Object[] alligator = getIndicators().calculateIndicator(instrument, period, new OfferSide[] {OfferSide.BID}, INDICATOR_NAME,
                new IIndicators.AppliedPrice[] { IIndicators.AppliedPrice.MEDIAN_PRICE }, new Object[]{13, 8, 5}, shift);

        return new double[] {(double) alligator[0], (double) alligator[1], (double) alligator[2]};

    }

//    public ChAlligator findAlligatorByJaw(double jaw) {
//        Optional<ChAlligator> opt = all().stream().filter(al -> al.getJTL()[0] == jaw).findFirst();
//
//        return opt.isPresent() ? opt.get() : null;
//    }
//
//    public ChAlligator findAlligatorByTeeth(double teeth) {
//        Optional<ChAlligator> opt = all().stream().filter(al -> al.getJTL()[1] == teeth).findFirst();
//
//        return opt.isPresent() ? opt.get() : null;
//    }
//
//    public ChAlligator findAlligatorByLips(double lips) {
//        Optional<ChAlligator> opt = all().stream().filter(al -> al.getJTL()[2] == lips).findFirst();
//
//        return opt.isPresent() ? opt.get() : null;
//    }

//    public boolean doCrossWithLastFactorial() {
//        return false;
//    }
}
