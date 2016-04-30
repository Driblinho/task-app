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

    public Mail(String apiKey, InputStream emailTemplate) {

        this.apiKey = apiKey;
        this.emailTemplate = emailTemplate;



    }

    public String getApDomain() {
        return apDomain;
    }

    public void setApDomain(String apDomain) {
        this.apDomain = apDomain;
    }

    public String getNameUser() {
        return nameUser;
    }

    public void setNameUser(String nameUser) {
        this.nameUser = nameUser;
    }

    public String getEmailUser() {
        return emailUser;
    }

    public void setEmailUser(String emailUser) {
        this.emailUser = emailUser;
    }

    public String getNameApp() {
        return nameApp;
    }

    public void setNameApp(String nameApp) {
        this.nameApp = nameApp;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }



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
