import com.bandwidth.sdk.*;
import com.bandwidth.sdk.model.Call;
import com.bandwidth.sdk.model.Message;
import com.bandwidth.sdk.xml.Response;
import com.bandwidth.sdk.xml.elements.*;

import com.bandwidth.sdk.exception.XMLMarshallingException;

import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

import static spark.Spark.*;

public class Main {

    public static void main(String[] args) {

        port(getHerokuAssignedPort());

        authenticate();
        String fromNumber = System.getenv().get("PHONE_NUMBER");

        staticFileLocation("/public");
        String layout = "templates/layout.ftl";

        get("/", (req, res) -> {
            HashMap model = new HashMap();
            model.put("template", "templates/phone.ftl");
            model.put("fromNumber", fromNumber);
            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());

        get("/phone", (req, res) -> {
            String toNumber = "+1" + req.queryParams("number");
            String text = req.queryParams("words");

            if (req.queryParams("action").equals("call")) {
                try {
                    String host = "http://" + req.host() + "/callEvents";
                    outboundCall(toNumber, fromNumber, host, text);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                sendText(toNumber, fromNumber, text);
            }

            HashMap model = new HashMap();
            model.put("template", "templates/phone.ftl");
            model.put("fromNumber", fromNumber);
            return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());

        get("/callEvents", (req, res) -> {
            String text = req.queryParams("tag");
            String event = req.queryParams("eventType");

            String bxml = "";

            if (event.equals("answer")) {
                try {
                    Response response = new Response();

                    SpeakSentence speakSentence = new SpeakSentence(text, "kate", "female", "en_US");
                    Hangup hangup = new Hangup();

                    response.add(speakSentence);
                    response.add(hangup);
                    bxml = response.toXml();

                    res.type("text/xml");
                } catch (XMLMarshallingException e) {
                    System.out.println(bxml);
                    System.out.println(e);
                }
                return bxml;
            } else {
                res.status(200);
                return res;
            }
        });

        get("/transferText", (req, res) -> {

            String bxml = "";
            try {
                Response response = new Response();

                String originalNumber = req.queryParams("from");
                String originalText = req.queryParams("text");
                SendMessage sendMessage = new SendMessage(fromNumber, "+19196705750", originalNumber + ": " + originalText);

                response.add(sendMessage);

                bxml = response.toXml();

                res.type("text/xml");
            } catch (XMLMarshallingException e) {
                System.out.println(bxml);
                System.out.println(e);
            }
            return bxml;
        });

        get("/transfer", (req, res) -> {

            String bxml = "";
            //           String callerID = req.queryParams("callId");
            if (req.queryParams("eventType").equals("answer")) {
                try {
                    Response response = new Response();

                    SpeakSentence speakSentence = new SpeakSentence("Transferring your call, please wait.", "kate", "female", "en_US");
//                Transfer transfer = new Transfer("+19195158209", callerID);
                    Transfer transfer = new Transfer("+19195158209", fromNumber);

                    response.add(speakSentence);
                    response.add(transfer);

                    bxml = response.toXml();

                    res.type("text/xml");
                } catch (XMLMarshallingException e) {
                    System.out.println(bxml);
                    System.out.println(e);
                }
                return bxml;
            } else {
                res.status(200);
                return res;
            }
        });

    }

    public static void authenticate() {
        String userId = System.getenv().get("BANDWIDTH_USER_ID");
        String apiToken = System.getenv().get("BANDWIDTH_API_TOKEN");
        String apiSecret = System.getenv().get("BANDWIDTH_API_SECRET");

        try {
            BandwidthClient.getInstance().setCredentials(userId, apiToken,
                    apiSecret);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void outboundCall(String toNumber, String fromNumber, String callbackURL,
                                    String text) throws Exception {

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("to", toNumber);
        params.put("from", fromNumber);
        params.put("callbackUrl", callbackURL);
        params.put("tag", text);
        params.put("callbackHttpMethod", "GET");

        Call.create(params);
    }

    public static void sendText(String toNumber, String fromNumber, String text) {
        try {
            Message message = Message.create(toNumber, fromNumber, text);
            System.out.println("Sent Message");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567;
    }

}
