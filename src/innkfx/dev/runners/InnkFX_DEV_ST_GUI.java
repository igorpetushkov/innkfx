package innkfx.dev.runners;

import com.dukascopy.api.*;
import com.dukascopy.api.system.*;
import com.dukascopy.api.system.tester.ITesterExecution;
import com.dukascopy.api.system.tester.ITesterExecutionControl;
import com.dukascopy.api.system.tester.ITesterGui;
import com.dukascopy.api.system.tester.ITesterUserInterface;
import innkfx.strategies.catchthechaos.CatchTheChaos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Future;

import com.dukascopy.api.system.ITesterClient.*;

@SuppressWarnings("serial")
public class InnkFX_DEV_ST_GUI extends JFrame implements ITesterUserInterface, ITesterExecution {
    private static final Logger LOGGER = LoggerFactory.getLogger(InnkFX_DEV_ST_GUI.class);

    private final int frameWidth = 600;
    private final int frameHeight = 600;
    private final int controlPanelHeight = 40;
    
    private JPanel currentChartPanel = null;
    private ITesterExecutionControl executionControl = null;
    
    private JPanel controlPanel = null;
    private JButton startStrategyButton = null;
    private JButton pauseButton = null;
    private JButton continueButton = null;
    private JButton cancelButton = null;
    
    private static String jnlpUrl = "https://www.dukascopy.com/client/demo/jclient/jforex.jnlp";
    private static String userName = "***";
    private static String password = "***";

    private CatchTheChaos ST;
    
    InnkFX_DEV_ST_GUI(){
        ST = new CatchTheChaos(LOGGER);

    	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
    }
    
    @Override
	public void setChartPanels(Map<IChart, ITesterGui> chartPanels) {
		if(chartPanels != null && chartPanels.size() > 0){
			
			IChart chart = chartPanels.keySet().iterator().next();
			Instrument instrument = chart.getInstrument();
			setTitle(instrument.toString() + " " + chart.getSelectedOfferSide() + " " + chart.getSelectedPeriod());
			
			JPanel chartPanel = chartPanels.get(chart).getChartPanel();
			addChartPanel(chartPanel);
		}
	}

	@Override
	public void setExecutionControl(ITesterExecutionControl executionControl) {
		this.executionControl = executionControl;
	}

    public void startStrategy() throws Exception {
        final ITesterClient client = TesterFactory.getDefaultInstance();
        setDates(client);

        client.setSystemListener(new ISystemListener() {
            @Override
            public void onStart(long processId) {
                LOGGER.info("Strategy started: " + processId);
            }

            @Override
            public void onStop(long processId) {
                LOGGER.info("Strategy stopped: " + processId);

                try {
                    ST.doReport(client.getReportData(processId));
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
                if (client.getStartedStrategies().size() == 0) {
                    //Do nothing
                }
            }

            @Override
            public void onConnect() {
                LOGGER.info("Connected");
            }

            @Override
            public void onDisconnect() {}
        });

        connect(client);


        client.setSubscribedInstruments(new HashSet<>(Arrays.asList(ST.INSTRUMENTS)));
        client.setInitialDeposit(Instrument.EURUSD.getSecondaryJFCurrency(), ST.INITIAL_DEPOSIT);

        LOGGER.info("Downloading data");
        Future<?> future = client.downloadData(null);
        future.get();
        LOGGER.info("Starting strategy");
        Thread.sleep(5000);

        client.startStrategy(ST, ST.getLoadingProgressListener(), this, this);
    }

    public void setDates(ITesterClient client) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT 0"));
//        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+3:00"));

        try {
            Date dateFrom = dateFormat.parse(ST.DATE_FROM);
            Date dateTo = dateFormat.parse(ST.DATE_TO);

            client.setDataInterval(DataLoadingMethod.ALL_TICKS, dateFrom.getTime(), dateTo.getTime());
        } catch (ParseException e) {
            System.out.println("setDates failed");
        }
    }

    public void connect(IClient client) throws Exception {
        client.connect(jnlpUrl, userName, password);

        int i = 10;
        while (i > 0 && !client.isConnected()) {
            Thread.sleep(1000);
            i--;
        }
        if (!client.isConnected()) {
            System.err.println("Failed to connect Dukascopy servers");
            System.exit(1);
        }
    }

	private void addChartPanel(JPanel chartPanel){
		removeCurrentChartPanel();
		
		this.currentChartPanel = chartPanel;
		chartPanel.setPreferredSize(new Dimension(frameWidth, frameHeight - controlPanelHeight));
		chartPanel.setMinimumSize(new Dimension(frameWidth, 500));
		chartPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		getContentPane().add(chartPanel);
		this.validate();
		chartPanel.repaint();
	}

    private void removeCurrentChartPanel(){
        if(this.currentChartPanel != null){
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        InnkFX_DEV_ST_GUI.this.getContentPane().remove(InnkFX_DEV_ST_GUI.this.currentChartPanel);
                        InnkFX_DEV_ST_GUI.this.getContentPane().repaint();
                    }
                });
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public static void main(String[] args) throws Exception {
    	InnkFX_DEV_ST_GUI testerMainGUI = new InnkFX_DEV_ST_GUI();
    	testerMainGUI.showChartFrame();
    }

	public void showChartFrame() throws Exception {
		setSize(frameWidth, frameHeight);
		centerFrame();
		setVisible(true);
        startStrategy();
	}

	private void centerFrame(){
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenSize = tk.getScreenSize();
        int screenHeight = screenSize.height;
        int screenWidth = screenSize.width;
        setSize((int)(screenWidth / 1.3), (int)(screenHeight / 1.3));
        setLocation(100, 100);
	}
}