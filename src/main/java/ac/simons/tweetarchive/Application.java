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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import twitter4j.TwitterStream;

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

    public static void main(final String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(final ApplicationArguments args) throws Exception {

        if (twitterStream == null) {
            return;
        }

        twitterStream.addListener(this.statusAdapter);
        twitterStream.user();
    }
}
