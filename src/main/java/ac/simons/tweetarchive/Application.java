/*
 * Copyright 2016 michael-simons.eu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ac.simons.tweetarchive;

import ac.simons.tweetarchive.tweets.UserStreamAdapterImpl;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * @author Michael J. Simons, 2016-09-05
 */
@SpringBootApplication
@SuppressWarnings({"checkstyle:designforextension"})
public class Application implements ApplicationRunner {

    @Autowired(required = false)
    private TwitterStream twitterStream;

    @Autowired
    private UserStreamAdapterImpl statusAdapter;

    public static void main(final String... args) throws Exception {
        final OptionParser optionParser = new OptionParser();
        optionParser.allowsUnrecognizedOptions();
        optionParser.acceptsAll(Arrays.asList("g", "generate-tokens"))
                .withRequiredArg()
                .withValuesSeparatedBy(",");

        final OptionSet optionSet = optionParser.parse(args);
        if (optionSet.hasArgument("g")) {
            final List<String> values = (List<String>) optionSet.valuesOf("g");
            createTwitterOauthTokens(values.get(0), values.get(1));
        } else {
            SpringApplication.run(Application.class, args);
        }
    }

    @Override
    public void run(final ApplicationArguments args) throws Exception {

        if (twitterStream == null) {
            return;
        }

        twitterStream.addListener(this.statusAdapter);
        twitterStream.user();
    }

    static void createTwitterOauthTokens(final String consumerKey, final String consumerSecret) throws Exception {
        final Twitter twitter = TwitterFactory.getSingleton();
        twitter.setOAuthConsumer(consumerKey, consumerSecret);
        final RequestToken requestToken = twitter.getOAuthRequestToken();
        AccessToken accessToken = null;
        final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (null == accessToken) {
            System.out.println("Open the following URL and grant access to your account:");
            System.out.println(requestToken.getAuthorizationURL());
            System.out.print("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
            String pin = br.readLine();
            try {
                if (pin.length() > 0) {
                    accessToken = twitter.getOAuthAccessToken(requestToken, pin);
                } else {
                    accessToken = twitter.getOAuthAccessToken();
                }
            } catch (TwitterException te) {
                if (401 == te.getStatusCode()) {
                    System.out.println("Unable to get the access token.");
                } else {
                    throw te;
                }
            }
        }

        final Properties properties = new Properties();
        properties.put("twitter4j.oauth.consumerKey", consumerKey);
        properties.put("twitter4j.oauth.consumerSecret", consumerSecret);
        properties.put("twitter4j.oauth.accessToken", accessToken.getToken());
        properties.put("twitter4j.oauth.accessTokenSecret", accessToken.getTokenSecret());

        try (final FileOutputStream out = new FileOutputStream("application.properties", true)) {
            properties.store(out, null);
        }
    }
}
