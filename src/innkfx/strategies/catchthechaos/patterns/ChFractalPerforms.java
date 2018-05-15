package innkfx.strategies.catchthechaos.patterns;

import com.dukascopy.api.*;
import com.dukascopy.api.drawings.ITextChartObject;

import innkfx.strategies.catchthechaos.CatchTheChaos;
import innkfx.strategies.catchthechaos.alligator.ChAlligator;
import innkfx.strategies.catchthechaos.fractal.ChFractal;
import innkfx.strategies.catchthechaos.common.ChPerformsType;

import java.awt.*;

public class ChFractalPerforms {
    private CatchTheChaos ctc;
    private IContext context;

    public ChFractalPerforms(CatchTheChaos ctc, IContext context) {
        this.ctc = ctc;
        this.context = context;
    }

    public void init() { }

    public void onTick(Instrument instrument, ITick tick) throws JFException {
        catchWaveUp(instrument, tick);
        catchWaveDown(instrument, tick);
    }

//    public void onBar(Instrument instrument, IBar bar) throws JFException {}
//    public void onStop() throws JFException {}

    private void catchWaveUp(Instrument instrument, ITick tick) throws JFException {
        ChFractal lastUpFr = ctc.getChFractalMan().getLastUpFractal();

        if (isUpFractalCaught(lastUpFr, instrument, tick)) {
            if (doesPriceAboveLastAlligatorTeeth(tick)) {
                ChAlligator alligator = ctc.getChAlligatorMan().last();
                boolean isLipsAboveJaw = alligator.getLips() > alligator.getJaw();

                if (!isLipsAboveJaw) {
                    return;
                }


                if (isLastUpAlligatorAwake()) {
                    System.out.println("Fractal perform with LastUpAlligatorAwake");
                } else if (isLastUpAlligatorSatisfied(instrument, tick)) {
                    System.out.println("Fractal perform with LastUpAlligatorSatisfied");
                } else {
                    return;
                }

                lastUpFr.perform();
                ctc.submitOrder(instrument, ChPerformsType.FRACTAL_UP);

                if (CatchTheChaos.SHOW_MARK) {
                    __markFor(instrument, tick, Color.BLUE);
                }
            } else {
                lastUpFr.invalid();
            }
        }
    }

    private void catchWaveDown(Instrument instrument, ITick tick) throws JFException {
        ChFractal lastDownFr = ctc.getChFractalMan().getLastDownFractal();

        if (isDownFractalCaught(lastDownFr, instrument, tick)) {
            if (doesPriceBelowLastAlligatorTeeth(tick)) {
                ChAlligator alligator = ctc.getChAlligatorMan().last();
                boolean isLipsBelowJaw = alligator.getLips() < alligator.getJaw();

                if (!isLipsBelowJaw) {
                    return;
                }

                if (isLastDownAlligatorAwake()) {
                    System.out.println("Fractal perform with LastDownAlligatorAwake");
                } else if (isLastDownAlligatorSatisfied(instrument, tick)) {
                    System.out.println("Fractal perform with LastDownAlligatorSatisfied");
                } else {
                    return;
                }

                lastDownFr.perform();
                ctc.submitOrder(instrument, ChPerformsType.FRACTAL_DOWN);

                if (CatchTheChaos.SHOW_MARK) {
                    __markFor(instrument, tick, Color.RED);
                }
            } else {
                lastDownFr.invalid();
            }
        }
    }

    private boolean isUpFractalCaught(ChFractal fractal, Instrument instrument, ITick tick) {
        double adt = (CatchTheChaos.FRACTAL_OVERLAPPING / Math.pow(10, instrument.getPipScale()));

        return fractal != null && !fractal.isPerformed() && fractal.isValid() && fractal.getPrice() + adt < tick.getBid();
    }

    private boolean isDownFractalCaught(ChFractal fractal, Instrument instrument, ITick tick) {
        double adt = (CatchTheChaos.FRACTAL_OVERLAPPING / Math.pow(10, instrument.getPipScale()));

        return fractal != null && !fractal.isPerformed() && fractal.isValid() && fractal.getPrice() - adt > tick.getBid();
    }

    private boolean doesPriceAboveLastAlligatorTeeth(ITick tick) {
        ChAlligator alligator = ctc.getChAlligatorMan().last();
        double teeth = alligator.getTeeth();
        double bid = tick.getBid();

        return bid > teeth;
    }

    private boolean doesPriceBelowLastAlligatorTeeth(ITick tick) {
        ChAlligator alligator = ctc.getChAlligatorMan().last();
        double teeth = alligator.getTeeth();
        double bid = tick.getBid();

        return bid < teeth;
    }

    private boolean isLastUpAlligatorAwake() {
        ChAlligator alligator = ctc.getChAlligatorMan().last();
        double fromLipsToTeeth = alligator.valueFromLipsToTeeth1000();

        return fromLipsToTeeth < 0 && Math.abs(fromLipsToTeeth) >= 0.0957;
    }

    private boolean isLastDownAlligatorAwake() {
        ChAlligator alligator = ctc.getChAlligatorMan().last();
        double fromLipsToTeeth = alligator.valueFromLipsToTeeth1000();

        return fromLipsToTeeth > 0 && Math.abs(fromLipsToTeeth) >= 0.0957;
    }

    private boolean isLastUpAlligatorSatisfied(Instrument instrument, ITick tick) throws JFException {
        ChAlligator lastBarAlligator = ctc.getChAlligatorMan().last();
        ChAlligator lastSecAlligator = ctc.getChAlligatorMan().buildAlligator(instrument, Period.ONE_SEC, tick.getTime(), 1);

        double lastBarLT = lastBarAlligator.valueFromLipsToTeeth1000();
        double lastSecLT = lastSecAlligator.valueFromLipsToTeeth1000();

        return ((lastBarLT + lastSecLT) / 2) >= 0.0957 &&
                ((lastBarLT >= 0.05 && lastSecLT >= 0.05) || (lastBarLT >= 0.35));
    }

    private boolean isLastDownAlligatorSatisfied(Instrument instrument, ITick tick) throws JFException {
        ChAlligator lastBarAlligator = ctc.getChAlligatorMan().last();
        ChAlligator lastSecAlligator = ctc.getChAlligatorMan().buildAlligator(instrument, Period.ONE_SEC, tick.getTime(), 1);

        double lastBarLT = Math.abs(lastBarAlligator.valueFromLipsToTeeth1000());
        double lastSecLT = Math.abs(lastSecAlligator.valueFromLipsToTeeth1000());

        return ((lastBarLT + lastSecLT) / 2) >= 0.0957 &&
                ((lastBarLT >= 0.05 && lastSecLT >= 0.05) || (lastBarLT >= 0.47 && lastSecLT >= 0.037));
    }

    private void __markFor(Instrument instrument, ITick tick, Color color) throws JFException {
        ChAlligator barAlligator = ctc.getChAlligatorMan().last();
        ChAlligator secAlligator = ctc.getChAlligatorMan().buildAlligator(instrument, Period.ONE_SEC, tick.getTime(), 1);

        if (barAlligator != null && secAlligator != null) {
            double barLT = barAlligator.valueFromLipsToTeeth1000();
            double secTL = secAlligator.valueFromLipsToTeeth1000();

            __mark(barLT, secTL, color, tick, instrument);
        } else {
            System.out.println("error: barAlligator or secAlligator is NULL");
        }
    }

    private void __mark(double a1, double a2, Color color, ITick tick, Instrument instrument) {
        IChart chart = context.getChart(instrument);

        ITextChartObject text = chart.getChartObjectFactory().createText("ttt-" + tick.getTime(), tick.getTime(), tick.getBid());
        text.setText("___", new Font(Font.DIALOG, Font.PLAIN, 30));
        text.setColor(color);
        chart.add(text);

        ITextChartObject text2 = chart.getChartObjectFactory().createText("ddd-" + tick.getTime(), tick.getTime(), tick.getBid());
        text2.setText("a" + String.format("%.2f", a1) + "b" + String.format("%.2f", a2),
                new Font(Font.DIALOG, Font.PLAIN, 16));
        text2.setColor(Color.YELLOW);
        chart.add(text2);
    }
}
