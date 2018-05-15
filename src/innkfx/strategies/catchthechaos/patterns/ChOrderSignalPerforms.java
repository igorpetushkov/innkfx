package innkfx.strategies.catchthechaos.patterns;

import com.dukascopy.api.*;

import innkfx.strategies.catchthechaos.CatchTheChaos;
import innkfx.strategies.catchthechaos.common.ChPerformsType;
import innkfx.strategies.catchthechaos.order.ChOrderSignal;

public class ChOrderSignalPerforms {
    private CatchTheChaos ctc;

    private static final double ADT = 1;

    public ChOrderSignalPerforms(CatchTheChaos ctc) {
        this.ctc = ctc;
    }

    public void onTick(Instrument instrument, ITick tick) throws JFException {
        catchUpOrderSignal(instrument, tick);
        catchDownOrderSignal(instrument, tick);
    }

//    public void onStop() throws JFException {}

    private void catchUpOrderSignal(Instrument instrument, ITick tick) throws JFException {
        ChOrderSignal lastUpOrderSignal = ctc.getChOrderSignalMan().getLastUpOrderSignal();

        if (isUpFractalCaught(lastUpOrderSignal, instrument, tick)) {
            if (tick.getBidVolume() > 1) {
                lastUpOrderSignal.perform();
                ctc.submitOrder(instrument, ChPerformsType.ORDER_SIGNAL_UP, lastUpOrderSignal.getPrice());
            }
        }
    }

    private void catchDownOrderSignal(Instrument instrument, ITick tick) throws JFException {
        ChOrderSignal lastDownOrderSignal = ctc.getChOrderSignalMan().getLastDownOrderSignal();

        if (isDownOrderSignalCaught(lastDownOrderSignal, instrument, tick)) {
            if (tick.getBidVolume() > 1) {
                lastDownOrderSignal.perform();
                ctc.submitOrder(instrument, ChPerformsType.ORDER_SIGNAL_DOWN, lastDownOrderSignal.getPrice());
            }
        }
    }

    private boolean isUpFractalCaught(ChOrderSignal orderSignal, Instrument instrument, ITick tick) {
        double adt = (ADT / Math.pow(10, instrument.getPipScale()));

        return orderSignal != null && !orderSignal.isPerformed() && orderSignal.getPrice() + adt < tick.getBid();
    }

    private boolean isDownOrderSignalCaught(ChOrderSignal orderSignal, Instrument instrument, ITick tick) {
        double adt = (ADT / Math.pow(10, instrument.getPipScale()));

        return orderSignal != null && !orderSignal.isPerformed() && orderSignal.getPrice() - adt > tick.getBid();
    }
}
