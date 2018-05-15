package innkfx.strategies.catchthechaos.fractal;

import com.dukascopy.api.*;
import innkfx.strategies.catchthechaos.common.ChIndicatorMan;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChFractalMan extends ChIndicatorMan<ChFractal> {
    public static final String INDICATOR_NAME = "FRACTAL";

    public ChFractalMan(IContext context, Instrument[] instruments, Period period) {
        super(context, instruments, period, INDICATOR_NAME);
    }

    public void onBar(Instrument instrument, IBar bar) throws JFException {
        addAll(buildFractals(instrument, getPeriod(), bar.getTime()));
    }

    public List<ChFractal> buildFractals(Instrument instrument, Period period, long timestamp) throws JFException {
        double[] fracs = calculateFractal(instrument, period);

        List<ChFractal> fractals = new ArrayList<>();

        if (fracs[0] > 0) {
            fractals.add(new ChFractal(instrument, getPeriod(), timestamp, ChFractal.Side.UP, fracs[0]));
        }

        if (fracs[1] > 0) {
            fractals.add(new ChFractal(instrument, getPeriod(), timestamp, ChFractal.Side.DOWN, fracs[1]));
        }

        return fractals;
    }

    public double[] calculateFractal(Instrument instrument, Period period) throws JFException {
        Object[] fracs = getIndicators().calculateIndicator(instrument, period, new OfferSide[] {OfferSide.BID}, INDICATOR_NAME,
                new IIndicators.AppliedPrice[] { IIndicators.AppliedPrice.MEDIAN_PRICE }, new Object[]{2}, 3);

        return new double[]{(double) fracs[0], (double) fracs[1]};
    }

    public List<ChFractal> getUpFractals() {
        return all().stream().filter(ChFractal::isUP).collect(Collectors.toList());
    }

    public List<ChFractal> getDownFractals() {
        return all().stream().filter(ChFractal::isDown).collect(Collectors.toList());
    }

    public ChFractal getLastUpFractal() {
        List<ChFractal> upFrs = getUpFractals();

        return upFrs.size() > 0 ? upFrs.get(upFrs.size() - 1) : null;
    }

    public ChFractal getLastDownFractal() {
        List<ChFractal> downFrs = getDownFractals();

        return downFrs.size() > 0 ? downFrs.get(downFrs.size() - 1) : null;
    }
}
