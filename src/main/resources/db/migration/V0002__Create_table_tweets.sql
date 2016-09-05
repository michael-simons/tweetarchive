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

create table tweets (
    id                      BIGINT PRIMARY KEY,
    twitter_user_id         BIGINT NOT NULL,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL,
    content                 TEXT NOT NULL,
    source                  VARCHAR(128) NOT NULL,
    in_reply_to_status_id   BIGINT,
    in_reply_to_screen_name VARCHAR(16),
    in_reply_to_user_id     BIGINT,
    quoted_status_id        BIGINT,
    country_code            VARCHAR(8),
    lang                    VARCHAR(8),
    latitude                DOUBLE PRECISION,
    longitude               DOUBLE PRECISION,
    raw_data                JSONB NOT NULL
);