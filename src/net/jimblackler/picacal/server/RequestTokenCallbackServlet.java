package net.jimblackler.picacal.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jimblackler.picacal.Common;
import net.jimblackler.picacal.Secrets;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.util.common.util.Base64;

public class RequestTokenCallbackServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException {
    GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
    oauthParameters.setOAuthConsumerKey(Secrets.CONSUMER_KEY);
    oauthParameters.setOAuthConsumerSecret(Secrets.CONSUMER_SECRET);

    GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(new OAuthHmacSha1Signer());
    String oauthTokenSecret = (String) request.getSession().getAttribute("oauthTokenSecret");
    oauthParameters.setOAuthTokenSecret(oauthTokenSecret);
    oauthHelper.getOAuthParametersFromCallback(request.getQueryString(), oauthParameters);
    try {
      JSONObject jsonObject = new JSONObject();
      String accessToken = oauthHelper.getAccessToken(oauthParameters);
      if (accessToken.length() == 0) {
        throw new ServletException("No access token");
      }
      jsonObject.put(Common.ACCESS_TOKEN_KEY, accessToken);
      String oauthTokenSecret2 = oauthParameters.getOAuthTokenSecret();
      jsonObject.put(Common.ACCESS_TOKEN_SECRET_KEY, oauthTokenSecret2);
      jsonObject.put(Common.USER_TIMEZONE, "Europe/London");
      for (Cookie cookie : request.getCookies()) {
        if (cookie.getName().equals("timezone")) {
          jsonObject.put(Common.USER_TIMEZONE, cookie.getValue());
        }
      }
      byte[] bytes = jsonObject.toString().getBytes();
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(Secrets.ENCRYPTION_PASSWORD, "AES"));
      byte[] encryptedBytes = cipher.doFinal(bytes);
      String string = Base64.encode(encryptedBytes);
      String data = URLEncoder.encode(string, "UTF-8");
      String string2 = request.getRequestURL().toString().replace("/return", "/ical") + "?data="
          + data;
      response.sendRedirect("http://www.google.com/calendar/render?cid="
          + URLEncoder.encode(string2, "UTF-8"));
    } catch (OAuthException e) {
      throw new ServletException(e);
    } catch (JSONException e) {
      throw new ServletException(e);
    } catch (InvalidKeyException e) {
      throw new ServletException(e);
    } catch (IllegalBlockSizeException e) {
      throw new ServletException(e);
    } catch (BadPaddingException e) {
      throw new ServletException(e);
    } catch (NoSuchAlgorithmException e) {
      throw new ServletException(e);
    } catch (NoSuchPaddingException e) {
      throw new ServletException(e);
    } catch (UnsupportedEncodingException e) {
      throw new ServletException(e);
    } catch (IOException e) {
      throw new ServletException(e);
    }
  }
}