import com.bandwidth.sdk.*;
import com.bandwidth.sdk.model.Call;
import com.bandwidth.sdk.model.Message;
import com.bandwidth.sdk.xml.Response;
import com.bandwidth.sdk.xml.elements.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.text.SimpleDateFormat;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

import static spark.Spark.*;

public class Main {

    public static String[] transcriptions = {"", "", "", "", ""};
    public static String[] mediaLinks = {"/messages", "/messages", "/messages", "/messages", "/messages"};
    public static int transCounter = 0;

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

        get("/messages", (req, res) -> {
            HashMap model = new HashMap();
            model.put("template", "templates/transcriptions.ftl");
            model.put("transcriptions", transcriptions);
            model.put("mediaLinks", mediaLinks);
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return bxml;
            } else {
                res.status(200);
                return res;
            }
        });

        get("/transferText", (req, res) -> {
            System.out.println("Transferring text");

            String bxml = "";
            try {
                Response response = new Response();

                String originalNumber = req.queryParams("from");
                String originalText = req.queryParams("text");
                SendMessage sendMessage = new SendMessage(fromNumber, "+19196705750", originalNumber + ": " + originalText);

                response.add(sendMessage);

                bxml = response.toXml();

                res.type("text/xml");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bxml;
        });

        get("/voicemail", (req, res) -> {
            System.out.println("In voicemail");

            String bxml = "";
            if (req.queryParams("eventType").equals("answer")) {
                try {
                    Response response = new Response();

                    SpeakSentence speakSentence = new SpeakSentence("Press 1 to leave a voicemail. Press 2 to be transferred.", "kate", "female", "en_US");
                    Gather gather = new Gather();
                    gather.setRequestUrl("http://" + req.host() + "/transfer");
                    gather.setMaxDigits(1);
                    gather.setSpeakSentence(speakSentence);

                    response.add(gather);
                    response.add(speakSentence);

                    bxml = response.toXml();

                    res.type("text/xml");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("Going to transfer");
                return bxml;
            } else {
                res.status(200);
                return res;
            }
        });

        get("/transfer", (req, res) -> {

            System.out.println("In transfer");
            System.out.println(req.queryParams());
            System.out.println(req.queryParams("eventType"));
            if (!req.queryParams("eventType").equals("gather")) {  //If not a gather eventType then we end
                res.status(200);                                 //hanging up triggers the /transfer to be called a 2nd time
                return res;
            }
            String bxml = "";
            //           String callerID = req.queryParams("callId");
            try {
                if (req.queryParams("digits").equals("1")) {
                    System.out.println("In record try");

                    Response response = new Response();
                    SpeakSentence speakSentence = new SpeakSentence("Please leave a message.", "kate", "female", "en_US");
                    Record record = new Record("http://" + req.host() + "/transcriptions", 1000);
                    record.setMaxDuration(300);
                    record.setTranscribe(true);
                    record.setTranscribeCallbackUrl("http://" + req.host() + "/transcriptions");
                    response.add(speakSentence);
                    response.add(record);
                    bxml = response.toXml();
                    
                } else {
                    System.out.println("pressed 2");
                    Response response = new Response();

                    SpeakSentence speakSentence = new SpeakSentence("Transferring your call, please wait.", "kate", "female", "en_US");
                    //                Transfer transfer = new Transfer("+19195158209", callerID);
                    Transfer transfer = new Transfer("+19195158209", fromNumber);

                    response.add(speakSentence);
                    response.add(transfer);

                    bxml = response.toXml();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            res.type("text/xml");
            return bxml;
        });


        get("/transcriptions", (req, res) -> {
            if (req.queryParams("state").equals("complete")) {
                String mediaUrl = "https://api.catapult.inetwork.com/v1/users/" + System.getenv().get("BANDWIDTH_USER_ID") + "/media/" + req.queryParams("callId") + "-1.wav";
                String transUrl = req.queryParams("recordingUri") + "/transcriptions";
                try {
                    HttpResponse<JsonNode> response = Unirest.get(transUrl).basicAuth(System.getenv().get("BANDWIDTH_API_TOKEN"), System.getenv().get("BANDWIDTH_API_SECRET")).asJson();
                    JsonNode transcription = response.getBody();
                    String trans = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date()) + " - " + transcription.toString();
                    transcriptions[transCounter % 5] = trans;
                    mediaLinks[transCounter % 5] = mediaUrl;
                    transCounter++;
                } catch (com.mashape.unirest.http.exceptions.UnirestException e) {
                    System.out.println(e);
                }
            }
            return null;
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
            Message.create(toNumber, fromNumber, text);
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
