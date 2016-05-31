package pl.tarsius.database.Model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ireq on 30.05.16.
 */
public class ApiKeys {
    private static Logger loger = LoggerFactory.getLogger(ApiKeys.class);
    private String mailApiKey;
    private String cloudinaryApiKey;
    private String cloudinaryApiSecret;
    private String cloudinaryCloudNam;

    public ApiKeys(String mailApiKey, String cloudinaryCloudNam, String cloudinaryApiKey, String cloudinaryApiSecret) {
        this.cloudinaryCloudNam = cloudinaryCloudNam;
        this.mailApiKey = mailApiKey;
        this.cloudinaryApiKey = cloudinaryApiKey;
        this.cloudinaryApiSecret = cloudinaryApiSecret;
    }




    /**
     * Getter for property 'mailApiKey'.
     *
     * @return Value for property 'mailApiKey'.
     */
    public String getMailApiKey() {
        return mailApiKey;
    }

    /**
     * Setter for property 'mailApiKey'.
     *
     * @param mailApiKey Value to set for property 'mailApiKey'.
     */
    public void setMailApiKey(String mailApiKey) {
        this.mailApiKey = mailApiKey;
    }

    /**
     * Getter for property 'cloudinaryApiKey'.
     *
     * @return Value for property 'cloudinaryApiKey'.
     */
    public String getCloudinaryApiKey() {
        return cloudinaryApiKey;
    }

    /**
     * Setter for property 'cloudinaryApiKey'.
     *
     * @param cloudinaryApiKey Value to set for property 'cloudinaryApiKey'.
     */
    public void setCloudinaryApiKey(String cloudinaryApiKey) {
        this.cloudinaryApiKey = cloudinaryApiKey;
    }

    /**
     * Getter for property 'cloudinaryApiSecret'.
     *
     * @return Value for property 'cloudinaryApiSecret'.
     */
    public String getCloudinaryApiSecret() {
        return cloudinaryApiSecret;
    }

    /**
     * Setter for property 'cloudinaryApiSecret'.
     *
     * @param cloudinaryApiSecret Value to set for property 'cloudinaryApiSecret'.
     */
    public void setCloudinaryApiSecret(String cloudinaryApiSecret) {
        this.cloudinaryApiSecret = cloudinaryApiSecret;
    }

    /**
     * Getter for property 'cloudinaryCloudNam'.
     *
     * @return Value for property 'cloudinaryCloudNam'.
     */
    public String getCloudinaryCloudNam() {
        return cloudinaryCloudNam;
    }

    /**
     * Setter for property 'cloudinaryCloudNam'.
     *
     * @param cloudinaryCloudNam Value to set for property 'cloudinaryCloudNam'.
     */
    public void setCloudinaryCloudNam(String cloudinaryCloudNam) {
        this.cloudinaryCloudNam = cloudinaryCloudNam;
    }
}
