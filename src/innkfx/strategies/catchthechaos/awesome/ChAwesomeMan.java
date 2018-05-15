package innkfx.strategies.catchthechaos.awesome;

import com.dukascopy.api.*;
import innkfx.strategies.catchthechaos.common.ChIndicatorMan;

import java.util.stream.Collectors;

public class ChAwesomeMan extends ChIndicatorMan<ChAwesome> {
    public static final String INDICATOR_NAME = "AWESOME2";

    public ChAwesomeMan(IContext context, Instrument[] instruments, Period period) {
        super(context, instruments, period, INDICATOR_NAME);
    }

    public void onBar(Instrument instrument, IBar bar) throws JFException {
        add(buildAwesome(instrument, getPeriod(), bar.getTime()));
    }

    public ChAwesome buildAwesome(Instrument instrument, Period period, long timestamp) throws JFException {
        ChAwesome awe = new ChAwesome(instrument, period, timestamp, calculateAwesome(instrument, period));

        return markAsRiseOrFall(awe);
    }

    public double calculateAwesome(Instrument instrument, Period period) throws JFException {
        Object[] awes = getIndicators().calculateIndicator(instrument, period, new OfferSide[] {OfferSide.BID}, INDICATOR_NAME,
                new IIndicators.AppliedPrice[] { IIndicators.AppliedPrice.MEDIAN_PRICE }, new Object[]{5, 0, 34, 0}, 1);

        return (double) awes[0];
    }

    private ChAwesome markAsRiseOrFall(ChAwesome currAwe) {
        ChAwesome lastAwe = last();

        if (lastAwe != null) {
            if (lastAwe.getValue() < 0 && currAwe.getValue() > 0) {
                currAwe.markAsRise();
//                System.out.println("UP");
//                System.out.println(currAwe.getTime());
//                System.out.println("lastbar");
//                System.out.println(currAwe.getValue1000());
//                System.out.println(lastAwe.getTime());
//                System.out.println(lastAwe.getValue1000());

//                int ee = 0;
            } else if (lastAwe.getValue() > 0 && currAwe.getValue() < 0) {
                currAwe.markAsFall();
//                System.out.println("DOWN");
//                System.out.println(currAwe.getTime());
//                System.out.println("lastbar");
//                System.out.println(currAwe.getValue1000());
//                System.out.println(lastAwe.getTime());
//                System.out.println(lastAwe.getValue1000());

//                int ee = 0;
            }
        }

        return currAwe;
    }
}
