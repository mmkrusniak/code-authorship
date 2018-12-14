package pl.jpetryk.redditbot.connectors;

import org.json.JSONException;
import pl.jpetryk.redditbot.exceptions.ImgurException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

/**
 * Created by Jan on 22/02/15.
 */
public interface ImgurConnectorInterface {

    String reuploadImage(String imageURL) throws ImgurException;
}
