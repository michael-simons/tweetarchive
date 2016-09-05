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

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Siva
 * @author Michael J. Simons
 */
@ConfigurationProperties(prefix = Twitter4jProperties.TWITTER4J_PREFIX)
@Getter
@Setter
public final class Twitter4jProperties {

    public static final String TWITTER4J_PREFIX = "twitter4j";

    /**
     * Enables debug output. Effective only with the embedded logger.
     */
    private boolean debug = false;

    private final OAuth oauth = new OAuth();

    @Getter
    @Setter
    public static class OAuth {

        /**
         * OAuth consumer key.
         */
        private String consumerKey;

        /**
         * OAuth consumer secret.
         */
        private String consumerSecret;

        /**
         * OAuth access token.
         */
        private String accessToken;

        /**
         * OAuth access token secret.
         */
        private String accessTokenSecret;
    }
}
