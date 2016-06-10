import com.bandwidth.sdk.*;
import com.bandwidth.sdk.model.Call;
import com.bandwidth.sdk.model.Message;

import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

import static spark.Spark.*;

public class Main {

	public static void main(String[] args) {

		port(getHerokuAssignedPort());

		authenticate();

		staticFileLocation("/public");
		String layout = "templates/layout.ftl";

		get("/", (req, res) -> {
			HashMap model = new HashMap();
			model.put("template", "templates/phone.ftl");
			model.put("transferNumber", transferNumber);
			return new ModelAndView(model, layout);
		}, new VelocityTemplateEngine());

		get("/phone", (req, res) -> {
			
			String toNumber = "+1" + req.queryParams("number");
			String text = req.queryParams("words");

			if (req.queryParams("action").equals("call")) {
				System.out.println("Going to try to make call.");
				try {
					outboundCall(toNumber, "+18328627643", text);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("Going to try to send text.");
				sendText(toNumber, "+18328627643", text);
			}
			
			HashMap model = new HashMap();
			model.put("template", "templates/phone.ftl");
			return new ModelAndView(model, layout);
		}, new VelocityTemplateEngine());

	}

	public static void authenticate() {
		String userId = "u-72jjash6ldbrtsjvmrsfetq";
		String apiToken = "t-depqhu2y25ut7gsdkussxbq";
		String apiSecret = "ajk2odf574li7qvbkz7qtg3fr36wsfnttcpso6y";

		try {
			BandwidthClient.getInstance().setCredentials(userId, apiToken,
					apiSecret);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void outboundCall(String toNumber, String fromNumber,
			String text) throws Exception {

		System.out.println("Inside call function");
		System.out.println("toNumber: " + toNumber);
		System.out.println("fromNumber: " + fromNumber);
		System.out.println("Message: " + text);
		Call call = Call.create(toNumber, fromNumber, "https://protected-badlands-29901.herokuapp.com/phone", null);
		System.out.println("Call created");

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
