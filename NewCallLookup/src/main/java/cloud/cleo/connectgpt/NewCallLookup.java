/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cloud.cleo.connectgpt;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.connect.ConnectClient;

/**
 * Receive a ConnectEvent via SNS
 *
 * @author sjensen
 */
public class NewCallLookup implements RequestHandler<SNSEvent, Void> {

    // Initialize the Log4j logger.
    Logger log = LogManager.getLogger();

    final static ObjectMapper mapper = new ObjectMapper();

    final static ConnectClient connect = ConnectClient.builder()
            .region(Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable())))
            .build();

    
    @Override
    public Void handleRequest(SNSEvent event, Context context) {
        try {
            log.debug(event);

            SNSEvent.SNS sns = event.getRecords().get(0).getSNS();
            log.debug("Record recieved " + sns);

            log.debug("Message Content is");
            log.debug(sns.getMessage());
            
            // Map the message content onto a ConnectEvent
            final var connEvent = mapper.readTree(sns.getMessage());

            
            connect.updateContactAttributes((t) -> {
                t.instanceId(connEvent.findValue("InstanceARN").asText())
                        .initialContactId(connEvent.findValue("InitialContactId").asText())
                        .attributes(Map.of("Hello", "World"));
            });
            
        } catch (Exception e) {
            log.error(e);
        }
        return null;
    }

}
