package innkfx.strategies.catchthechaos.patterns;

import java.util.Date;

import com.dukascopy.api.*;
import com.dukascopy.api.drawings.ITextChartObject;

import innkfx.strategies.catchthechaos.CatchTheChaos;
import innkfx.strategies.catchthechaos.common.ChPerformsType;
import innkfx.strategies.catchthechaos.awesome.ChAwesome;
import innkfx.utils.DateUtils;

import java.awt.*;

public class ChAwesomePerforms {
    private CatchTheChaos ctc;
    private IContext context;

    public ChAwesomePerforms(CatchTheChaos ctc, IContext context) {
        this.ctc = ctc;
        this.context = context;
    }

    public void init() { }

    public void onBar(Instrument instrument, IBar bar) throws JFException {
        if (isSleepAweTime(bar.getTime())) {
            return;
        }

        catchRiseAwe(instrument, bar);
        catchFallAwe(instrument, bar);
    }

//    public void onTick(Instrument instrument, ITick tick) throws JFException {}
//    public void onStop() throws JFException {}

    private void catchRiseAwe(Instrument instrument, IBar bar) throws JFException {
        ChAwesome firstLastBarAwe = ctc.getChAwesomeMan().last(0);
        ChAwesome secondLastBarAwe = ctc.getChAwesomeMan().last(1);

        if (secondLastBarAwe != null &&  secondLastBarAwe.isValid() && !secondLastBarAwe.isPerformed() && secondLastBarAwe.isRise()) {

            double value = secondLastBarAwe.getValue1000();

            secondLastBarAwe.perform();
            ctc.submitOrder(instrument, ChPerformsType.AWESOME_UP);

            if (CatchTheChaos.SHOW_MARK) {
                __mark(
                        secondLastBarAwe.getValue1000(),
                        firstLastBarAwe.getValue1000(),
                        Color.BLUE, bar, instrument);
            }
        }
    }

    private void catchFallAwe(Instrument instrument, IBar bar) throws JFException {
        ChAwesome firstLastBarAwe = ctc.getChAwesomeMan().last(0);
        ChAwesome secondLastBarAwe = ctc.getChAwesomeMan().last(1);

        if (secondLastBarAwe != null &&  secondLastBarAwe.isValid() && !secondLastBarAwe.isPerformed() && secondLastBarAwe.isFall()) {

            double value = secondLastBarAwe.getValue1000();

            secondLastBarAwe.perform();
            ctc.submitOrder(instrument, ChPerformsType.AWESOME_DOWN);

            if (CatchTheChaos.SHOW_MARK) {
                __mark(
                        secondLastBarAwe.getValue1000(),
                        firstLastBarAwe.getValue1000(),
                        Color.RED, bar, instrument);
            }
        }
    }

    private boolean isSleepAweTime(long timestamp) {
        int tickHour = new Integer(DateUtils.formatToGMT0(timestamp).substring(6, 8));

        return tickHour >= CatchTheChaos.SLEEP_TIME_AWE_FROM || tickHour < CatchTheChaos.SLEEP_TIME_AWE_TO;
    }

    private void __mark(double a1, double a2, Color color, IBar tick, Instrument instrument) {
        IChart chart = context.getChart(instrument);

        ITextChartObject text = chart.getChartObjectFactory().createText("ttt-" + tick.getTime(), tick.getTime(), tick.getClose());
        text.setText("_ _ _ _", new Font(Font.DIALOG, Font.PLAIN, 30));
        text.setColor(color);
        chart.add(text);

        ITextChartObject text2 = chart.getChartObjectFactory().createText("ddd-" + tick.getTime(), tick.getTime(), tick.getClose());
        text2.setText("a" + String.format("%.2f", a1) + "b" + String.format("%.2f", a2),
                new Font(Font.DIALOG, Font.PLAIN, 16));
        text2.setColor(Color.YELLOW);
        chart.add(text2);
    }
}
