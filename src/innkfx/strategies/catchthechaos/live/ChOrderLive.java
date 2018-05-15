package innkfx.strategies.catchthechaos.live;

import com.dukascopy.api.*;

import com.dukascopy.api.drawings.ITextChartObject;
import innkfx.strategies.catchthechaos.CatchTheChaos;
import innkfx.strategies.catchthechaos.fractal.ChFractal;

import java.awt.*;

public class ChOrderLive {
    private CatchTheChaos ctc;
    private IContext context;
    private IEngine engine;
    private IHistory history;

    private ChOrderSL chOrderSL;

    private ITick lastTick;
    private double lastTickPrice = 0;

    private int sellSignalsCount = 0;
    private int buySignalsCount = 0;

    public ChOrderLive(CatchTheChaos ctc, IContext context) {
        this.ctc = ctc;

        this.context = context;
        this.engine = context.getEngine();
        this.history = context.getHistory();

        this.chOrderSL = new ChOrderSL();
    }

    public void onTick(Instrument instrument, ITick tick) throws JFException {
        double currTickPrice = tick.getBid();

        if (lastTickPrice == 0) {
            lastTickPrice = currTickPrice;
            return;
        }

        if (Math.abs(currTickPrice - lastTickPrice) / instrument.getPipValue() >= 0.5) {
            engine.getOrders(instrument).stream().forEach(order -> {
                if (order.getCloseTime() == 0) {
                    try {
                        double plPips = order.getProfitLossInPips();
                        double pipAmount = tick.getBidVolume();
                        System.out.println(plPips + " - " + pipAmount);

                        chOrderSL.updateOrderSL(order, tick);
                    } catch (JFException e) {
                        System.out.println("ERROR: ChOrderLive -> onTick and updateOrderSL");
                    }
                }
            });

            lastTick = tick;
        }

        lastTickPrice = currTickPrice;
    }

    public void onMessage(IMessage message) throws JFException {
        System.out.println(message.toString());
        System.out.println(
                "curr : " + history.getLastTick(message.getOrder().getInstrument()).getBid() +
                " open : " + message.getOrder().getOpenPrice() + " sl: " + message.getOrder().getStopLossPrice() +
                " pips: " + message.getOrder().getProfitLossInPips() + " - " + lastTick.getBidVolume()
        );
        System.out.println("reason: " + message.getReasons());

        IOrder order = message.getOrder();

        if (message.getType().equals(IMessage.Type.ORDER_CLOSE_OK) && order.getLabel().startsWith("CTC")) {
            Instrument instrument = message.getOrder().getInstrument();
            double plPips = order.getProfitLossInPips();

            if (CatchTheChaos.SHOW_MARK) {
                __markEnd(order);
            }

            if (ctc.isSleepTime(lastTick.getTime()) || order.getLabel().startsWith("CTC_SIG") || !CatchTheChaos.SIGNAL_PERFORMS) {
                return;
            }

            double gapPips = CatchTheChaos.SIGNAL_GAP * instrument.getPipValue() * (order.getOrderCommand().isLong() ? 1 : -1);
            double price = order.getClosePrice() + gapPips;

            if (plPips >= 5 && plPips <= 20) {
                if (order.getOrderCommand().isLong() && buySignalsCount < 2) {
                    ctc.getChOrderSignalMan().addOrderSignal(instrument, Period.ONE_SEC, order.getCloseTime(), ChFractal.Side.UP, price);

                    buySignalsCount += 1;
                    sellSignalsCount = 0;
                } else if (order.getOrderCommand().isShort() && sellSignalsCount < 2) {
                    ctc.getChOrderSignalMan().addOrderSignal(instrument, Period.ONE_SEC, order.getCloseTime(), ChFractal.Side.DOWN, price);

                    sellSignalsCount += 1;
                    buySignalsCount = 0;
                }
            } else if (plPips <= -10){
                if (order.getOrderCommand().isLong() && sellSignalsCount < 2) {
                    ctc.getChOrderSignalMan().addOrderSignal(instrument, Period.ONE_SEC, order.getCloseTime(), ChFractal.Side.DOWN, price);

                    sellSignalsCount += 1;
                    buySignalsCount = 0;
                } else if (order.getOrderCommand().isShort() && buySignalsCount < 2) {
                    ctc.getChOrderSignalMan().addOrderSignal(instrument, Period.ONE_SEC, order.getCloseTime(), ChFractal.Side.UP, price);

                    buySignalsCount += 1;
                    sellSignalsCount = 0;
                }
            }
        }
    }

    private void __markEnd(IOrder order) {
        IChart chart = context.getChart(order.getInstrument());

        ITextChartObject text = chart.getChartObjectFactory().createText("ttt-" + order.getCloseTime(), order.getCloseTime(), order.getClosePrice());
        text.setText("____", new Font(Font.DIALOG, Font.PLAIN, 25));
        text.setColor(order.getProfitLossInPips() > 0 ? Color.LIGHT_GRAY : Color.WHITE);
        chart.add(text);
    }

}
