package org.jon.ivmark.worldcup.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.jon.ivmark.worldcup.client.domain.*;
import org.jon.ivmark.worldcup.shared.GameResult;
import org.jon.ivmark.worldcup.shared.LoginInfo;
import org.jon.ivmark.worldcup.shared.PlaysDto;
import org.jon.ivmark.worldcup.shared.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class WebApp implements EntryPoint {

    private static final int ONE_INDEX = 0;
    private static final int X_INDEX = 1;
    private static final int TWO_INDEX = 2;

    private LoginInfo loginInfo = null;

    private VerticalPanel userPanel = new VerticalPanel();
    private Label userLabel = new Label();

    private Anchor signOutLink = new Anchor("Logga ut");
    private final List<Label> numRowsLabels = new ArrayList<>(Round.NUM_ROUNDS);

    private final List<Button> saveButtons = new ArrayList<>(Round.NUM_ROUNDS);
    private List<Round> rounds;
    private TextBox teamTextBox = new TextBox();
    private Button saveSettingsButton = new Button("Spara");
    private VerticalPanel resultsPanel = new VerticalPanel();
    private List<Result> results;

    public WebApp() {
        this.rounds = new ArrayList<>(Round.NUM_ROUNDS);
        for (int i = 0; i < Round.NUM_ROUNDS; i++) {
            rounds.add(new Round(i));
        }

        saveSettingsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                saveSettings();
            }
        });
        teamTextBox.addKeyUpHandler(new KeyUpHandler() {

            @Override
            public void onKeyUp(KeyUpEvent event) {
                // TODO: Use Pattern
                saveSettingsButton.setEnabled(teamTextBox.getText().trim().length() > 1);
            }
        });
        teamTextBox.setMaxLength(50);
    }

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        // Check login status using login service.
        LoginServiceAsync loginService = GWT.create(LoginService.class);
        loginService.login(GWT.getHostPageBaseURL(), new AsyncCallback<LoginInfo>() {
            public void onFailure(Throwable error) {
            }

            public void onSuccess(LoginInfo result) {
                loginInfo = result;
                if (loginInfo.isLoggedIn()) {
                    loadTeam();
                    loadRounds();
                    loadResultPage();
                } else {
                    loadLogin();
                }
            }

        });
    }

    private void loadTeam() {
        PlayServiceAsync playService = GWT.create(PlayService.class);
        playService.getTeamName(new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(String teamName) {
                signOutLink.setHref(loginInfo.getLogoutUrl());
                userLabel.setText(getWelcomeText(teamName));
                teamTextBox.setText(teamName);
            }
        });
    }

    private void loadLogin() {
        Window.Location.assign(loginInfo.getLoginUrl());
    }

    private void loadWorldCupPage() {
        Grid mainGrid = new Grid(4, Round.NUM_ROUNDS);
        mainGrid.setStyleName("mainGrid");

        HTMLTable.CellFormatter cellFormatter = mainGrid.getCellFormatter();

        for (int col = 0; col < Round.NUM_ROUNDS; col++) {
            mainGrid.setText(0, col, "Omgång " + (col + 1));
            cellFormatter.setStyleName(0, col, "tableHeading");
        }

        Games games = Games.allGames();

        for (int roundIndex = 0; roundIndex < Round.NUM_ROUNDS; roundIndex++) {
            Grid grid = new Grid(Round.NUM_GAMES + 1, 4);
            grid.setStyleName("roundTable");

            grid.setText(0, 0, "");
            grid.setText(0, 1, "1");
            grid.setText(0, 2, "X");
            grid.setText(0, 3, "2");

            Round round = getRound(roundIndex);

            for (int gameIndex = 0; gameIndex < Round.NUM_GAMES; gameIndex++) {
                GameId gameId = new GameId(roundIndex, gameIndex);
                Game game = games.get(gameId);
                Label gameLabel = new Label(game.label());
                int row = gameIndex + 1;
                grid.setWidget(row, 0, gameLabel);

                Play play = round.getPlay(gameIndex);

                CheckBox checkBoxOne = createCheckBox(roundIndex, gameIndex, ONE_INDEX, play.isOneChecked());
                grid.setWidget(row, 1, checkBoxOne);

                CheckBox checkBoxX = createCheckBox(roundIndex, gameIndex, X_INDEX, play.isXChecked());
                grid.setWidget(row, 2, checkBoxX);

                CheckBox checkBoxTwo = createCheckBox(roundIndex, gameIndex, TWO_INDEX, play.isTwoChecked());
                grid.setWidget(row, 3, checkBoxTwo);
            }

            HTMLTable.CellFormatter cellFormatter1X2 = grid.getCellFormatter();
            for (int r = 0; r < 17; r++) {
                for (int c = 1; c < 4; c++) {
                    cellFormatter1X2.setHorizontalAlignment(r, c, HasHorizontalAlignment.ALIGN_CENTER);
                }
            }

            Label label = new Label(round.numRowsText());
            mainGrid.setWidget(2, roundIndex, label);

            final Button saveButton = new Button("Spara");
            saveButton.setEnabled(false);
            final int finalRoundNumber = roundIndex;
            saveButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    saveButton.setText("Sparar...");
                    saveRound(saveButton, finalRoundNumber);
                }
            });
            mainGrid.setWidget(3, roundIndex, saveButton);

            numRowsLabels.add(label);
            saveButtons.add(saveButton);

            mainGrid.setWidget(1, roundIndex, grid);
        }

        DockLayoutPanel mainPanel = new DockLayoutPanel(Style.Unit.EM);

        TabLayoutPanel tabs = new TabLayoutPanel(1.5, Style.Unit.EM);
        tabs.add(mainGrid, "Dina spel");

        tabs.add(resultsPanel, "Resultat");
        tabs.add(new Label("Inte tillgänglig ännu."), "Topplista");
        tabs.add(new Label("Inte tillgängliga ännu."), "Alla spel");

        HorizontalPanel settingsPanel = new HorizontalPanel();
        settingsPanel.add(new Label("Lagnamn:"));
        settingsPanel.add(teamTextBox);
        settingsPanel.add(saveSettingsButton);
        settingsPanel.setStyleName("settingsPanel");
        tabs.add(settingsPanel, "Inställningar");

        tabs.add(new HTML(Rules.rulesHtml()), "Regler");

        DockLayoutPanel header = new DockLayoutPanel(Style.Unit.EM);

        userPanel.add(userLabel);
        userPanel.add(signOutLink);
        header.addEast(userPanel, 20);
        mainPanel.addNorth(header, 4);
        mainPanel.add(tabs);

        RootLayoutPanel.get().add(mainPanel);
    }

    private void loadResultPage() {
        ResultsServiceAsync resultsService = GWT.create(ResultsService.class);
        resultsService.loadResults(new AsyncCallback<List<Result>>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(List<Result> results) {
                WebApp.this.results = results;
                renderResultsPanel();
            }
        });
    }

    private void renderResultsPanel() {
        Grid main = new Grid(2, 3);

        HTMLTable.CellFormatter cellFormatter = main.getCellFormatter();

        for (int col = 0; col < Round.NUM_ROUNDS; col++) {
            main.setText(0, col, "Omgång " + (col + 1));
            cellFormatter.setStyleName(0, col, "tableHeading");
        }

        Games games = Games.allGames();
        for (final Result result : results) {

            Grid resultGrid = new Grid(Round.NUM_GAMES + 1, 4);

            resultGrid.setText(0, 1, "1");
            resultGrid.setText(0, 2, "X");
            resultGrid.setText(0, 3, "2");

            resultGrid.setStyleName("resultsGrid");
            int gameIndex = 0;
            for (GameResult gameResult : result.getResults()) {
                Game game = games.get(new GameId(result.getRoundIndex(), gameIndex));
                int row = gameIndex + 1;
                resultGrid.setText(row, 0, game.label());
                for (int i = 0; i < 3; i++) {
                    RadioButton radioButton = createRadioButton(result, gameIndex, gameResult, i);
                    radioButton.setEnabled(loginInfo.isAdmin());
                    resultGrid.setWidget(row, i + 1, radioButton);
                }
                gameIndex++;

                HTMLTable.CellFormatter cellFormatter1X2 = resultGrid.getCellFormatter();
                for (int r = 0; r < 17; r++) {
                    for (int c = 1; c < 4; c++) {
                        cellFormatter1X2.setHorizontalAlignment(r, c, HasHorizontalAlignment.ALIGN_CENTER);
                    }
                }
            }

            main.setWidget(1, result.getRoundIndex(), resultGrid);
        }
        resultsPanel.setStyleName("resultsPanel");
        resultsPanel.add(main);
        if (loginInfo.isAdmin()) {
        Button saveButton = new Button("Spara");
        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                saveResults();
            }
        });
        resultsPanel.add(saveButton);
        }
    }

    private void saveResults() {
        ResultsServiceAsync resultsService = GWT.create(ResultsService.class);
        for (Result result : results) {
            resultsService.saveResult(result, new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                }

                @Override
                public void onSuccess(Void result) {
                }
            });
        }
    }

    private RadioButton createRadioButton(final Result result, final int gameIndex, final GameResult gameResult,
                                          final int index) {
        final RadioButton radioButton = new RadioButton("rb-" + result.getRoundIndex() + "-" + gameIndex);
        if (index == gameResult.intValue()) {
            radioButton.setValue(true);
        }
        radioButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (radioButton.getValue()) {
                    result.setResult(gameIndex, GameResult.fromInt(index));
                } else {
                    result.setResult(gameIndex, GameResult.UNKNOWN);
                }
            }
        });
        return radioButton;
    }

    private void saveSettings() {
        PlayServiceAsync playService = GWT.create(PlayService.class);
        final String name = teamTextBox.getText();
        playService.setTeamName(name, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(Void result) {
                userLabel.setText(getWelcomeText(name));
                saveSettingsButton.setEnabled(false);
            }
        });
    }

    private String getWelcomeText(String teamName) {
        return "Välkommen " + teamName;
    }

    private void loadRounds() {
        PlayServiceAsync playService = GWT.create(PlayService.class);
        playService.loadPlays(new AsyncCallback<List<PlaysDto>>() {
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(List<PlaysDto> result) {
                rounds = new ArrayList<>(result.size());
                for (PlaysDto playsDto : result) {
                    Round round = Round.fromPlaysDto(playsDto);
                    rounds.add(round);
                }
                loadWorldCupPage();
            }
        });

    }

    private void saveRound(final Button button, int roundIndex) {
        PlayServiceAsync playService = GWT.create(PlayService.class);
        PlaysDto plays = getRound(roundIndex).asDto();
        playService.savePlay(plays, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                RootPanel.get().add(new Label("Ooops, något gick åt skogen"));
            }

            @Override
            public void onSuccess(Void result) {
                button.setText("Spara");
                button.setEnabled(false);
            }
        });
    }

    private Round getRound(int roundIndex) {
        return rounds.get(roundIndex);
    }

    private CheckBox createCheckBox(final int round, final int game, final int signIndex, boolean check) {
        CheckBox checkBox = new CheckBox();
        checkBox.setValue(check);
        checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                checkBoxStateChanged(round, game, signIndex, event.getValue());
            }
        });
        return checkBox;
    }

    private void checkBoxStateChanged(int roundNumber, int gameNumber, int signIndex, boolean isChecked) {
        Round round = getRound(roundNumber);

        Play play = getGame(gameNumber, round);
        switch (signIndex) {
            case ONE_INDEX:
                play.setOne(isChecked);
                break;
            case X_INDEX:
                play.setX(isChecked);
                break;
            case TWO_INDEX:
                play.setTwo(isChecked);
                break;
        }

        Label label = numRowsLabels.get(roundNumber);
        label.setText(round.numRowsText());
        if (round.tooManyRows()) {
            label.setStyleName("invalid");
        } else {
            label.removeStyleName("invalid");
        }
        Button saveButton = saveButtons.get(roundNumber);
        saveButton.setEnabled(round.isValid());
    }

    private Play getGame(int gameNumber, Round round) {
        return round.getPlay(gameNumber);
    }

}
