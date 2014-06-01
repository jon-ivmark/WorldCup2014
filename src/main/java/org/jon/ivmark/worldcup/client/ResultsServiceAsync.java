package org.jon.ivmark.worldcup.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jon.ivmark.worldcup.shared.Result;

import java.util.List;

public interface ResultsServiceAsync {
    void loadResults(AsyncCallback<List<Result>> callback);

    void saveResult(Result result, AsyncCallback<Void> callback);
}
