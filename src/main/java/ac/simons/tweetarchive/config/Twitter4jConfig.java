/*
 * Copyright 2016 the original author or authors.
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
package ac.simons.tweetarchive.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * @author Siva
 * @author Michael J. Simons
 */
@Configuration
@ConditionalOnClass({TwitterStreamFactory.class, TwitterStream.class})
@ConditionalOnProperty(
        prefix = Twitter4jProperties.TWITTER4J_PREFIX,
        name = {"oauth.consumerKey", "oauth.consumerSecret", "oauth.accessToken", "oauth.accessTokenSecret"}
)
@EnableConfigurationProperties(Twitter4jProperties.class)
@RequiredArgsConstructor
@Slf4j
public class Twitter4jConfig {

    private final Twitter4jProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public TwitterStreamFactory twitterFactory() {
        final ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(properties.isDebug())
                .setOAuthConsumerKey(properties.getOauth().getConsumerKey())
                .setOAuthConsumerSecret(properties.getOauth().getConsumerSecret())
                .setOAuthAccessToken(properties.getOauth().getAccessToken())
                .setOAuthAccessTokenSecret(properties.getOauth().getAccessTokenSecret())
                .setJSONStoreEnabled(true);
        return new TwitterStreamFactory(cb.build());
    }

    @Bean
    @ConditionalOnMissingBean
    public TwitterStream twitterStream(final TwitterStreamFactory twitterStreamFactory) {
        return twitterStreamFactory.getInstance();
    }
}
