package net.jimblackler.picacal;

import javax.servlet.http.HttpServletRequest;

import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;

public class Authenticate {
  public static String getAuthUrl(HttpServletRequest request) throws OAuthException {
    GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
    oauthParameters.setOAuthConsumerKey(Secrets.CONSUMER_KEY);
    oauthParameters.setOAuthConsumerSecret(Secrets.CONSUMER_SECRET);
    oauthParameters.setScope("http://picasaweb.google.com/data/");
    StringBuffer reqURL = request.getRequestURL();
    reqURL.delete(reqURL.lastIndexOf("/"), reqURL.length());
    reqURL.append("/return");
    oauthParameters.setOAuthCallback(reqURL.toString());
    GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(new OAuthHmacSha1Signer());
    oauthHelper.getUnauthorizedRequestToken(oauthParameters);
    String oAuthTokenSecret = oauthParameters.getOAuthTokenSecret();
    request.getSession().setAttribute("oauthTokenSecret", oAuthTokenSecret);
    return oauthHelper.createUserAuthorizationUrl(oauthParameters);
  }
}
