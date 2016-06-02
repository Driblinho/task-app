package pl.tarsius.util;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.multipart.FormDataMultiPart;

import javax.ws.rs.core.MediaType;
import java.io.*;

/**
 * Created by Jarek on 2016-04-05.
 */
public class Mail {

    private String nameApp;
    private String desc;
    private String subject;
    private String message;
    private String token;
    private String apDomain;
    private String nameUser;
    private String emailUser;
    private InputStream emailTemplate;
    private String apiKey;

    /**
     * Konstruktor idealizujący klucz i szablon
     * @param apiKey Klucz do API
     * @param emailTemplate Szablon email
     */
    public Mail(String apiKey, InputStream emailTemplate) {

        this.apiKey = apiKey;
        this.emailTemplate = emailTemplate;
    }

    /** Konstruktor pobierający szablon Email
     * @param emailTemplate
     */
    public Mail(InputStream emailTemplate) {
        ApiKeyReader key = new ApiKeyReader().load();
        if(key!=null)
            this.apiKey = key.getMailApiKey();
        this.emailTemplate = emailTemplate;
    }

    /**
     * Getter for property 'apDomain'.
     *
     * @return Value for property 'apDomain'.
     */
    public String getApDomain() {
        return apDomain;
    }

    /**
     * Setter for property 'apDomain'.
     *
     * @param apDomain Value to set for property 'apDomain'.
     */
    public void setApDomain(String apDomain) {
        this.apDomain = apDomain;
    }

    /**
     * Getter for property 'nameUser'.
     *
     * @return Value for property 'nameUser'.
     */
    public String getNameUser() {
        return nameUser;
    }

    /**
     * Setter for property 'nameUser'.
     *
     * @param nameUser Value to set for property 'nameUser'.
     */
    public void setNameUser(String nameUser) {
        this.nameUser = nameUser;
    }

    /**
     * Getter for property 'emailUser'.
     *
     * @return Value for property 'emailUser'.
     */
    public String getEmailUser() {
        return emailUser;
    }

    /**
     * Setter for property 'emailUser'.
     *
     * @param emailUser Value to set for property 'emailUser'.
     */
    public void setEmailUser(String emailUser) {
        this.emailUser = emailUser;
    }

    /**
     * Getter for property 'nameApp'.
     *
     * @return Value for property 'nameApp'.
     */
    public String getNameApp() {
        return nameApp;
    }

    /**
     * Setter for property 'nameApp'.
     *
     * @param nameApp Value to set for property 'nameApp'.
     */
    public void setNameApp(String nameApp) {
        this.nameApp = nameApp;
    }

    /**
     * Getter for property 'desc'.
     *
     * @return Value for property 'desc'.
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Setter for property 'desc'.
     *
     * @param desc Value to set for property 'desc'.
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * Getter for property 'subject'.
     *
     * @return Value for property 'subject'.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Setter for property 'subject'.
     *
     * @param subject Value to set for property 'subject'.
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Getter for property 'message'.
     *
     * @return Value for property 'message'.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Setter for property 'message'.
     *
     * @param message Value to set for property 'message'.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Getter for property 'token'.
     *
     * @return Value for property 'token'.
     */
    public String getToken() {
        return token;
    }

    /**
     * Setter for property 'token'.
     *
     * @param token Value to set for property 'token'.
     */
    public void setToken(String token) {
        this.token = token;
    }


    /** Metoda odpowiedzialna za wysyłanie przygotowanego maila
     * @return ClientResponse
     * @throws FileNotFoundException
     */
    public ClientResponse send() throws FileNotFoundException {

        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter("api",
                apiKey));
        WebResource webResource = client.resource("https://api.mailgun.net/v3/sandbox10b47d8d12c04e6883d6cdccd46212c4.mailgun.org/messages");
        FormDataMultiPart form = new FormDataMultiPart();
        form.field("from", getNameApp()+" <"+ getApDomain()+">");
        form.field("to", getNameUser()+" <"+ getEmailUser()+">");
        form.field("subject", getSubject());


        BufferedReader bf = new BufferedReader(new InputStreamReader(emailTemplate));
        String s;
        String temp="";

        try {
            while ((s=bf.readLine())!=null)
            {
                s=s.replace("{nameApp}", getNameApp());
                s=s.replace("{desc}",getDesc());
                s=s.replace("{subject}",getSubject());
                s=s.replace("{message}", getMessage());
                s=s.replace("{token}",getToken());
                temp+=s;
            }

            form.field("html", temp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return webResource.type(MediaType.MULTIPART_FORM_DATA_TYPE).
                post(ClientResponse.class, form);

    }
}
