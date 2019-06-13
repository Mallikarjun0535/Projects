package com.dizzion.portal.infra.sms;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.Account;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Set;

import static com.twilio.rest.api.v2010.Account.Status.ACTIVE;

@Component
@Slf4j
public class TwilioClient {

    private final PhoneNumber twilioPhoneNumber;

    public TwilioClient(@Value("${twilio.account-sid}") String twilioAccountSid,
                        @Value("${twilio.auth-token}") String twilioAuthToken,
                        @Value("${twilio.phone-number}") String twilioPhoneNumber) {
        Twilio.init(twilioAccountSid, twilioAuthToken);
        this.twilioPhoneNumber = new PhoneNumber(twilioPhoneNumber);
    }

    public void sendSMS(Set<String> phoneNumbers, String content) {
        phoneNumbers.stream()
                .filter(StringUtils::hasText)
                .map(PhoneNumber::new)
                .forEach(number -> {
                    try {
                        Message.creator(number, twilioPhoneNumber, content).create();
                    } catch (ApiException ex) {
                        log.error("Twilio API exception, code=" + ex.getCode(), ex);
                    }
                });
    }

    public boolean isAvailable() {
        try {
            return Account.fetcher().fetch(Twilio.getRestClient()).getStatus() == ACTIVE;
        } catch (Exception e) {
            return false;
        }
    }
}
