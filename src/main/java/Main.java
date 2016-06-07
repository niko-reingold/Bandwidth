import com.bandwidth.sdk.*;
import com.bandwidth.sdk.model.Call;
import com.bandwidth.sdk.model.Message;
import com.bandwidth.sdk.xml.Response;
import com.bandwidth.sdk.xml.elements.*;
import com.bandwidth.sdk.exception.XMLInvalidAttributeException;
import com.bandwidth.sdk.exception.XMLInvalidTagContentException;
import com.bandwidth.sdk.exception.XMLMarshallingException;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.parsers.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.HashMap;

import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

import static spark.Spark.*;

public class Main extends HttpServlet {

  public static final Logger logger = Logger.getLogger(Main.class.getName());
  private Response response;

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

        post("/phone", (request, response) -> {
          
          String number = "+1" + request.queryParams("number");
          String text = request.queryParams("words");
          
          if("call" == request.attribute("action")){
            outboundCall(number,"+18328627643",text);
          } else if ("text" == request.attribute("action")){
            sendText(number,"+18328627643",text);
          }

          this.doGet(req, resp);
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
  
  public Response outboundCall(String toNumber, String fromNumber, String text){
       response = new Response();
       Call call = new Call(fromNumber, toNumber);
       SpeakSentence speakSentence = new SpeakSentence(text, "paul", "male", "en");

       response.add(call);
       response.add(speakSentence);

       return response;
  }

  private static int getHerokuAssignedPort() {
    ProcessBuilder processBuilder = new ProcessBuilder();
    if (processBuilder.environment().get("PORT") != null) {
      return Integer.parseInt(processBuilder.environment().get("PORT"));
    }
    return 4567;
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

        logger.info("get request /main");

    try {
      resp.setContentType("application/xml");
      resp.getWriter().print(response.toXml());
    } catch (XMLInvalidAttributeException | XMLInvalidTagContentException e) {
         logger.log(Level.SEVERE, "invalid attribute or value", e);
    } catch (XMLMarshallingException e) {
         logger.log(Level.SEVERE, "invalid xml", e);
    }
  }

}
