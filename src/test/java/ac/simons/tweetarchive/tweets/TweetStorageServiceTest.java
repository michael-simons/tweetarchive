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
package ac.simons.tweetarchive.tweets;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import org.joor.Reflect;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.Status;

/**
 * @author Michael J. Simons, 2016-09-05
 */
@RunWith(MockitoJUnitRunner.class)
public class TweetStorageServiceTest {

    private final Status simpleTweet;
    
    @Mock
    private TweetRepository tweetRepository;

    public TweetStorageServiceTest() throws IOException, JSONException {
        final Reflect statusFactory = Reflect.on("twitter4j.StatusJSONImpl");
        try (final InputStream in = this.getClass().getResourceAsStream("/tweets/simple-tweet.json")) {
            simpleTweet = statusFactory.create(new JSONObject(new Scanner(in).useDelimiter("\\Z").next())).as(Status.class);
        }
    }

    @Test
    public void extracTextShouldWork() {
        final TweetStorageService tweetStorageService = new TweetStorageService(this.tweetRepository);
        Status status;

        status = simpleTweet;
        assertThat(tweetStorageService.extractContent(status), is("RT @euregjug: Meldet euch zum Vortrag am 14.9 an http://www.euregjug.eu/register/9 Ihr könnt eine @jetbrains Lizenz und Bücher aus dem @dpunkt_verlag gewinnen…"));
    }
    
    
    @Test
    public void extractSourceShouldWork() {
        final TweetStorageService tweetStorageService = new TweetStorageService(this.tweetRepository);
        Status status;

        status = simpleTweet;
        assertThat(tweetStorageService.extractSource(status), is("Twitter for Mac"));

        status = mock(Status.class);
        assertThat(tweetStorageService.extractSource(status), is(nullValue()));

        status = mock(Status.class);
        when(status.getSource()).thenReturn("");
        assertThat(tweetStorageService.extractSource(status), is(nullValue()));
    }
}
