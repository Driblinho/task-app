package pl.tarsius.util;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by Jarek on 06.04.16.
 */
public class ImageCloudinaryUpload {

    private Cloudinary cloudinary;

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



}
