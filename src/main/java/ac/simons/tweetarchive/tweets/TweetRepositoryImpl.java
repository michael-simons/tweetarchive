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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of our extended repository.
 *
 * @author Michael J. Simons, 2016-09-06
 */
@RequiredArgsConstructor
@Slf4j
public class TweetRepositoryImpl implements TweetRepositoryExt {

    private static final ZoneId UTC = ZoneId.of("UTC");

    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<TweetEntity> searchByKeyword(final String keywords, final LocalDate from, final LocalDate to) {
        // Must be retrieved inside a transaction to take part of
        final FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);

        // Prepare a search query builder
        final QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(TweetEntity.class).get();

        // This is a boolean junction... I'll add at least a keyword query
        final BooleanJunction<BooleanJunction> outer = queryBuilder.bool();
        outer.must(
                queryBuilder
                .keyword()
                .onFields("content")
                .matching(keywords)
                .createQuery()
        );

        // And then 2 range queries if from and to are not null
        Optional.ofNullable(from)
                .map(f -> f.atStartOfDay(UTC)) // Must be a zoned date time to fit the field
                .map(f -> queryBuilder.range().onField("created_at").above(f).createQuery())
                .ifPresent(q -> outer.must(q));
        Optional.ofNullable(to)
                .map(f -> f.plusDays(1).atStartOfDay(UTC)) // Same here, but a day later
                .map(f -> queryBuilder.range().onField("created_at").below(f).excludeLimit().createQuery()) // which i exclude
                .ifPresent(q -> outer.must(q));
        return fullTextEntityManager.createFullTextQuery(outer.createQuery(), TweetEntity.class).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TweetEntity> searchByQuery(final String query) {
        final FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        List<TweetEntity> rv;
        try {
            final QueryParser queryParser = new QueryParser("content", fullTextEntityManager.getSearchFactory().getAnalyzer(TweetEntity.class));
            rv = fullTextEntityManager.createFullTextQuery(queryParser.parse(query), TweetEntity.class).getResultList();
        } catch (ParseException e) {
            log.error("Could not parse query", e);
            rv = new ArrayList<>();
        }
        return rv;
    }
}
