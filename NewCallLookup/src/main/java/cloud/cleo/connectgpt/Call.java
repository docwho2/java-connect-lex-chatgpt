package cloud.cleo.connectgpt;

import java.time.Instant;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

/**
 *
 * @author sjensen
 */
@DynamoDbBean
public class Call {
    
    private String contactId;
    private Instant callStart;

    public Call() {
    }

    
    public Call(String contactId, Instant callStart) {
        this.contactId = contactId;
        this.callStart = callStart;
    }

    /**
     * @return the contactId
     */
    public String getContactId() {
        return contactId;
    }

    /**
     * @param contactId the contactId to set
     */
    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    /**
     * @return the callStart
     */
    public Instant getCallStart() {
        return callStart;
    }

    /**
     * @param callStart the callStart to set
     */
    public void setCallStart(Instant callStart) {
        this.callStart = callStart;
    }
    
}
