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

import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.de.GermanStemFilterFactory;
import org.apache.lucene.analysis.snowball.SnowballPorterFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.AnalyzerDefs;
import org.hibernate.search.annotations.AnalyzerDiscriminator;
import org.hibernate.search.annotations.ClassBridge;
import org.hibernate.search.annotations.ClassBridges;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Parameter;
import org.hibernate.search.annotations.Spatial;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;
import org.hibernate.search.spatial.Coordinates;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author Michael J. Simons, 2016-09-05
 */
@Entity
@Table(name = "tweets")
@Indexed
@AnalyzerDefs({
    @AnalyzerDef(
            name = "en",
            tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
            filters = {
                @TokenFilterDef(factory = LowerCaseFilterFactory.class),
                @TokenFilterDef(factory = SnowballPorterFilterFactory.class,
                        params = {
                            @Parameter(name = "language", value = "English")
                        })
            }),
    @AnalyzerDef(
            name = "de",
            tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
            filters = {
                @TokenFilterDef(factory = LowerCaseFilterFactory.class),
                @TokenFilterDef(factory = GermanStemFilterFactory.class)
            }),
    @AnalyzerDef(
            name = "und",
            tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
            filters = {
                @TokenFilterDef(factory = LowerCaseFilterFactory.class)
            })
})
@ClassBridges({
    @ClassBridge(
            name = "year",
            index = Index.YES, analyze = Analyze.NO, store = Store.NO,
            impl = TweetYearBridge.class
    )
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TweetEntity implements Serializable {

    private static final long serialVersionUID = 5064886379919029239L;

    /**
     * Represents information to reconstruct the tweet in which the owning tweet
     * is a reply to.
     */
    @Embeddable
    @NoArgsConstructor
    @Getter
    public static class InReplyTo implements Serializable {

        private static final long serialVersionUID = -1282423230906376517L;

        /**
         * If the represented Tweet is a reply, this field will contain the
         * integer representation of the original Tweet’s ID.
         */
        @Column(name = "in_reply_to_status_id")
        @NotNull
        private Long inReplyToStatusId;

        /**
         * If the represented Tweet is a reply, this field will contain the
         * screen name of the original Tweet’s author.
         */
        @Column(name = "in_reply_to_screen_name")
        @NotNull
        @Field(name = "replied_to", index = Index.YES, analyze = Analyze.NO, store = Store.YES)
        private String inReplyToScreenName;

        /**
         * If the represented Tweet is a reply, this field will contain the
         * integer representation of the original Tweet’s author ID. This will
         * not necessarily always be the user directly mentioned in the Tweet.
         */
        @Column(name = "in_reply_to_user_id")
        @NotNull
        private Long inReplyToUserId;

        public InReplyTo(final Long inReplyToStatusId, final String inReplyToScreenName, final Long inReplyToUserId) {
            this.inReplyToStatusId = inReplyToStatusId;
            this.inReplyToScreenName = inReplyToScreenName;
            this.inReplyToUserId = inReplyToUserId;
        }
    }

    /**
     * Wrapper around latitude and longitude of a given location.
     */
    @Embeddable
    @NoArgsConstructor
    @Getter
    @EqualsAndHashCode(of = {"latitude", "longitude"})
    public static class Location implements Serializable, Coordinates {

        private static final long serialVersionUID = 1383123680180118517L;

        /**
         * Decimal latitude of this location.
         */
        @NotNull
        private Double latitude;

        /**
         * Decimal longitude of this location.
         */
        @NotNull
        private Double longitude;

        public Location(final Double latitude, final Double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    /**
     * Twitter status id.
     */
    @Id
    private long id;

    /**
     * Id of the twitter user that updated his status, retweeted or quoated a
     * tweet or replied to another.
     */
    @Column(name = "twitter_user_id", nullable = false)
    @NotNull
    private long twitterUserId;

    /**
     * UTC time when this Tweet was created.
     */
    @Column(name = "created_at", nullable = false)
    @NotNull
    @Field(store = Store.YES, index = Index.YES, analyze = Analyze.NO)
    private ZonedDateTime createdAt;

    /**
     * The actual tweet with all entites (short urls, image urls etc.) resolved
     * as plain url (not hyperlinked).
     */
    @Column(nullable = false)
    @NotBlank
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    @AnalyzerDiscriminator(impl = TweetLanguageDiscriminator.class)
    private String content;

    /**
     * Source of the tweets. Contains only the name, not a link.
     */
    @Column(nullable = false)
    @NotBlank
    @Field(index = Index.YES, analyze = Analyze.NO, store = Store.YES)
    private String source;

    /**
     * The raw json data of the original tweet object.
     */
    @Column(name = "raw_data", nullable = false, columnDefinition = "jsonb")
    @NotBlank
    private String rawData;

    /**
     * Facts used to reconstruct the replied tweet.
     */
    @Embedded
    @Setter
    @IndexedEmbedded
    private InReplyTo inReplyTo;

    /**
     * This field only surfaces when the Tweet is a quote Tweet. This field
     * contains the integer value Tweet ID of the quoted Tweet.
     */
    @Column(name = "quoted_status_id")
    @Setter
    private Long quotedStatusId;

    /**
     * Country code from the tweets place if available.
     */
    @Column(name = "country_code")
    @Setter
    @Field(store = Store.YES, index = Index.YES, analyze = Analyze.NO, name = "country_code")
    private String countryCode;

    /**
     * When present, indicates a BCP 47 language identifier corresponding to the
     * machine-detected language of the Tweet text, or “und” if no language
     * could be detected.
     */
    @Setter
    private String lang;

    /**
     * Exact location of this tweet.
     */
    @Embedded
    @Getter(onMethod = @__(
            @Spatial))
    @Setter
    private Location location;

    public TweetEntity(final long id, final long twitterUserId, final ZonedDateTime createdAt, final String content, final String source, final String rawData) {
        this.id = id;
        this.twitterUserId = twitterUserId;
        this.createdAt = createdAt;
        this.content = content;
        this.source = source;
        this.rawData = rawData;
    }
}
