import com.bandwidth.sdk.*;
import com.bandwidth.sdk.model.Call;
import com.bandwidth.sdk.model.Message;

import java.io.StringWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

import static spark.Spark.*;

public class Main {

	public static String transferNumber = null;

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

		get("/transfer", (req, res) -> {
			HashMap model = new HashMap();
			model.put("transferNumber", transferNumber);
			return new ModelAndView(model, "bxml/callForwarding.ftl");
		}, new VelocityTemplateEngine());

		get("/phone", (req, res) -> {
			HashMap model = new HashMap();
			model.put("template", "templates/phone.ftl");
			return new ModelAndView(model, layout);
		}, new VelocityTemplateEngine());

		post("/phone", (request, response) -> {

			String toNumber = "+1" + request.queryParams("number");
			String text = request.queryParams("words");

			if (request.queryParams("action").equals("call")) {
				System.out.println("Going to try and make call.");
				try {
					outboundCall(toNumber, "+18328627643", text);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				sendText(toNumber, "+18328627643", text);
			}
			return null;
		});
		
		post("/transfer", (request, response) -> {
			setTransferNumber(request.queryParams("forward"));
			return null;
		});

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

		System.out.println(toNumber);
		System.out.println(fromNumber);
		System.out.println(text);
		System.out.println("Inside call");
		Call call = Call.create(toNumber, fromNumber, "https://protected-badlands-29901.herokuapp.com/phone", null);
		System.out.println("Call created");

		Thread.sleep(10000);

		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("sentence", text);
		params.put("voice", "kate");
		call.speakSentence(params);

		System.out.println("Sentence Spoken");
		Thread.sleep(4000);

		call.hangUp();

	}

	public static void sendText(String toNumber, String fromNumber, String text) {
		try {
			Message message = Message.create(toNumber, fromNumber, text);
			System.out.println("message:" + message);
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

	public String getTransferNumber() {
		return transferNumber;
	}

	public static void setTransferNumber(String num) {
		transferNumber = "+1" + num;
	}

}
