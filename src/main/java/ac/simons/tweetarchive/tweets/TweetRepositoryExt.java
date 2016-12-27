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

import java.time.LocalDate;
import java.util.List;

/**
 * The part of the tweet repository that is not covered by Spring Data JPA.
 * Contains all the full text queries.
 *
 * @author Michael J. Simons, 2016-09-06
 */
public interface TweetRepositoryExt {

    /**
     * Searches tweets by keywords.
     *
     * @param keywords The kewords to search, separate by blanks
     * @param from Optional date range (start)
     * @param to Optional date range (end)
     * @return
     */
    List<TweetEntity> searchByKeyword(final String keywords, final LocalDate from, final LocalDate to);

    /**
     * Parses the query into a lucene query and handles it to the index.
     *
     * @param query The query to parse. Leading wildcards are not allowed
     * @return
     */
    List<TweetEntity> searchByQuery(final String query);

    /**
     * Retrieves the hierarchy of the tweet with the given id.
     *
     * @param id The id of the tweet whose hierarchy should be retrieved
     * @return A list of tweets
     */
    List<TweetEntity> getTweetHierarchy(final long id);
}
