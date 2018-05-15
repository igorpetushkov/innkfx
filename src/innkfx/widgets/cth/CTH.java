//package innkfx.widgets.cth;
//
//import com.dukascopy.api.Instrument;
//import com.dukascopy.api.JFException;
//import com.dukascopy.api.plugins.IPluginContext;
//import com.dukascopy.api.plugins.ui.UIFactory;
//import com.dukascopy.api.plugins.widget.IPluginWidget;
//import com.dukascopy.api.plugins.widget.PluginWidgetListener;
//import innkfx.storages.orders.OrderHistoryStorage;
//import innkfx.utils.DateUtils;
//
//import javax.swing.*;
//import javax.swing.table.TableModel;
//import javax.swing.table.TableRowSorter;
//import java.awt.*;
//import java.util.Arrays;
//import java.util.function.Function;
//
//public class CTH {
//    public enum UI { SIMPLE, JF }
//
//    private IPluginContext context;
//    private IPluginWidget widget;
//    private UIFactory jf;
//
//    private JPopupMenu popup;
//
//    private OrderHistoryStorage storage;
//
//    private innkfx.widgets.history.HistoryTable.UI ui;
//
//    private Function<?, ?> updatePL;
//    private Function<?, ?> pipsCl;
//    private Function<?, ?> usdCl;
//    private Function<?, ?> commCl;
//
//    private long currentFromTime;
//    private long currentToTime;
//
//    private TableRowSorter<TableModel> sorter;
//
//    public CTH(IPluginContext context) {
//        this.context = context;
//
//        jf = context.getUIFactory();
//    }
//
//    public void init() throws JFException {
//        init(null);
//    }
//
//    public void init(UI ui) throws JFException {
//        this.ui = ui != null ? ui : innkfx.widgets.history.HistoryTable.UI.SIMPLE;
//
//        initModel();
//        createWidget();
//
//        updateTable(DateUtils.getTodayStartTime(), 0);
//        updatePL.apply(null);
//    }
//
//    public void close() throws JFException {
//        context.removeWidget(widget);
//    }
//
//    public void destroy() throws  JFException {
//        close();
//
//        widget.addPluginWidgetListener(new PluginWidgetListener(){
//            public void onWidgetClose(){
//                context.getConsole().getOut().println("InnkHistory closed!");
//                context.stop();
//            }
//        });
//    }
//
//    private void initModel() {
//        model.init();
//    }
//
//    private void createWidget() {
//        widget = context.addWidget("InnkHistory");
//
//        JPanel main = widget.getContentPanel();
//        main.setLayout(new BorderLayout());
//
//        JTable table = ui.equals(innkfx.widgets.history.HistoryTable.UI.JF) ? jf.createTable(model).getComponent() : new JTable(model);
//        table.setAutoCreateRowSorter(true);
//
//        sorter = new TableRowSorter<>(model);
//        table.setRowSorter(sorter);
//
//        updateSort();
//
//        createTableHeaderPopup(table);
//        createTableToolbars(main);
//
//        JScrollPane scroll = ui.equals(innkfx.widgets.history.HistoryTable.UI.JF) ? jf.createScrollPanel(table).getComponent() : new JScrollPane(table);
//        main.add(scroll, BorderLayout.CENTER);
//    }
//
//    private void updateSort() {
//        sorter.setSortKeys(Arrays.asList(
//                new RowSorter.SortKey(0, SortOrder.DESCENDING)
//        ));
//        sorter.sort();
//    }
//
//    private void createTableToolbars(JPanel main){
//        JComboBox<String> instrumentSelect = ui.equals(innkfx.widgets.history.HistoryTable.UI.JF) ? jf.createComboBox(String.class).getComponent() : new JComboBox<>();
//        instrumentSelect.addItem("All");
//        for (String instrument: Instrument.toStringSet(context.getSubscribedInstruments())) {
//            instrumentSelect.addItem(instrument);
//        }
//
//        JComboBox<String> timesSelect = ui.equals(innkfx.widgets.history.HistoryTable.UI.JF) ? jf.createComboBox(String.class).getComponent() : new JComboBox<>();
//        for (String time: new String[]{ "Today", "Yesterday", "Week", "LastWeek", "Month" }) {
//            timesSelect.addItem(time);
//        }
//
//        JLabel countLb = ui.equals(innkfx.widgets.history.HistoryTable.UI.JF) ? jf.createLabel("0 ~").getComponent() : new JLabel("x ");
//        JComboBox<String> status = ui.equals(innkfx.widgets.history.HistoryTable.UI.JF) ? jf.createComboBox(String.class).getComponent() : new JComboBox<>();
//        status.addItem("0.00$");
//
//        updatePL = e -> {
//            status.removeAllItems();
//
//            double profit = model.getHistoryProfitInUSD();
//            double result = profit - model.getHistoryCommission();
//
//            status.addItem(String.format("%.1f", model.getHistoryProfitInPips()) + " p");
//            status.addItem(String.format("%.2f", result) + " $ / " + String.format("%.2f", profit) + " $");
//            status.addItem("-" + String.format("%.2f", model.getHistoryCommission()) + " c");
//
//            countLb.setText("x " + model.getRowCount());
//
//            status.setSelectedIndex(0);
//
//            return null;
//        };
//
//        status.addActionListener(e -> {
//            String option = (String) status.getSelectedItem();
//
//            if (option == null) {
//                return;
//            }
//
//            if (option.endsWith("c")) {
//                commCl.apply(null);
//            } else if (option.endsWith("$")) {
//                pipsCl.apply(null);
//                usdCl.apply(null);
//            } else {
//                pipsCl.apply(null);
//                usdCl.apply(null);
//            }
//
//            updateSort();
//        });
//
//        timesSelect.addActionListener(e -> {
//            String option = (String) timesSelect.getSelectedItem();
//
//            switch (option) {
//                case "Today":
//                    updateTable(DateUtils.getTodayStartTime(), 0);
//                    break;
//                case "Yesterday":
//                    updateTable(DateUtils.getYesterdayStartTime(), DateUtils.getTodayStartTime());
//                    break;
//                case "Week":
//                    updateTable(DateUtils.getWeekStartTime(), 0);
//                    break;
//                case "LastWeek":
//                    updateTable(DateUtils.getLastWeekStartTime(), DateUtils.getWeekStartTime());
//                    break;
//                case "Month":
//                    updateTable(DateUtils.getMonthStartTime(), 0);
//                    break;
//            }
//
//            updatePL.apply(null);
//        });
//
//        instrumentSelect.addActionListener(e -> {
//            String option = (String) instrumentSelect.getSelectedItem();
//            model.applyFilterByInstrument(option.equals("All") ? null : option);
//
//            updatePL.apply(null);
//        });
//
//        JButton btn = ui.equals(innkfx.widgets.history.HistoryTable.UI.JF) ? jf.createButton("R").getComponent() : new JButton("R");
//        btn.setBackground(Color.DARK_GRAY);
//        btn.addActionListener(e -> {
//            this.updateTable(currentFromTime, currentToTime);
//            updatePL.apply(null);
//            btn.setFocusable(false);
//        });
//
//        JPanel topToolbar = ui.equals(innkfx.widgets.history.HistoryTable.UI.JF) ? jf.createPanel().getComponent() : new JPanel();
//        topToolbar.setLayout(new FlowLayout(FlowLayout.LEFT));
//        topToolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
//
//        topToolbar.add(btn);
//        topToolbar.add(timesSelect);
//        topToolbar.add(instrumentSelect);
//        topToolbar.add(status);
//        topToolbar.add(countLb);
//
//        main.add(topToolbar, BorderLayout.NORTH);
//    }
//
//    public void updateTable(long fromTime, long toTime) {
//        try {
//            currentFromTime = fromTime;
//            currentToTime = toTime;
//            model.updateData(storage.getOrders(context.getSubscribedInstruments(), fromTime, toTime == 0 ? System.currentTimeMillis() : toTime));
//            widget.getContentPanel().validate();
//        } catch (JFException e) {
//            e.printStackTrace();
//        }
//    }
//}
//
//
//
//
