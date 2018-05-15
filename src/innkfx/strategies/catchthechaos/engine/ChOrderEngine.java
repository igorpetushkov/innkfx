package innkfx.strategies.catchthechaos.engine;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.dukascopy.api.*;
import com.dukascopy.api.IEngine.*;
import innkfx.strategies.catchthechaos.CatchTheChaos;
import innkfx.strategies.catchthechaos.common.ChPerformsType;
import innkfx.strategies.catchthechaos.fractal.ChFractal;
import innkfx.strategies.catchthechaos.fractal.ChFractalMan;

public class ChOrderEngine {
    private IEngine engine;
    private IHistory history;

    private ChFractalMan chFractalMan;

    public ChOrderEngine(IContext context, ChFractalMan chFractalMan) {
        this.engine = context.getEngine();
        this.history = context.getHistory();
        this.chFractalMan = chFractalMan;
    }

    public void submitOrder(Instrument instrument, ChPerformsType performType, double price) throws JFException {
        OrderCommand side;
        String performTypeShort;
        double sl;

        List<IOrder> openOrders = engine.getOrders();

        if (openOrders.size() > 2) {
            System.out.println("TOO many open orders!");
            return;
        } else if (openOrders.size() == 1) {
            IOrder openOrder = openOrders.get(0);
            if (openOrder.getProfitLossInPips() < 0) {
                openOrder.close();
                openOrder.waitForUpdate(2000, TimeUnit.MILLISECONDS);
            } else if (openOrder.getProfitLossInPips() > 15) {
                System.out.println("ONE open order already has profit more 15");
                return;
            }
        }

        if (performType.equals(ChPerformsType.AWESOME_UP)){
            side = OrderCommand.BUY;
            performTypeShort = "AWE";
            sl = calculateSL(instrument, side, CatchTheChaos.SL_AWE_PIPS_MAX);
        } else if (performType.equals(ChPerformsType.AWESOME_DOWN)) {
            side = OrderCommand.SELL;
            performTypeShort = "AWE";
            sl = calculateSL(instrument, side, CatchTheChaos.SL_AWE_PIPS_MAX);
        } else if (performType.equals(ChPerformsType.FRACTAL_UP)) {
            side = OrderCommand.BUY;
            performTypeShort = "FRA";
            sl = calculateSL(instrument, side, CatchTheChaos.SL_FRA_PIPS_MAX);
        } else if (performType.equals(ChPerformsType.FRACTAL_DOWN)) {
            side = OrderCommand.SELL;
            performTypeShort = "FRA";
            sl = calculateSL(instrument, side, CatchTheChaos.SL_FRA_PIPS_MAX);
        } else if (performType.equals(ChPerformsType.ORDER_SIGNAL_UP)) {
            side = OrderCommand.BUY;
            performTypeShort = "SIG";
            sl = calculateSL(instrument, side, CatchTheChaos.SL_SIG_PIPS_MAX);
        } else if (performType.equals(ChPerformsType.ORDER_SIGNAL_DOWN)) {
            side = OrderCommand.SELL;
            performTypeShort = "SIG";
            sl = calculateSL(instrument, side, CatchTheChaos.SL_SIG_PIPS_MAX);
        } else {
            return;
        }

        double tp = 0;

        IOrder order = engine.submitOrder(
                "CTC_" + performTypeShort + "_" + side.toString() + "_" + new Date().getTime(),
                instrument,
                side,
                CatchTheChaos.ORDER_AMOUNT,
                0,
                CatchTheChaos.SLIPPAGE,
                sl,
                tp
        );

        System.out.println("Submit Order: " + order.getOrderCommand() + ", type: " + performType + ", price: " + order.getOpenPrice());
    }

    public void closeAllOrders() throws JFException {
        for (IOrder order: engine.getOrders()) {
            order.close();
        }
    }

    private double calculateSL(Instrument instrument, OrderCommand side, double slPipsMax) throws JFException {
        ITick lastTick = history.getLastTick(instrument);
        double slPipsValue = slPipsMax;
        int slSideVector = 1;

        if (side.equals(OrderCommand.BUY)) {
            slSideVector = -1;

            ChFractal downFractal = chFractalMan.getLastDownFractal();

            if (downFractal != null) {
                double diffDown = (lastTick.getBid() - downFractal.getPrice()) / instrument.getPipValue();

                if (diffDown > 0 && diffDown < slPipsMax && slPipsMax > CatchTheChaos.SL_AWE_PIPS_MAX) {
                    slPipsValue = diffDown;
                }
            }
        } else {
            ChFractal upFractal = chFractalMan.getLastUpFractal();

            if (upFractal != null) {
                double diffUp = (upFractal.getPrice() - lastTick.getBid()) / instrument.getPipValue();

                if (diffUp > 0 && diffUp < slPipsMax && slPipsMax > CatchTheChaos.SL_AWE_PIPS_MAX) {
                    slPipsValue = diffUp;
                }
            }
        }

        return lastTick.getBid() + (slPipsValue * instrument.getPipValue() * slSideVector);
    }
}
