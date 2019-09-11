package com.solace.chat.application.web.server;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class ChatApplicationProperties {

    private String brokerUrl;
    private String vpn;
    private String username;
    private String password;
    private String publishTopic;
    private String subscribeTopic;

    public ChatApplicationProperties(String brokerUrl, String vpn, String username, String password, String publishTopic, String subscribeTopic) {
        this.brokerUrl = brokerUrl;
        this.vpn = vpn;
        this.username = username;
        this.password = password;
        this.publishTopic = publishTopic;
        this.subscribeTopic = subscribeTopic;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public String getVpn() {
        return vpn;
    }

    public void setVpn(String vpn) {
        this.vpn = vpn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPublishTopic() {
        return publishTopic;
    }

    public void setPublishTopic(String publishTopic) {
        this.publishTopic = publishTopic;
    }

    public String getSubscribeTopic() {
        return subscribeTopic;
    }

    public void setSubscribeTopic(String subscribeTopic) {
        this.subscribeTopic = subscribeTopic;
    }

    public static class Serializer extends StdSerializer<ChatApplicationProperties> {

        protected Serializer(Class<ChatApplicationProperties> t) {
            super(t);
        }

        @Override
        public void serialize(ChatApplicationProperties value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("sBROKERURL", value.getBrokerUrl());
            gen.writeStringField("sVPN", value.getVpn());
            gen.writeStringField("sUSERNAME", value.getUsername());
            gen.writeStringField("sPASSWORD", value.getPassword());
            gen.writeStringField("sPublishTopic", value.getPublishTopic());
            gen.writeStringField("sSubscribeTopic", value.getSubscribeTopic());
            gen.writeEndObject();
        }
    }
}