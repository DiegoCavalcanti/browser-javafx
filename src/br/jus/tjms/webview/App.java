package br.jus.tjms.webview;


import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.html.HTMLInputElement;

/**
 * Demonstrates a WebView object accessing a web page.
 *
 * @see javafx.scene.web.WebView
 * @see javafx.scene.web.WebEngine
 */
public class App extends Application {

    public static final String EVENT_TYPE_CLICK = "click";
    public static final String EVENT_TYPE_MOUSEOVER = "mouseover";
    public static final String EVENT_TYPE_MOUSEOUT = "mouseclick";

    private String url = "http://localhost:8080/ebjur/publico/login.xhtml";
    private static final String COMPONENTE_VIEW = "componente-assinador";
    private static final String COMPONENTE_MESSAGE = "componente-mensagem";
    private WebView view;
    private Stage stage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        Pane root = new WebViewPane();
        primaryStage.setScene(new Scene(root, 1024, 768));
        primaryStage.show();
    }

    /**
     * Create a resizable WebView pane
     */
    public class WebViewPane extends Pane {

        public WebViewPane() {

            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
            };

// Install the all-trusting trust manager
            try {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (GeneralSecurityException e) {

            }
            VBox.setVgrow(this, Priority.ALWAYS);
            setMaxWidth(Double.MAX_VALUE);
            setMaxHeight(Double.MAX_VALUE);

            view = new WebView();

            view.setMinSize(500, 400);
            view.setPrefSize(500, 400);
            final WebEngine eng = view.getEngine();
            eng.load(url);
            final TextField locationField = new TextField(url);
            locationField.setMaxHeight(Double.MAX_VALUE);
            Button goButton = new Button("Go");
            goButton.setDefaultButton(true);
            EventHandler<ActionEvent> goAction = new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    eng.load(locationField.getText().startsWith("http://") ? locationField.getText() : "http://" + locationField.getText());
                    //eng.load(locationField.getText());
                    System.out.println(".handle()");

                }
            };
            goButton.setOnAction(goAction);
            locationField.setOnAction(goAction);
            eng.setJavaScriptEnabled(true);

            //onchange url
            eng.locationProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    locationField.setText(newValue);
                    System.out.println(".changed()");

                }
            });

                  final AtomicBoolean submitted = new AtomicBoolean();
            //onchange page
            eng.documentProperty().addListener(new ChangeListener<Document>() {
                @Override
                public void changed(ObservableValue<? extends Document> prop, Document oldDoc, Document newDoc) {
                    enableFirebug(eng);
                    System.out.println(".changed()");
                    eng.getDocument().getElementById("username").setAttribute("value", "admin");
                    eng.getDocument().getElementById("password").setAttribute("value", "admin");

                    HTMLInputElement botaoAcessar = (HTMLInputElement) eng.getDocument().getElementById("acessar");
                    botaoAcessar.click();
                    System.out.println("Pagina atual:"+eng.getTitle());
                    eng.load("http://localhost:8080/ebjur/restrito/pessoa/pessoa-list.xhtml");

                    eng.getDocument().getElementById("formListaPessoa:j_idt58").setAttribute("value", "ad");
                    
                    
                    
                    
                }
            });

            //onalert
            view.getEngine().setOnAlert(new EventHandler<WebEvent<String>>() {
                @Override
                public void handle(WebEvent<String> event) {
                    Alert dialogoInfo = new Alert(Alert.AlertType.INFORMATION);
                    dialogoInfo.setTitle("");
                    dialogoInfo.setHeaderText("Mensagem");
                    dialogoInfo.setContentText(event.getData());
                    dialogoInfo.showAndWait();
                }
            });

            //onalert
            view.getEngine().setConfirmHandler(new Callback<String, Boolean>() {

                @Override
                public Boolean call(String message) {

                    Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setTitle("Pergunta...");
                    String s = message;
                    alert.setContentText(s);

                    Optional<?> result = alert.showAndWait();

                    if ((result.isPresent()) && (result.get() == ButtonType.OK)) {

                        return true;
                    } else {
                        return false;
                    }

                }

            });

            GridPane grid = new GridPane();
            grid.setVgap(5);
            grid.setHgap(5);
            GridPane.setConstraints(locationField, 0, 0, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.SOMETIMES);
            GridPane.setConstraints(goButton, 1, 0);
            GridPane.setConstraints(view, 0, 1, 2, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);
            grid.getColumnConstraints().addAll(new ColumnConstraints(100, 100, Double.MAX_VALUE, Priority.ALWAYS, HPos.CENTER, true),
                    new ColumnConstraints(40, 40, 40, Priority.NEVER, HPos.CENTER, true));
            grid.getChildren().addAll(locationField, goButton, view);
            getChildren().add(grid);

        }

        @Override
        protected void layoutChildren() {
            List<Node> managed = getManagedChildren();
            double width = getWidth();
            double height = getHeight();
            double top = getInsets().getTop();
            double right = getInsets().getRight();
            double left = getInsets().getLeft();
            double bottom = getInsets().getBottom();
            for (int i = 0; i < managed.size(); i++) {
                Node child = managed.get(i);
                layoutInArea(child, left, top, width - left - right, height - top - bottom, 0, Insets.EMPTY, true, true, HPos.CENTER, VPos.CENTER);
            }
        }
    }

    private static class MyEventListener implements EventListener {

        private Stage stage;
        private Document doc;

        public MyEventListener(Document doc, Stage stage) {
            super();
            this.doc = doc;
            this.stage = stage;
        }

        @Override
        public void handleEvent(org.w3c.dom.events.Event evt) {
            System.out.println("Event received: " + evt.getType());

            String comp = ((Element) evt.getTarget()).getAttribute("id");
            System.out.println("id:" + comp);
            System.out.println("Target:" + evt.getTarget().toString());
            System.out.println("Evento:" + evt.getType().toString());
            if (comp.equalsIgnoreCase(COMPONENTE_VIEW)) {

              

            }

        }
    }

    public static void showMessage(Document doc, String texto) {
        Element message = doc.getElementById(COMPONENTE_MESSAGE);
        if (message != null) {
            message.setTextContent(texto);
        }
    }

    private static void enableFirebug(final WebEngine engine) {
        engine.executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}");
    }
}
