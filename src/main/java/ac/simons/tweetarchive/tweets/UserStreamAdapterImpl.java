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

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterObjectFactory;
import twitter4j.UserStreamAdapter;

/**
 * @author Michael J. Simons, 2016-09-05
 */
@Component
@RequiredArgsConstructor
public final class UserStreamAdapterImpl extends UserStreamAdapter {

    private final TweetStorageService tweetStorageService;

    @Override
    public void onStatus(final Status status) {
        this.tweetStorageService.store(status, TwitterObjectFactory.getRawJSON(status));
    }

    @Override
    public void onDeletionNotice(final StatusDeletionNotice statusDeletionNotice) {
        this.tweetStorageService.delete(statusDeletionNotice.getStatusId());
    }
}
