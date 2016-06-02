package pl.tarsius.util;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**Klasa obsługująca wysyłanie awatarów
 * Created by Jarek on 06.04.16.
 */
public class ImageCloudinaryUpload {

    /**
     * Pole na obiekt {@link Cloudinary}
     */
    private Cloudinary cloudinary;

    /**
     * Konstruktor wczytujać z bazy konfiguracje API
     */
    public ImageCloudinaryUpload() {
        ApiKeyReader key = new ApiKeyReader().load();
        if(key!=null)
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", key.getCloudinaryCloudNam(),
                "api_key", key.getCloudinaryApiKey(),
                "api_secret", key.getCloudinaryApiSecret()));
    }

    /**
     * Konstruktor pobierający konfigurację API
     * @param cloudNam
     * @param apiSecret
     * @param apiKey
     */
    public ImageCloudinaryUpload(String cloudNam, String apiSecret, String apiKey) {

        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudNam,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }


    /**
     * Metoda wysyłająca zdjęcie
     * @param imagePath
     * @return
     * @throws IOException
     */
    public Map<String,Object> send(String imagePath) throws IOException {

        return this.send(imagePath,null);

    }

    /**
     * Metoda wysyłająca zdjęcie z określoną nazwą
     * @param imagePath Path do obrazka
     * @param nameFile Nazwa Pliku
     * @return Dane zwrotne z API
     * @throws IOException Wyjątek w wypadku problemu z plikiem
     */
    public Map<String,Object> send(String imagePath,String nameFile) throws IOException {

        File toUpload = new File(imagePath);
        Map<String,Object> cfg = (nameFile!=null)?ObjectUtils.asMap("public_id", nameFile):ObjectUtils.emptyMap();
        Map uploadResult = cloudinary.uploader().upload(toUpload,cfg);

        return uploadResult;

    }

    /**
     * Metoda pobiera adres url zdjęcia na podstawie ID
     * @param id ID zdjęcia
     * @return Adres zdjęcia
     */
    public String getUrl(String id) {
        return cloudinary.url().transformation(
                new Transformation().width(128).height(128).crop("thumb").gravity("faces")
        ).generate(id);
    }



}
