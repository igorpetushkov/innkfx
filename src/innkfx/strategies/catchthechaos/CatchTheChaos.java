package innkfx.strategies.catchthechaos;

import com.dukascopy.api.*;
import com.dukascopy.api.feed.IFeedDescriptor;
import com.dukascopy.api.system.ITesterReportData;
import innkfx.dev.IST;

import innkfx.strategies.catchthechaos.common.ChPerformsType;

import innkfx.strategies.catchthechaos.alligator.ChAlligatorMan;
import innkfx.strategies.catchthechaos.fractal.ChFractalMan;
import innkfx.strategies.catchthechaos.awesome.ChAwesomeMan;
import innkfx.strategies.catchthechaos.order.ChOrderSignalMan;

import innkfx.strategies.catchthechaos.patterns.ChFractalPerforms;
import innkfx.strategies.catchthechaos.patterns.ChAwesomePerforms;
import innkfx.strategies.catchthechaos.patterns.ChOrderSignalPerforms;

import innkfx.strategies.catchthechaos.engine.ChOrderEngine;
import innkfx.strategies.catchthechaos.live.ChOrderLive;

import innkfx.utils.DateUtils;
import org.slf4j.Logger;

import java.util.Date;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CatchTheChaos implements IStrategy, IST  {
    public final int INITIAL_DEPOSIT = 1000;
    public Instrument[] INSTRUMENTS = {
        Instrument.EURUSD
    };

    // #1 +AWE/+AWE/+AWE/+FRA/-SIG/+FRA >> 63.2
//    public static String DATE_FROM = "05/02/2018 00:00:00";
//    public static String DATE_TO = "05/02/2018 23:00:00";

    // #2 -FRA/+SIG/+AWE/+FRA/+SIG/+FRA/+FRA >> 122.2
//    public static String DATE_FROM = "07/02/2018 00:00:00";
//    public static String DATE_TO = "07/02/2018 23:00:00";

    // #3 AWE+4 >> 7.5
//    public static String DATE_FROM = "08/02/2018 00:00:00";
//    public static String DATE_TO = "08/02/2018 23:00:00";

    // #4
    public static String DATE_FROM = "12/02/2018 00:00:00";
    public static String DATE_TO = "23/02/2018 23:00:00";

//    public static String DATE_FROM = "19/02/2018 00:00:00";
//    public static String DATE_TO = "19/02/2018 23:00:00";


    // #5
//    public static String DATE_FROM = "12/02/2018 10:00:00";
//    public static String DATE_TO = "12/02/2018 16:00:00";


    // TODO: 1 close order before night!!! and on sleep time
    // TODO: 2 mark manual operation with order
    // TODO: 3 only one order per instrument, if awe then create signal then create one more signal!
    // TODO: 4 check alligator prices for awe and signals?
    // TOOD: 5 awe max sl pips = 10 ?

    @Configurable("SL_FRA_PIPS_MAX")
    public static int SL_FRA_PIPS_MAX = 40;
    @Configurable("SL_AWE_PIPS_MAX")
    public static int SL_AWE_PIPS_MAX = 15;
    @Configurable("SL_SIG_PIPS_MAX")
    public static int SL_SIG_PIPS_MAX = 15;

    @Configurable("TP_AWE_PIPS_MIN")
    public static int TP_AWE_PIPS_MIN = 4;

    @Configurable("ORDER_AMOUNT")
    public static double ORDER_AMOUNT = 0.001;
    @Configurable("TRAILING_STEP")
    public static double TRAILING_STEP = 10;
    @Configurable("SLIPPAGE")
    public static double SLIPPAGE = 0;
    @Configurable("SHOW_MARK")
    public static boolean SHOW_MARK = true;

    @Configurable("AWESOME_PERFORMS")
    public static boolean AWESOME_PERFORMS = true;
    @Configurable("FRACTAL_PERFORMS")
    public static boolean FRACTAL_PERFORMS = true;
    @Configurable("SIGNAL_PERFORMS")
    public static boolean SIGNAL_PERFORMS = true;

    @Configurable("FRACTAL_OVERLAPPING")
    public static int FRACTAL_OVERLAPPING = 1;
    @Configurable("SIGNAL_GAP")
    public static int SIGNAL_GAP = 5;


    @Configurable("SLEEP_TIME_FROM")
    public static int SLEEP_TIME_FROM = 21;
    @Configurable("SLEEP_TIME_TO")
    public static int SLEEP_TIME_TO = 2;

    @Configurable("SLEEP_TIME_AWE_FROM")
    public static int SLEEP_TIME_AWE_FROM = 19;
    @Configurable("SLEEP_TIME_AWE_TO")
    public static int SLEEP_TIME_AWE_TO = 6;

    @Configurable("SLEEP_CLOSE_TIME")
    public static int SLEEP_CLOSE_TIME = 22;

    private Period CHAOS_PERIOD = Period.THIRTY_MINS;

    private Logger LOGGER;

    private IContext context;
    private IEngine engine;
    private IIndicators indicators;
    private IConsole console;
    private IHistory history;


    // TODO: - zero +
    // TODO: - + + but small amount (it's not ok)

    private ChFractalMan chFractalMan;
    private ChAlligatorMan chAlligatorMan;
    private ChAwesomeMan chAwesomeMan;
    private ChOrderSignalMan chOrderSignalMan;

    private ChFractalPerforms chFractalPerforms;
    private ChAwesomePerforms chAwesomePerforms;
    private ChOrderSignalPerforms chOrderSignalPerforms;

    private ChOrderEngine chOrderEngine;
    private ChOrderLive chOrderLive;

    public CatchTheChaos(Logger LOGGER) {
        this.LOGGER = LOGGER;
    }

    public ChFractalMan getChFractalMan() {
        return chFractalMan;
    }

    public ChAlligatorMan getChAlligatorMan() {
        return chAlligatorMan;
    }

    public ChAwesomeMan getChAwesomeMan() {
        return chAwesomeMan;
    }

    public ChOrderSignalMan getChOrderSignalMan() {
        return chOrderSignalMan;
    }

    public void onStart(IContext context) throws JFException {
        this.context = context;

        engine = context.getEngine();
        indicators = context.getIndicators();
        history = context.getHistory();
        console = context.getConsole();

        buildChaos();
        buildPatterns();

        initChaos();
//        initPatterns();

        console.getOut().println("Started");
    }

    private void buildChaos() {
        for (Instrument instrument: INSTRUMENTS) {
            IChart chart = context.getChart(instrument);
            IFeedDescriptor fd = chart.getFeedDescriptor();
            fd.setPeriod(CHAOS_PERIOD);
            chart.setFeedDescriptor(fd);
        }

        chFractalMan = new ChFractalMan(context, INSTRUMENTS, CHAOS_PERIOD);
        chAlligatorMan = new ChAlligatorMan(context, INSTRUMENTS, CHAOS_PERIOD);
        chAwesomeMan = new ChAwesomeMan(context, INSTRUMENTS, CHAOS_PERIOD);
        chOrderSignalMan = new ChOrderSignalMan(context, INSTRUMENTS, CHAOS_PERIOD);

        chOrderEngine = new ChOrderEngine(context, chFractalMan);
        chOrderLive = new ChOrderLive(this, context);
    }

    private void buildPatterns() {
        chFractalPerforms = new ChFractalPerforms(this, context);
        chAwesomePerforms = new ChAwesomePerforms(this, context);
        chOrderSignalPerforms = new ChOrderSignalPerforms(this);
    }

    private void initChaos() {
        chFractalMan.init();
        chAlligatorMan.init();
        chAwesomeMan.init();
    }

//    private void initPatterns() {
//        chFractalPerforms.init(context);
//        chAwesomePerforms.init(context);
//    }

    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
        if (isSleepTime(bidBar.getTime())) {
            return;
        }

        if (period.equals(CHAOS_PERIOD)) {
            chFractalMan.onBar(instrument, bidBar);
            chAlligatorMan.onBar(instrument, bidBar);
            chAwesomeMan.onBar(instrument, bidBar);
        }

        if (period.equals(CHAOS_PERIOD)) {
            if (AWESOME_PERFORMS) {
                chAwesomePerforms.onBar(instrument, bidBar);
            }
        }
    }

    public void onTick(Instrument instrument, ITick tick) throws JFException {
        if (isSleepCloseTime(tick.getTime())) {
            chOrderEngine.closeAllOrders();

            return;
        }

        if (isSleepTime(tick.getTime())) {
            return;
        }

        if (FRACTAL_PERFORMS) {
            chFractalPerforms.onTick(instrument, tick);
        }

        if (SIGNAL_PERFORMS) {
            chOrderSignalPerforms.onTick(instrument, tick);
        }

        chOrderLive.onTick(instrument, tick);
    }

    public void submitOrder(Instrument instrument, ChPerformsType performType, double price) throws JFException {
        chOrderEngine.submitOrder(instrument, performType, price);
    }

    public void submitOrder(Instrument instrument, ChPerformsType performType) throws JFException {
        submitOrder(instrument, performType, 0);
    }

    public void onMessage(IMessage message) throws JFException {
        if (message.toString().startsWith("ORDER_")) {
            chOrderLive.onMessage(message);
        }
    }

    public void onAccount(IAccount account) throws JFException {
    }

    public void onStop() throws JFException {
//        chFractalPerforms.onStop();
//        chAwesomePerforms.onStop();

        console.getOut().println("Stopped");
    }

    public boolean isSleepTime(long timestamp) {
        int tickHour = new Integer(DateUtils.formatToGMT0(timestamp).substring(6, 8));

        return tickHour >= SLEEP_TIME_FROM || tickHour < SLEEP_TIME_TO;
    }

    private boolean isSleepCloseTime(long timestamp) {
        int tickHour = new Integer(DateUtils.formatToGMT0(timestamp).substring(6, 8));

        return tickHour >= SLEEP_CLOSE_TIME;
    }

    public LoadingProgressListener getLoadingProgressListener() {
        return new LoadingProgressListener() {
            @Override
            public void dataLoaded(long startTime, long endTime, long currentTime, String information) {
                LOGGER.info(information);
            }

            @Override
            public void loadingFinished(boolean allDataLoaded, long startTime, long endTime, long currentTime) {
                if (allDataLoaded) {
                    LOGGER.info("startTime: " + new Date(startTime) + " - " + "currentTime: " + new Date(currentTime));
                }
            }

            @Override
            public boolean stopJob() {
                return false;
            }
        };
    }

    public void doReport(ITesterReportData report){
//                    List<IOrder> openOrders = report.getOpenOrders();
        List<IOrder> closedOrders = report.getClosedOrders();

        double pips = 0;
        double comms = 0;
        for(IOrder order: closedOrders) {
            String closeTime = DateUtils.formatToGMT0(order.getCloseTime());
            String fillTime = DateUtils.formatToGMT0(order.getFillTime());
            String instr = order.getInstrument() + "";
            String openPrice = order.getOpenPrice() + "";
            String closePrice = order.getClosePrice() + "";

            String label = order.getLabel();

            if (label.startsWith("CTC_AWE")) {
                label = "AWE";
            } else if (label.startsWith("CTC_FRA")) {
                label = "FRA";
            } else if (label.startsWith("CTC_SIG")) {
                label = "SIG";
            }

            double pipsss = order.getProfitLossInPips();
            pips += pipsss;
            String plPips = pipsss + "";

            double ee = order.getProfitLossInUSD() / order.getProfitLossInPips();
            comms += (order.getCommission() / ee);
            String comm = (order.getCommission() / ee) + "";

            LOGGER.info(
                    "[" +instr + ":" + order.getOrderCommand() + ":" + label + "] " +
                    fillTime + " - " + closeTime + " .. " +
                    openPrice + " - " + closePrice + " >> " + plPips + " (-" + comm + ")"
            );
        }
        LOGGER.info("###### PIPS = " +  String.format("%.1f", pips - comms) + " [" + String.format("%.1f", pips) +  " - " + String.format("%.2f", comms) +"]");
        LOGGER.info("- - - - -");

        try {
            List<IOrder> openOrders = engine.getOrders();

            LOGGER.info("###### OPEN ORDERS = " + openOrders.size());
            for (IOrder order: openOrders) {
                String openPrice = order.getOpenPrice() + "";

                String label = order.getLabel();
                String instr = order.getInstrument() + "";
                String fillTime = DateUtils.formatToGMT0(order.getFillTime());

                if (label.startsWith("CTC_AWE")) {
                    label = "AWE";
                } else if (label.startsWith("CTC_FRA")) {
                    label = "FRA";
                } else if (label.startsWith("CTC_SIG")) {
                    label = "SIG";
                }

                LOGGER.info(
                        "[" +instr + ":" + order.getOrderCommand() + ":" + label + "] " +
                        "from " + openPrice + " / " + fillTime + " >> " + String.format("%.1f", order.getProfitLossInPips())
                );
            }
        } catch (JFException e) {
            // error here
        }
    }
}