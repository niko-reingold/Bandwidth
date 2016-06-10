import com.bandwidth.sdk.*;
import com.bandwidth.sdk.model.Call;
import com.bandwidth.sdk.model.Message;
import com.bandwidth.sdk.exception.XMLInvalidAttributeException;
import com.bandwidth.sdk.exception.XMLInvalidTagContentException;
import com.bandwidth.sdk.exception.XMLMarshallingException;
import com.bandwidth.sdk.xml.Response;
import com.bandwidth.sdk.xml.elements.*;

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
				System.out.println("Going to try to make call.");
				try {
                    String host = "http://" + req.host() + "/callEvents";
                    System.out.println(host);
					outboundCall(toNumber, fromNumber, host, text);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("Going to try to send text.");
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

            if(event.equals("answer")){
                try {
                    Response response = new Response();

                    SpeakSentence speakSentence = new SpeakSentence(text, "kate", "female", "en_US");
                    Hangup hangup = new Hangup();

                    response.add(speakSentence);
                    response.add(hangup);
                    bxml = response.toXml();

                    System.out.println("Made bxml response");
                    System.out.println(bxml);

                    res.type("text/xml");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return bxml
            } else {
                res.status(200);
                return res;
            }
        });

        get("/transfer", (req, res) -> {

            System.out.println("In transfer");

            String bxml = "";
            //           String callerID = req.queryParams("callId");
            if(req.queryParams("eventType").equals("answer")){
                System.out.println("recieving call");
                try {
                    Response response = new Response();

                    SpeakSentence speakSentence = new SpeakSentence("Transferring your call, please wait.", "kate", "female", "en_US");
//                Transfer transfer = new Transfer("+13364078290", callerID);
                    Transfer transfer = new Transfer("+13364078290", fromNumber);

                    response.add(speakSentence);
                    response.add(transfer);

                    bxml = response.toXml();

                    System.out.println("Made bxml response");
                    System.out.println(bxml);

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

		System.out.println("Inside call function");
		System.out.println("toNumber: " + toNumber);
		System.out.println("fromNumber: " + fromNumber);
		System.out.println("Message: " + text);
		Call.create(toNumber, fromNumber, callbackURL, text);
		System.out.println("Call created");
/*
		Thread.sleep(20000);
		
		System.out.println("About to speak sentence");

		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("sentence", text);
		params.put("voice", "paul");
		params.put("gender", "male");
		params.put("locale", "en_US");
		call.speakSentence(params);

		System.out.println("Sentence Spoken");
		Thread.sleep(4000);

		call.hangUp();
*/
	}

	public static void sendText(String toNumber, String fromNumber, String text) {
		System.out.println("Inside text function");
		System.out.println("toNumber: " + toNumber);
		System.out.println("fromNumber: " + fromNumber);
		System.out.println("Message: " + text);
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
