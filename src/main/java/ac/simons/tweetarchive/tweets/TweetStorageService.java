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

import ac.simons.tweetarchive.tweets.TweetEntity.InReplyTo;
import java.time.ZoneId;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import twitter4j.Place;
import twitter4j.Status;
import twitter4j.URLEntity;

/**
 * Parses {@link Status statuses} and their raw json representation and creates
 * {@link TweetEntity tweet entities}.
 *
 * @author Michael J. Simons, 2016-09-05
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TweetStorageService {

    /**
     * Pattern to extract the source from a tweet.
     */
    private static final Pattern SOURCE_PATTERN = Pattern.compile("<a.*?>(.*)</a>");

    private final TweetRepository tweetRepository;

    @Transactional
    public TweetEntity store(final Status status, final String rawContent) {
        final TweetEntity tweet = new TweetEntity(
                status.getId(),
                status.getUser().getId(),
                status.getCreatedAt().toInstant().atZone(ZoneId.of("UTC")),
                extractContent(status),
                extractSource(status),
                rawContent
        );
        tweet.setCountryCode(Optional.ofNullable(status.getPlace()).map(Place::getCountryCode).orElse(null));
        if (status.getInReplyToStatusId() != -1L && status.getInReplyToUserId() != -1L && status.getInReplyToScreenName() != null) {
            tweet.setInReplyTo(new InReplyTo(status.getInReplyToStatusId(), status.getInReplyToScreenName(), status.getInReplyToUserId()));
        }
        tweet.setLang(status.getLang());
        tweet.setLocation(Optional.ofNullable(status.getGeoLocation()).map(g -> new TweetEntity.Location(g.getLatitude(), g.getLongitude())).orElse(null));
        // TODO Handle quoted tweets
        return this.tweetRepository.save(tweet);
    }

    @Transactional
     public void delete(final long id) {
        long count = this.tweetRepository.deleteById(id);
        log.info("Deleted {} status...", count);
    }

    String extractContent(final Status status) {
        // TODO Handle quoted tweets
        final Status workStatus;
        if (status.isRetweet()) {
            workStatus = status.getRetweetedStatus();
        } else {
            workStatus = status;
        }

        final StringBuilder rv = new StringBuilder();

        final String text = workStatus.getText();
        int pos = 0;
        for (URLEntity urlEntity : workStatus.getURLEntities()) {
            rv.append(text.substring(pos, urlEntity.getStart()));
            rv.append(urlEntity.getExpandedURL());
            pos = urlEntity.getEnd();

        }
        if (pos <= text.length()) {
            rv.append(text.substring(pos, text.length()));
        }

        if (status.isRetweet()) {
            rv.insert(0, String.format("RT @%s: ", workStatus.getUser().getScreenName()));
        }
        return rv.toString();
    }

    String extractSource(final Status status) {
        return Optional.ofNullable(status.getSource())
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(SOURCE_PATTERN::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group(1).trim())
                .orElse(null);
    }
}
