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

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * This rest controller displays a single tweet and all replys to it.
 *
 * @author Michael J. Simons, 2016-12-27
 */
@RestController
@RequiredArgsConstructor
public class TweetsApiController {
    private final TweetRepository tweetRepository;

    @GetMapping("/tweets/{tweetId}")
    public List<TweetEntity> getTweet(@PathVariable final long tweetId) {
        return this.tweetRepository.getTweetHierarchy(tweetId);
    }
}
