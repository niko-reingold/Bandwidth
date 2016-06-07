import com.bandwidth.sdk.*;
import com.bandwidth.sdk.model.Call;
import com.bandwidth.sdk.model.Message;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import java.util.HashMap;

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
          HashMap model =new HashMap();
          model.put("template", "templates/phone.ftl");
          return new ModelAndView(model, layout);
        }, new VelocityTemplateEngine());

        post("/", (request, response) -> {
          
          String toNumber = "+1" + request.queryParams("number");
          String text = request.queryParams("words");

          MustacheFactory mf = new DefaultMustacheFactory();
          Mustache mustache = mf.compile("template.mustache");
          mustache.execute(new PrintWriter(System.out), new Main()).flush();

          System.out.println(request.params("action"));
          
          if("call" == request.queryParams("action")){
            response.type("/bxml/call.xml");
            //outboundCall(number,"+18328627643",text);
          } else if ("text" == request.queryParams("action")){
            sendText(toNumber,"+18328627643",text);
          }
          return null;
        });
       

  }

  public static void authenticate(){
    String userId = "u-72jjash6ldbrtsjvmrsfetq";          //my userId
        String apiToken = "t-depqhu2y25ut7gsdkussxbq";          //my token
        String apiSecret = "ajk2odf574li7qvbkz7qtg3fr36wsfnttcpso6y"; //my secret

        try {
            BandwidthClient.getInstance().setCredentials(userId, apiToken, apiSecret);
            // use other resource classes here.
        }
        catch(Exception e) {
            e.printStackTrace();
        }
  }
  
  public static void sendText(String toNumber, String fromNumber, String text){
    try {
             Message message = Message.create(toNumber, fromNumber, text);  
             System.out.println("message:" + message);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  // public void outboundCall(String toNumber, String fromNumber, String text){
  //      action = new Response();
  //      Call call = new Call(fromNumber, toNumber);
  //      SpeakSentence speakSentence = new SpeakSentence(text, "paul", "male", "en");

  //      action.add(call);
  //      action.add(speakSentence);
  // }

  private static int getHerokuAssignedPort() {
    ProcessBuilder processBuilder = new ProcessBuilder();
    if (processBuilder.environment().get("PORT") != null) {
      return Integer.parseInt(processBuilder.environment().get("PORT"));
    }
    return 4567;
  }

}
