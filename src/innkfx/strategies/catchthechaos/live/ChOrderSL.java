package innkfx.strategies.catchthechaos.live;

import com.dukascopy.api.*;
import innkfx.strategies.catchthechaos.CatchTheChaos;

public class ChOrderSL {
    public void updateOrderSL(IOrder order, ITick tick) throws JFException {
        String label = order.getLabel();

        if (label.startsWith("CTC_AWE")) {
            updateAweOrderSL(order, tick);
        } else if (label.startsWith("CTC_FRA")) {
            updateFraOrderSL(order, tick);
        } else if (label.startsWith("CTC_SIG")) {
            updateSigOrderSL(order, tick);
        }
    }

    private void updateAweOrderSL(IOrder order, ITick tick) throws JFException {
        double plPips = order.getProfitLossInPips();
        double bidVol = tick.getBidVolume();

        if (plPips >= 5 && bidVol >= 2) {
            int slSideVector = order.getOrderCommand().isLong() ? -1 : 1;
            double slPrice = tick.getBid() + (CatchTheChaos.TP_AWE_PIPS_MIN * order.getInstrument().getPipValue() * slSideVector);

            order.setStopLossPrice(
                    slPrice,
                    OfferSide.BID
            );
        }
    }

    private void updateSigOrderSL(IOrder order, ITick tick) throws JFException {
        double plPips = order.getProfitLossInPips();
        double bidVol = tick.getBidVolume();

        if (plPips >= 5 && bidVol >= 2 && order.getTrailingStep() == 0) {
            double slPre = 7;
            double tls = CatchTheChaos.TRAILING_STEP;

            if ((plPips >= 5) && plPips < 10) {
                slPre = 7;
                tls = 0;
            } else if (plPips >= 10 && plPips < 20) {
                slPre = 8;
                tls = 0;
            } else if (plPips >= 20) {
                slPre = 10;
            }

            int slSideVector = order.getOrderCommand().isLong() ? -1 : 1;
            double slPrice = tick.getBid() + (slPre * order.getInstrument().getPipValue() * slSideVector);

            order.setStopLossPrice(
                    slPrice,
                    OfferSide.BID,
                    tls
            );
        }
    }

    private void updateFraOrderSL(IOrder order, ITick tick) throws JFException {
        double plPips = order.getProfitLossInPips();
        double bidVol = tick.getBidVolume();

        if (plPips >= 15 && bidVol >= 2 && order.getTrailingStep() == 0) {
            double slPre = 1;
            double tls = CatchTheChaos.TRAILING_STEP;

            if (plPips >= 15 && plPips < 20) {
                slPre = 15;
                tls = 0;
            } else if (plPips >= 20 && plPips < 30) {
                slPre = 15;
                tls = 0;
            } else if (plPips >= 30) {
                slPre = 20;
                tls = 10;
            }

            int slSideVector = order.getOrderCommand().isLong() ? -1 : 1;
            double slPrice = tick.getBid() + (slPre * order.getInstrument().getPipValue() * slSideVector);

            order.setStopLossPrice(
                    slPrice,
                    OfferSide.BID,
                    tls
            );
        }
    }
}
