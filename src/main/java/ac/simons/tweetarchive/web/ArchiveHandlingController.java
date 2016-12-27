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
package ac.simons.tweetarchive.web;

import ac.simons.tweetarchive.tweets.TweetStorageService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joor.Reflect;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.Status;

/**
 * @author Michael J. Simons, 2016-09-06
 */
@Controller
@RequestMapping("/upload")
@RequiredArgsConstructor
@Slf4j
public final class ArchiveHandlingController {

    private static final SimpleDateFormat DATE_FORMAT_IN = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US);
    private static final SimpleDateFormat DATE_FORMAT_OUT = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", Locale.US);
    private static final Pattern PATTERN_CREATED_AT = Pattern.compile("(\"created_at\" : )\"(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} \\+\\d{4})\"");

    private final TweetStorageService tweetStorageService;
    private final Reflect statusFactory = Reflect.on("twitter4j.StatusJSONImpl");

    @GetMapping
    public String index() {
        return "upload";
    }

    /**
     * As you can see, it get's nasty here...
     * <br>
     * Twitter4j doesn't offer an official way to parse Twitters JSON, so I
     * brute force my way into the twitter4j.StatusJSONImpl implementation of
     * Status.
     * <br>
     * And even if there was an official way, the JSON files inside the
     * official(!) Twitter archive differ from the API, even if they are said to
     * be identical. By the way, I'm not the only one, who
     * <a href="https://twittercommunity.com/t/why-does-twitter-json-archive-have-a-different-format-than-the-rest-api-1-1/35530">noticed
     * that</a>.
     * <br>
     * Furthermore, I didn't even bother to add error handling or tests.
     *
     * @param archive The uploaded archive
     * @return Redirect to the index
     * @throws java.io.IOException
     * @throws twitter4j.JSONException
     */
    @PostMapping
    public String store(
            @NotNull final MultipartFile archive,
            final RedirectAttributes redirectAttributes
    ) throws IOException, JSONException {
        try (final ZipInputStream archiv = new ZipInputStream(archive.getInputStream())) {
            ZipEntry entry;
            while ((entry = archiv.getNextEntry()) != null) {
                if (!entry.getName().startsWith("data/js/tweets/") || entry.isDirectory()) {
                    continue;
                }
                log.debug("Reading archive entry {}...", entry.getName());
                final BufferedReader buffer = new BufferedReader(new InputStreamReader(archiv, StandardCharsets.UTF_8));

                final String content
                        = buffer.lines().skip(1)
                        .map(l -> {
                            Matcher m = PATTERN_CREATED_AT.matcher(l);
                            String rv = l;
                            if (m.find()) {
                                try {
                                    rv = m.replaceFirst("$1\"" + DATE_FORMAT_OUT.format(DATE_FORMAT_IN.parse(m.group(2))) + "\"");
                                } catch (ParseException ex) {
                                    log.warn("Unexpected date format in twitter archive", ex);
                                }
                            }
                            return rv;
                        }).collect(Collectors.joining(""))
                        .replaceAll("\"sizes\" : \\[.+?\\],", "\"sizes\" : {},");

                final JSONArray statuses = new JSONArray(content);
                for (int i = 0; i < statuses.length(); ++i) {
                    final JSONObject rawJSON = statuses.getJSONObject(i);
                    // https://twitter.com/lukaseder/status/772772372990586882 ;)
                    final Status status = statusFactory.create(rawJSON).as(Status.class);
                    this.tweetStorageService.store(status, rawJSON.toString());
                }
            }
        }
        redirectAttributes.addFlashAttribute("message", "Done.");
        return "redirect:/upload";
    }
}
