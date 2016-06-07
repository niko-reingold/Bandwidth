import com.bandwidth.sdk.*;
import com.bandwidth.sdk.model.Call;
import com.bandwidth.sdk.model.Message;
import com.bandwidth.sdk.xml.Response;
import com.bandwidth.sdk.xml.elements.*;
import com.bandwidth.sdk.exception.XMLInvalidAttributeException;
import com.bandwidth.sdk.exception.XMLInvalidTagContentException;
import com.bandwidth.sdk.exception.XMLMarshallingException;

import javax.servlet.http.HttpServlet;

import org.xml.sax.*;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

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
        
  //    Document xmlDoc = getDocument("./src/main/callForwarding.xml");
        

        post("/phone", (request, response) -> {
          
          String number = "+1" + request.queryParams("number");
          String text = request.queryParams("words");
          
          if("call" == request.queryParams("action")){
            outboundCall(number,"+18328627643",text);
          } else if ("text" == request.queryParams("action")){
            sendText(number,"+18328627643",text);
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

    // get("/message", (req, res) -> {
    //  String bxml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
    //    "<Response>\n" +
    //      "<SendMessage from=\"" + fromNumber + "\" to=\"" + toNumber + "\">\n" +
    //        text + "\n" +
    //      "</SendMessage>\n" +
    //    "</Response>";
    //  send(bxml);
    // });
  }
  
  public static void outboundCall(String toNumber, String fromNumber, String text){
    try {
       Response response = new Response();
       Call call = new Call(fromNumber, toNumber);
       SpeakSentence speakSentence = new SpeakSentence(text, "paul", "male", "en");

    //   Call call = Call.create(toNumber, fromNumber);
       response.add(call);
       response.add(speakSentence);

       resp.setContentType("application/xml");
       resp.getWriter().print(response.toXml());
    } catch (XMLInvalidAttributeException | XMLInvalidTagContentException e) {
        logger.log(Level.SEVERE, "invalid attribute or value", e);
    } catch (XMLMarshallingException e) {
        logger.log(Level.SEVERE, "invalid xml", e);
    }

    //   call.hangUp();
    // } catch (Exception e) {
    //   e.printStackTrace();
    // }

    // get("/call", (req, res) -> {
    //  String bxml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
    //    "<Response>\n" +
    //      "<Call from=\"" + fromNumber + "\" to=\"" + toNumber + "\">\n" +
    //      "<SpeakSentence>" + text + "</SpeakSentence>\n" +
    //      "</Call>\n" +
    //    "</Response>";
    //  post(bxml);
    // });
  }

  private static int getHerokuAssignedPort() {
    ProcessBuilder processBuilder = new ProcessBuilder();
    if (processBuilder.environment().get("PORT") != null) {
      return Integer.parseInt(processBuilder.environment().get("PORT"));
    }
    return 4567;
  }

  private static Document getDocument(String docString) {
    
    try {
      DocumentBuilderFactory factory =DocumentBuilderFactory.newInstance();
      
      factory.setIgnoringComments(true);
      factory.setIgnoringElementContentWhitespace(true);
      factory.setValidating(true);
      
      DocumentBuilder builder = factory.newDocumentBuilder();
      
      return builder.parse(new InputSource(docString));
    }
    
    catch(Exception ex){
      System.out.println(ex.getMessage());
    }
    
    return null;
  }

}
