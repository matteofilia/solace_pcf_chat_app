# Solace Chat Application

## About

This demo chat application is based of off the Solace developer exercises for the Solace PubSub+ Cloud
https://solace.com/blog/build-chat-app-solace-1/

## Running

### With Pivotal Cloud Foundry

1. Ensure you have a working Solace PubSub+ tile setup for Pivotal Cloud Foundry
2. Ensure TCP routing is enabled (double check that your changes did take effect - you will likely have to recreate your PubSub+ service)
3. Create a PubSub+ service named solace-pubsub-sample-instance, or modify the app manifest with the name of your PubSub+ service
```
cf create-service solace-pubsub $SERVICE_PLAN_NAME solace-pubsub-sample-instance
```
4. Push the application
```
cf push
```
The application will run automatically. Use the route provided by the chat-server app to access it.

### Locally

1. Ensure both application.properties and the application-properties-local.js file contains your application settings
2. Run using Maven

```
mvn spring-boot:run
```

You can access the chat application on http://localhost:8081
