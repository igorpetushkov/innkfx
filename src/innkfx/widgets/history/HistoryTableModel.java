package innkfx.widgets.history;

import com.dukascopy.api.*;
import com.google.common.collect.Maps;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.stream.Stream;
import javax.swing.table.TableColumn;
import java.util.stream.*;
import java.text.DecimalFormat;

import innkfx.utils.DateUtils;

@SuppressWarnings("serial")
class HistoryTableModel extends AbstractTableModel {
    class HistoryTableColumn extends TableColumn {
        private String name;
        private boolean visible;
        private Function<IOrder, ?> valueFunc;

        HistoryTableColumn(String name, Function<IOrder, ?> valueFunc, boolean visible) {
            this.name = name;
            this.visible = visible;
            this.valueFunc = valueFunc;
        }

        String getName() {
            return name;
        }

        String getValue(IOrder order) {
            Object value = valueFunc.apply(order);

            return value != null ? value.toString() : "";
        }

        void toggleVisible() {
            this.visible = !this.visible;
        }

        void setVisible(boolean visible) {
            this.visible = visible;
        }

        boolean isVisible() {
            return this.visible;
        }
    }

    class HistoryTableData {
        private List<IOrder> orders;
        private List<IOrder> buffer;

        private Predicate<IOrder> filterInstrument;

        HistoryTableData() {
            orders = new ArrayList<>();
            buffer = new ArrayList<>();
        }

        void update(List<IOrder> orders) {
            this.orders = orders;

            updateBuffer();
        }

        IOrder getOrder(int index) {
            return buffer.get(index);
        }

        List<IOrder> getOrders() {
            return buffer;
        }


        int size() {
            return buffer.size();
        }

        void setInstrumentFilter(String instrumentBy) {
            this.filterInstrument = instrumentBy != null ? order -> order.getInstrument().equals(instrumentBy) : null;

            updateBuffer();
        }

        private void updateBuffer() {
            Stream<IOrder> buffer = orders.stream();

            if (filterInstrument != null) {
                buffer = buffer.filter(filterInstrument);
            }

            this.buffer = buffer.collect(Collectors.toList());
        }
    }


    private HistoryTableData data;
    private ArrayList<HistoryTableColumn> columns;

    HistoryTableModel(){
        data = new HistoryTableData();
    }

    public void init() {
        columns = new ArrayList<>(Arrays.asList(
            new HistoryTableColumn("Close Time", order -> DateUtils.formatToGMT3(order.getCloseTime()), true),
            new HistoryTableColumn("Fill Time", order -> DateUtils.formatToGMT3(order.getFillTime()), false),
            new HistoryTableColumn("Pos. ID", IOrder::getId, false),
            new HistoryTableColumn("Instrument", IOrder::getInstrument, true),
            new HistoryTableColumn("Side", IOrder::getOrderCommand, true),
            new HistoryTableColumn("Amount", IOrder::getAmount, false),
            new HistoryTableColumn("Open Price", IOrder::getOpenPrice, true),
            new HistoryTableColumn("Close Price", order -> {
                double tp = order.getTakeProfitPrice();
                double sl = order.getStopLossPrice();
                double cp = order.getClosePrice();

                if (tp > 0) {
                    return tp + " [tp]";
                } else if (sl > 0) {
                    return sl + " [sl]";
                } else {
                    return cp;
                }
            }, true),
            new HistoryTableColumn("P/L pips", IOrder::getProfitLossInPips, true),
            new HistoryTableColumn("P/L", order -> order.getProfitLossInUSD() + " $", false),
            new HistoryTableColumn("Commission", order -> order.getCommission() + " c", false),
            new HistoryTableColumn("Comment", IOrder::getComment, false)
        ));
    }

    public void updateData(List<IOrder> orders) {
        data.update(orders);

        fireTableDataChanged();
    }

    public List<HistoryTableColumn> getColumns() {
        return columns;
    }

    public List<HistoryTableColumn> getVisibleColumns() {
        return columns.stream().filter(HistoryTableColumn::isVisible).collect(Collectors.toList());
    }

    public int getRowCount() {
        return data.size();
    }

    public int getColumnCount() {
        return getVisibleColumns().size();
    }

    public String getColumnName(int columnIndex) {
        return getVisibleColumns().get(columnIndex).getName();
    }

    public Object getValueAt(int row, int columnIndex) {
        IOrder order = data.getOrder(row);
        HistoryTableColumn column = getVisibleColumns().get(columnIndex);

        if(column == null) {
            return null;
        }

        return column.getValue(order);
    }

    public void applyFilterByInstrument(String instrumentBy) {
        data.setInstrumentFilter(instrumentBy);
        fireTableDataChanged();
    }

    public double getHistoryProfitInUSD() {
        return data.getOrders().stream().mapToDouble(order -> {
            double pl = order.getProfitLossInUSD();

            return Double.isNaN(pl) ? 0 : pl;
        }).sum();
    }

    public double getHistoryProfitInPips() {
        return data.getOrders().stream().mapToDouble(IOrder::getProfitLossInPips).sum();
    }

    public double getHistoryCommission() {
        return data.getOrders().stream().mapToDouble(IOrder::getCommission).sum();
    }
}

