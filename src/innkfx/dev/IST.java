package innkfx.dev;

import com.dukascopy.api.LoadingProgressListener;
import com.dukascopy.api.system.ITesterReportData;

public interface IST {
    LoadingProgressListener getLoadingProgressListener();
    void doReport(ITesterReportData report);
}
