import com.bandwidth.sdk.*;
import com.bandwidth.sdk.model.Call;
import com.bandwidth.sdk.model.Message;
import com.bandwidth.sdk.xml.Response;
import com.bandwidth.sdk.xml.elements.*;
import com.bandwidth.sdk.exception.XMLInvalidAttributeException;
import com.bandwidth.sdk.exception.XMLInvalidTagContentException;
import com.bandwidth.sdk.exception.XMLMarshallingException;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.HashMap;

import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

import static spark.Spark.*;

public class PhoneServlet extends HttpServlet {

	public static final Logger logger = Logger.getLogger(Main.class.getName());

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

        logger.info("get request /phone");

    try {
      Response response = getResponse();

      resp.setContentType("application/xml");
      resp.getWriter().print(response.toXml());
    } catch (XMLInvalidAttributeException | XMLInvalidTagContentException e) {
         logger.log(Level.SEVERE, "invalid attribute or value", e);
    } catch (XMLMarshallingException e) {
         logger.log(Level.SEVERE, "invalid xml", e);
    }
  }

}