package net.jimblackler.picacal.server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.GregorianCalendar;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Url;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.property.XProperty;
import net.fortuna.ical4j.util.UidGenerator;
import net.jimblackler.picacal.Common;
import net.jimblackler.picacal.Secrets;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.Link;
import com.google.gdata.data.Person;
import com.google.gdata.data.photos.AlbumFeed;
import com.google.gdata.data.photos.PhotoEntry;
import com.google.gdata.util.ServiceException;
import com.google.gdata.util.common.util.Base64;
import com.google.gdata.util.common.util.Base64DecoderException;

@SuppressWarnings("serial")
public class ICalServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    try {
      byte[] encryptedBytes = Base64.decode(request.getParameter("data"));

      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(Secrets.ENCRYPTION_PASSWORD, "AES"));
      byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
      JSONObject jsonObject = new JSONObject(URLDecoder.decode(new String(decryptedBytes), "UTF-8"));

      PicasawebService service = new PicasawebService("exampleCo-exampleApp-1");
      GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
      oauthParameters.setOAuthConsumerKey(Secrets.CONSUMER_KEY);
      oauthParameters.setOAuthConsumerSecret(Secrets.CONSUMER_SECRET);
      String token = jsonObject.getString(Common.ACCESS_TOKEN_KEY);
      if (token.length() == 0) {
        throw new ServletException("Access token not supplied");
      }
      oauthParameters.setOAuthToken(token);
      String tokenSecret = jsonObject.getString(Common.ACCESS_TOKEN_SECRET_KEY);
      if (tokenSecret.length() == 0) {
        throw new ServletException("Access token secret not supplied");
      }
      oauthParameters.setOAuthTokenSecret(tokenSecret);
      service.setOAuthCredentials(oauthParameters, new OAuthHmacSha1Signer());

      TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
      TimeZone timezone = registry.getTimeZone(jsonObject.getString(Common.USER_TIMEZONE));

      URL feedUrl = new URL(
          "https://picasaweb.google.com/data/feed/api/user/default?kind=photo&max-results=1000");
      AlbumFeed feed = service.getFeed(feedUrl, AlbumFeed.class);

      Calendar calendar = new Calendar();

      PropertyList properties = calendar.getProperties();
      properties.add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
      calendar.getComponents().add(timezone.getVTimeZone());
      List<Person> authors = feed.getAuthors();
      if (authors.size() > 0) {
        properties.add(new XProperty("X-WR-CALNAME", "Photos by " + authors.get(0).getName()));
      } else {
        properties.add(new XProperty("X-WR-CALNAME", "Photos"));
      }
      properties.add(new XProperty("X-WR-TIMEZONE", timezone.getID()));
      properties.add(Version.VERSION_2_0);
      properties.add(CalScale.GREGORIAN);

      UidGenerator ug = new UidGenerator(null, "1");

      for (PhotoEntry photo : feed.getPhotoEntries()) {
        System.out.println(photo.getTitle().getPlainText());
        String eventName = photo.getTitle().getPlainText();

        // The convoluted time handling here is a factor of (1) Java's limited time handling with
        // respect to time zones (2) iCal4j's inheritance of these limited out-dated types and (3)
        // the strange time stamps found in EXIF data. Here, instead of UTC, photos are time stamped
        // in milliseconds from the epoch UTC with the same clock hands as the local time in which
        // the picture was taken.
        // Unfortunately in Java a DateTime object with a milliseconds since 1970 time is *always*
        // interpreted as being a UTC time. Switching time zones converts this time to this time
        // zone. This is not what we want because the time was already in local space, converting
        // adds a second offset.
        // The solution is to load the object as if it were a UTC DateTime, then create a new local
        // time object from the calendar values.
        Long value = photo.getTimestampExt().getValue();

        GregorianCalendar eventDate = new GregorianCalendar();
        eventDate.setTimeInMillis(value);

        GregorianCalendar localDate = new GregorianCalendar(timezone);
        localDate.setTimeInMillis(0);
        localDate.set(eventDate.get(GregorianCalendar.YEAR),
            eventDate.get(GregorianCalendar.MONTH), eventDate.get(GregorianCalendar.DATE),
            eventDate.get(GregorianCalendar.HOUR_OF_DAY), eventDate.get(GregorianCalendar.MINUTE),
            eventDate.get(GregorianCalendar.SECOND));
        DateTime start = new DateTime(localDate.getTime());
        start.setTimeZone(timezone);
        VEvent meeting = new VEvent(start, start, eventName);

        Link link = photo.getLink("alternate", "text/html");
        String href2 = link.getHref();

        PropertyList properties2 = meeting.getProperties();
        properties2.add(new Description(href2));
        properties2.add(new Url(new URI(href2)));
        properties2.add(ug.generateUid());

        calendar.getComponents().add(meeting);
      }

      // response.setContentType("text/calendar");
      response.setContentType("text/html");
      new CalendarOutputter().output(calendar, response.getWriter());
    } catch (JSONException e) {
      throw new ServletException(e);
    } catch (ValidationException e) {
      throw new ServletException(e);
    } catch (URISyntaxException e) {
      throw new ServletException(e);
    } catch (ServiceException e) {
      throw new ServletException(e);
    } catch (OAuthException e) {
      throw new ServletException(e);
    } catch (Base64DecoderException e) {
      throw new ServletException(e);
    } catch (InvalidKeyException e) {
      throw new ServletException(e);
    } catch (NoSuchAlgorithmException e) {
      throw new ServletException(e);
    } catch (NoSuchPaddingException e) {
      throw new ServletException(e);
    } catch (IllegalBlockSizeException e) {
      throw new ServletException(e);
    } catch (BadPaddingException e) {
      throw new ServletException(e);
    }
  }
}
