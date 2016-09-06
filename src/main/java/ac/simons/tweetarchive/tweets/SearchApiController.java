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
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The API to the search repository.
 *
 * @author Michael J. Simons, 2016-09-06
 */
@RestController
@RequiredArgsConstructor
public final class SearchApiController {

    private final TweetRepository tweetRepository;

    /**
     * @param q The keywords to search for
     * @param from Optional date (formatted as yyyy-MM-dd)
     * @param to Optional date (formatted as yyyy-MM-dd)
     * @return
     */
    @GetMapping("/search")
    public List<TweetEntity> search(
            @NotNull @RequestParam final String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate to
    ) {
        return this.tweetRepository.searchByKeyword(q, from, to);
    }

    /**
     * @param q The query
     * @return
     */
    @GetMapping("/extendedSearch")
    public List<TweetEntity> extendedSearch(
            @NotNull @RequestParam final String q
    ) {
        return this.tweetRepository.searchByQuery(q);
    }
}
