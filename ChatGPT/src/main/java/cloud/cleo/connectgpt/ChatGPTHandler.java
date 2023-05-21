/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cloud.cleo.connectgpt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.softwarebymark.lex.AbstractLexRequestHandler;
import com.github.softwarebymark.lex.domain.FulfillmentState;
import com.github.softwarebymark.lex.domain.Intent;
import com.github.softwarebymark.lex.domain.LexRequest;
import com.github.softwarebymark.lex.domain.LexResponse;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import java.util.List;
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
public class ChatGPTHandler extends AbstractLexRequestHandler {

    // Initialize the Log4j logger.
    Logger log = LogManager.getLogger();

    final static ObjectMapper mapper = new ObjectMapper();

    final static ConnectClient connect = ConnectClient.builder()
            .region(Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable())))
            .build();

    
    final static OpenAiService service = new OpenAiService(System.getenv("OPENAI_API_KEY"));
    final static String OPENAI_MODEL = System.getenv("OPENAI_MODEL");
    
    @Override
    public LexResponse handleRequest(LexRequest lexRequest, Map<String, String> sessionAttributes) {
        try {
            final var intentName = lexRequest.getSessionState().getIntent().getName();
            log.debug("Intent: " + intentName);
            if ("FallbackIntent".equalsIgnoreCase(intentName)) {
                return processGPT(lexRequest);
            }
        } catch (Exception e) {
            log.error(e);

            return createCloseDialogActionResponse(FulfillmentState.Failed, "Sorry, I'm having a problem fulfilling your request.  Please try again later.",
                    new Intent(lexRequest.getSessionState().getIntent(), FulfillmentState.Failed));
        }
        return createCloseDialogActionResponse(FulfillmentState.Failed, "Could Not match any intents",
                new Intent(lexRequest.getSessionState().getIntent(), FulfillmentState.Failed));
    }

    private LexResponse processGPT(LexRequest lexRequest) {
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .messages(List.of(new ChatMessage("user",lexRequest.getInputTranscript())))
                .model(OPENAI_MODEL)
                .maxTokens(500)
                .build();
        
        final var completion = service.createChatCompletion(request);
        log.debug(completion);
        
        return createCloseDialogActionResponse(FulfillmentState.Fulfilled, completion.getChoices().get(0).getMessage().getContent(),
                new Intent(lexRequest.getSessionState().getIntent(), FulfillmentState.Fulfilled));
    }

}
