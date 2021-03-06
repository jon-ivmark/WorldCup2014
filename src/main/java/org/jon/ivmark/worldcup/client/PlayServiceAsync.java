package org.jon.ivmark.worldcup.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.jon.ivmark.worldcup.shared.LoginInfo;
import org.jon.ivmark.worldcup.shared.PlaysDto;
import org.jon.ivmark.worldcup.shared.SimilarityMatrix;

import java.util.List;
import java.util.Map;

public interface PlayServiceAsync {

    void savePlay(PlaysDto plays, AsyncCallback<Void> callback);

    void loadPlays(AsyncCallback<List<PlaysDto>> async);

    void getTeamName(AsyncCallback<String> async);

    void setTeamName(String teamName, AsyncCallback<Void> async);

    void loadAllCompletePlays(AsyncCallback<Map<String, List<PlaysDto>>> async);

    void loadSimilarities(AsyncCallback<SimilarityMatrix> async);
}
