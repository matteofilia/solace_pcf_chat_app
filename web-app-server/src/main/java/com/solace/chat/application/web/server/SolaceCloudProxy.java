package com.solace.chat.application.web.server;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.Gson;
import com.solace.chat.application.common.*;
import com.solace.services.core.model.SolaceServiceCredentials;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.nio.charset.Charset;

import org.json.JSONObject;

/**
 * The SolaceCloudProxy simply acts as a REST-ful proxy to the SolaceCloudInstace.
 * @author Thomas Kunnumpurath
 */
@Controller
public class SolaceCloudProxy {

    //Gson object for serializing/deserializing json objects
    Gson gson = new Gson();

    //Properties are read from resources/application.properties
    @Value("${solace.rest.host:}")
    private String solaceRESTHost;

    @Value("${solace.username:}")
    private String solaceUsername;

    @Value("${solace.password:}")
    private String solacePassword;

    @Value("${solace.vpn:}")
    private String solaceVPN;

    private String solaceWebMessagingHost;

    //HttpHeader for the http post
    private HttpHeaders httpHeaders;

    //Setting up the header for the Solace Request
    @PostConstruct
    public void init() {
        // Load environment configuration from cloud foundry
        String vcapServices = System.getenv("VCAP_SERVICES");
        if (vcapServices == null || vcapServices.length() == 0 || vcapServices.equals("{}")) {
            // Do nothing, we are running locally so all values will be imported from application.properties
        } else {
            JSONObject pubsubCredentials = new JSONObject(vcapServices)
                .getJSONArray("solace-pubsub")
                .getJSONObject(0)
                .getJSONObject("credentials");
            solaceUsername = pubsubCredentials.getString("clientUsername");
            solacePassword = pubsubCredentials.getString("clientPassword");
            solaceRESTHost = pubsubCredentials
                .getJSONArray("restUris")
                .getString(0);
            solaceWebMessagingHost = pubsubCredentials
                .getJSONArray("publicWebMessagingUris")
                .getString(0);
            solaceVPN = pubsubCredentials.getString("msgVpnName");
        }

        httpHeaders = new HttpHeaders() {{
            String auth = solaceUsername + ":" + solacePassword;
            byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(Charset.forName("US-ASCII")));
            String authHeader = "Basic " + new String(encodedAuth);
            set("Authorization", authHeader);
            
            //This header determines that the Http Request needs a response
            set("Solace-Reply-Wait-Time-In-ms", "3000");
            set("Content-Type","application/json");
        }};
    }

    //Function that makes a REST-ful call to Solace
    @RequestMapping(value = "/solace/cloud/proxy", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity ProxyLoginRequestToSolace(@RequestBody UserObject userObject) {
        //Pass through a response code based on the result of the REST-ful request
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<UserObject> request = new HttpEntity<UserObject>(userObject, httpHeaders);
                
        //The result of the request is an authenticated object
        AuthenticatedObject authenticatedObject = restTemplate.postForObject(solaceRESTHost + "/LOGIN/MESSAGE/REQUEST", request, AuthenticatedObject.class);

        //Pass through a response code based on the result of the REST-ful request
        if (authenticatedObject.isAuthenticated()) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }
    }

    @RequestMapping(value = "/resources/application-properties.js", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> getApplicationProperties() {
        String vcapServices = System.getenv("VCAP_SERVICES");
        if (vcapServices == null || vcapServices.length() == 0 || vcapServices.equals("{}")) {
            // We are running locally, redirect to application-properties-local.js file and do not generate application properties
            try {
                httpHeaders.setLocation(new URI("/resources/application-properties-local.js"));
            } catch (Exception e) {};
            ResponseEntity<String> redirect = new ResponseEntity<String>(null, httpHeaders, HttpStatus.SEE_OTHER);
            return redirect;
        }

        ChatApplicationProperties applicationProperties = new ChatApplicationProperties(
                solaceWebMessagingHost,
                solaceVPN,
                solaceUsername,
                solacePassword,
                "solace/chat",
                "solace/chat"
        );

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(
            new SimpleModule().addSerializer(
                ChatApplicationProperties.class,
                new ChatApplicationProperties.Serializer(ChatApplicationProperties.class)
            )
        );

        ResponseEntity<String> response;
        try {
             response = new ResponseEntity<String>(
                    "connectOptions = " + mapper.writeValueAsString((Object) applicationProperties),
                    httpHeaders,
                    HttpStatus.OK
            );
        } catch (JsonProcessingException e) {
            response = new ResponseEntity<String>("{error: true}", httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }
}