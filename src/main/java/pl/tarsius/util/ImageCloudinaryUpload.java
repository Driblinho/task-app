package pl.tarsius.util;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;

import java.io.*;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Jarek on 06.04.16.
 */
public class ImageCloudinaryUpload {

    private Cloudinary cloudinary;

    public ImageCloudinaryUpload() {
        Properties properties = new Properties();
        try {
            InputStream cfgFile = new FileInputStream(getClass().getClassLoader().getResource("properties/cloudinary.properties").getFile());
            properties.load(cfgFile);
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", properties.getProperty("cloudinary.cloudNam"),
                    "api_key", properties.getProperty("cloudinary.apiKey"),
                    "api_secret", properties.getProperty("cloudinary.apiSecret")));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ImageCloudinaryUpload(String cloudNam, String apiSecret, String apiKey) {

        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudNam,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }


    public Map<String,Object> send(String imagePath) throws IOException {

        return this.send(imagePath,null);

    }
    public Map<String,Object> send(String imagePath,String nameFile) throws IOException {

        File toUpload = new File(imagePath);
        Map<String,Object> cfg = (nameFile!=null)?ObjectUtils.asMap("public_id", nameFile):ObjectUtils.emptyMap();
        Map uploadResult = cloudinary.uploader().upload(toUpload,cfg);

        return uploadResult;

    }

    public String getUrl(String id) {
        return cloudinary.url().transformation(
                new Transformation().width(128).height(128).crop("thumb").gravity("faces")
        ).generate(id);
    }



}
