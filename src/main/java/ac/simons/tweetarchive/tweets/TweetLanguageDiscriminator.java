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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.hibernate.search.analyzer.Discriminator;

/**
 * Used for selecting the correct analyzer based on the tweets detected
 * language.
 *
 * @author Michael J. Simons, 2016-09-05
 */
public final class TweetLanguageDiscriminator implements Discriminator {

    private static final List<String> SUPPORTED_LANGUAGES = Collections.unmodifiableList(Arrays.asList("de", "en"));

    @Override
    public String getAnalyzerDefinitionName(final Object value, final Object entity, final String field) {
        final TweetEntity tweet = (TweetEntity) entity;
        return Optional.ofNullable(tweet.getLang())
                .map(l -> l.toLowerCase(Locale.ENGLISH))
                .filter(l -> SUPPORTED_LANGUAGES.contains(l))
                .orElse("und");
    }
}
