var broker = new PubSubPlusBroker();
/***CALLBACK HANDLERS***/
  //alert handler
  function alertHandler(bResult, sMessage) {

    //create an empty div to be used for alerting
    //assign it our alert message
    var $div = $("<div></div>");
    $div.text(sMessage);

    //style the alert according to whether we
    //succeeded or failed.
    if (bResult) {
      $div.addClass("alert alert-success");
    } else {
      $div.addClass("alert alert-danger");
    }

    //add in our alert and have it fade out
    //in 5s.
    $("#alerts").append($div);
    $div.fadeOut(5000);

    return;
  }


  //call back function to handle intial connections
  function connectHandler(bResult, sMessage) {
    console.debug("In connectHandler");

    alertHandler(bResult, sMessage);
    if (bResult) {
      //we connected fine, now try and subscribe
      broker.subscribe(alertHandler);
      broker.onTopicMessage(messageHandler);
    }

    return;
  }

  //handles messages pulled from the broker
  function messageHandler(sMessage) {
    var hue = sMessage.slice(0, sMessage.search(";"));
    sMessage = sMessage.slice(sMessage.search(";")+1, sMessage.length);
    console.log("Hue for this connected user is: " + hue);

    console.debug("message handler called with text: " + sMessage);
    updateChatArea(sMessage, hue);

    return;
  }

  /***HELPER METHODS***/
  //updates the chat window with new text
  function updateChatArea(sText, hue) {
    $("#chatArea").append("<p class=\"message\" style=\"color: hsl("+hue+", 75%, 40%); background-color: hsl("+hue+", 75%, 90%)\">"+sText+"</p>");
  }


  function connectToSolace(){

  //try to connect
  broker.connect(connectHandler);
  }

   /***Button functions***/
  $(document).ready(function() {
    $('#chatInput').keypress(function (e) {
      var key = e.which;
      if(key == 13)  // the enter key code
       {
         $('#buttonSend').click();
         return false;  
       }
     });
  
   
    /*
     *Called when a user sends a message
     */
    $("#buttonSend").click(function() {
      console.debug("Caught send button event");
  
      //the text that the user just typed.
      var sChatText = $("#chatUser").text() + ": " + $("#chatInput").val();
  
      console.debug(sChatText);
  
      //attempt a publish to the broker topic
      broker.publish(sChatText, alertHandler)
      
  
      //reset the text input box
      $("#chatInput").val("");
    })
    });

